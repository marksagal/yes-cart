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

package org.yes.cart.bulkimport.xml.impl;

import org.apache.commons.lang.StringUtils;
import org.yes.cart.bulkcommon.model.ImpExTuple;
import org.yes.cart.bulkcommon.xml.XmlValueAdapter;
import org.yes.cart.bulkimport.xml.XmlEntityImportHandler;
import org.yes.cart.bulkimport.xml.XmlImportDescriptor;
import org.yes.cart.bulkimport.xml.internal.*;
import org.yes.cart.domain.entity.Seo;
import org.yes.cart.domain.i18n.I18NModel;
import org.yes.cart.domain.i18n.impl.StringI18NModel;
import org.yes.cart.service.async.JobStatusListener;
import org.yes.cart.util.DateUtils;

import java.time.LocalDateTime;

/**
 * User: denispavlov
 * Date: 05/11/2018
 * Time: 22:23
 */
public abstract class AbstractXmlEntityHandler<T, E> implements XmlEntityImportHandler<T> {

    protected static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private final String contextNamespace;
    private final String elementName;

    protected AbstractXmlEntityHandler(final String elementName) {
        this.elementName = elementName;
        this.contextNamespace = "org.yes.cart.bulkimport.xml.internal";
    }

    protected AbstractXmlEntityHandler(final String elementName, final String contextNamespace) {
        this.elementName = elementName;
        this.contextNamespace = contextNamespace;
    }

    @Override
    public String getContextNamespace() {
        return contextNamespace;
    }

    @Override
    public String getElementName() {
        return elementName;
    }

    @Override
    public void handle(final JobStatusListener statusListener, final XmlImportDescriptor xmlImportDescriptor, final ImpExTuple<String, T> tuple, final XmlValueAdapter xmlValueAdapter, final String fileToExport) {

        final T xmlType = tuple.getData();

        final E domain = getOrCreate(xmlType);

        final boolean isNew = isNew(domain);

        final EntityImportModeType mode = determineImportMode(xmlType);

        switch (mode) {
            case DELETE:
                if (!isNew) {
                    delete(domain);
                }
                break;
            case INSERT_ONLY:
                if (!isNew) {
                    break;
                }
            case UPDATE_ONLY:
                if (isNew) {
                    break;
                }
            case MERGE:
            default:
                saveOrUpdate(domain, xmlType, mode);

        }

    }

    /**
     * Determine import mode for give XML
     *
     * @param xmlType XML object
     *
     * @return import mode
     */
    protected abstract EntityImportModeType determineImportMode(final T xmlType);

    /**
     * Perform delete operation.
     *
     * @param domain domain object
     */
    protected abstract void delete(final E domain);

    /**
     * Perform save or update operation.
     *
     * @param domain  domain object
     * @param xmlType XML object
     * @param mode    desired mode
     */
    protected abstract void saveOrUpdate(final E domain, final T xmlType, final EntityImportModeType mode);

    /**
     * Process I18n XML chunk.
     *
     * @param i18ns    i18n block
     * @param existing existing value in domain object
     *
     * @return updated i18n for domain object
     */
    protected String processI18n(final I18NsType i18ns, final String existing) {
        final I18NImportModeType mode = i18ns != null && i18ns.getImportMode() != null ? i18ns.getImportMode() : I18NImportModeType.MERGE;
        return processI18nInternal(i18ns, existing, mode);
    }

    private String processI18nInternal(final I18NsType i18ns, final String existing, final I18NImportModeType mode) {
        final I18NModel model = mode == I18NImportModeType.REPLACE ? new StringI18NModel() : new StringI18NModel(existing);
        if (i18ns != null) {
            for (final I18NType i18n : i18ns.getI18N()) {
                model.putValue(i18n.getLang(), i18n.getValue());
            }
        }
        if (model.getAllValues().isEmpty()) {
            return null;
        }
        return model.toString();
    }

    /**
     * Process Local date/time from given XML string.
     *
     * @param ldt local date time
     *
     * @return date time
     */
    protected LocalDateTime processLDT(final String ldt) {
        if (StringUtils.isBlank(ldt)) {
            return null;
        }
        return DateUtils.ldtParse(ldt, TIMESTAMP_FORMAT);
    }

    /**
     * Update SEO element.
     *
     * @param seo       SEO XML
     * @param existing  existing SEO
     */
    protected void updateSeo(final SeoType seo, final Seo existing) {
        if (seo != null) {
            final I18NImportModeType mode = seo.getImportMode() != null ? seo.getImportMode() : I18NImportModeType.MERGE;
            if (mode == I18NImportModeType.MERGE) {
                if (StringUtils.isNotBlank(seo.getUri())) {
                    existing.setUri(seo.getUri());
                }
                if (StringUtils.isNotBlank(seo.getMetaTitle())) {
                    existing.setTitle(seo.getMetaTitle());
                }
                existing.setDisplayTitle(processI18nInternal(seo.getMetaTitleDisplay(), existing.getDisplayTitle(), mode));
                if (StringUtils.isNotBlank(seo.getMetaKeywords())) {
                    existing.setMetakeywords(seo.getMetaKeywords());
                }
                existing.setDisplayMetakeywords(processI18nInternal(seo.getMetaKeywordsDisplay(), existing.getDisplayMetakeywords(), mode));
                if (StringUtils.isNotBlank(seo.getMetaDescription())) {
                    existing.setMetadescription(seo.getMetaDescription());
                }
                existing.setDisplayMetadescription(processI18nInternal(seo.getMetaDescriptionDisplay(), existing.getDisplayMetadescription(), mode));
            } else { // replace
                existing.setUri(seo.getUri());
                existing.setTitle(seo.getMetaTitle());
                existing.setDisplayTitle(processI18nInternal(seo.getMetaTitleDisplay(), existing.getDisplayTitle(), mode));
                existing.setMetakeywords(seo.getMetaKeywords());
                existing.setDisplayMetakeywords(processI18nInternal(seo.getMetaKeywordsDisplay(), existing.getDisplayMetakeywords(), mode));
                existing.setMetadescription(seo.getMetaDescription());
                existing.setDisplayMetadescription(processI18nInternal(seo.getMetaDescriptionDisplay(), existing.getDisplayMetadescription(), mode));
            }
        }
    }

    /**
     * Retrieve domain object for given XML type
     *
     * @param xmlType XML object
     *
     * @return domain object
     */
    protected abstract E getOrCreate(final T xmlType);

    /**
     * Determine if domain object is new
     *
     * @param domain domain object
     *
     * @return new flag
     */
    protected abstract boolean isNew(final E domain);

}