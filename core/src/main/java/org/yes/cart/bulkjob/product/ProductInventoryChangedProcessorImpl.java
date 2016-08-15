/*
 * Copyright 2009 Denys Pavlov, Igor Azarnyi
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.yes.cart.bulkjob.product;

import org.slf4j.Logger;
import org.yes.cart.bulkjob.cron.AbstractLastRunDependentProcessorImpl;
import org.yes.cart.cache.CacheBundleHelper;
import org.yes.cart.cluster.node.NodeService;
import org.yes.cart.service.domain.ProductService;
import org.yes.cart.service.domain.RuntimeAttributeService;
import org.yes.cart.service.domain.SkuWarehouseService;
import org.yes.cart.service.domain.SystemService;
import org.yes.cart.util.ShopCodeContext;

import java.util.Date;
import java.util.List;

/**
 * Processor that scrolls though all modified inventory and re-indexes products so that
 * all latest information is propagated to all nodes.
 *
 * Last time this job runs is stored in system preferences: JOB_PROD_INV_UPDATE_LAST_RUN_[NODEID]
 * So that next run we only scan orders that have changed since last job run.
 *
 * User: denispavlov
 * Date: 27/04/2015
 * Time: 15:42
 */
public class ProductInventoryChangedProcessorImpl extends AbstractLastRunDependentProcessorImpl
        implements ProductInventoryChangedProcessorInternal {

    private static final String LAST_RUN_PREF = "JOB_PROD_INV_UPDATE_LAST_RUN_";

    private final SkuWarehouseService skuWarehouseService;
    private final ProductService productService;
    private final NodeService nodeService;
    private final CacheBundleHelper productCacheHelper;

    private int batchSize = 100;

    public ProductInventoryChangedProcessorImpl(final SkuWarehouseService skuWarehouseService,
                                                final ProductService productService,
                                                final NodeService nodeService,
                                                final SystemService systemService,
                                                final RuntimeAttributeService runtimeAttributeService,
                                                final CacheBundleHelper productCacheHelper) {
        super(systemService, runtimeAttributeService);
        this.skuWarehouseService = skuWarehouseService;
        this.productService = productService;
        this.nodeService = nodeService;
        this.productCacheHelper = productCacheHelper;
    }

    /** {@inheritDoc} */
    @Override
    protected String getLastRunPreferenceAttributeName() {
        return LAST_RUN_PREF + getNodeId();
    }

    /** {@inheritDoc} */
    @Override
    protected void doRun(final Date lastRun) {

        final Logger log = ShopCodeContext.getLog(this);

        final String nodeId = getNodeId();

        if (isLuceneIndexDisabled()) {
            log.info("Reindexing products inventory updates on {} ... disabled", nodeId);
            return;
        }

        final long start = System.currentTimeMillis();

        log.info("Check changed orders products to be reindexed on {}, batch {}", nodeId, batchSize);

        final List<String> productSkus = skuWarehouseService.findProductSkuForWhichInventoryChangedAfter(lastRun);

        if (productSkus != null && !productSkus.isEmpty()) {

            log.info("Inventory changed for {} since {}", productSkus.size(), lastRun);

            int fromIndex = 0;
            int toIndex = 0;
            while (fromIndex < productSkus.size()) {

                toIndex = fromIndex + batchSize > productSkus.size() ? productSkus.size() : fromIndex + batchSize;
                final List<String> skuBatch = productSkus.subList(fromIndex, toIndex);
                log.info("Reindexing SKU {}  ... so far reindexed {}", skuBatch, fromIndex);

                self().reindexBatch(skuBatch);

                fromIndex = toIndex;

            }

            flushCaches();

        }

        log.info("Reindexing inventory updates on {}, reindexed {}", nodeId, productSkus.size());

        final long finish = System.currentTimeMillis();

        final long ms = (finish - start);

        log.info("Reindexing inventory updates on {} ... completed in {}s", nodeId, (ms > 0 ? ms / 1000 : 0));

    }

    protected void flushCaches() {

        productCacheHelper.flushBundleCaches();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reindexBatch(final List<String> skuCodes) {

        for (final String sku : skuCodes) {

            productService.reindexProductSku(sku);

        }

    }

    protected String getNodeId() {
        return nodeService.getCurrentNodeId();
    }

    protected Boolean isLuceneIndexDisabled() {
        return nodeService.getCurrentNode().isFtIndexDisabled();
    }

    /**
     * Batch size for remote index update.
     *
     * @param batchSize batch size
     */
    public void setBatchSize(final int batchSize) {
        this.batchSize = batchSize;
    }

    private ProductInventoryChangedProcessorInternal self;

    private ProductInventoryChangedProcessorInternal self() {
        if (self == null) {
            self = getSelf();
        }
        return self;
    }

    public ProductInventoryChangedProcessorInternal getSelf() {
        return null;
    }

}
