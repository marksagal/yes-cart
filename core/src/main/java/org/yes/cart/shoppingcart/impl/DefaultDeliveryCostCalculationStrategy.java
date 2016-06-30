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

package org.yes.cart.shoppingcart.impl;

import org.yes.cart.domain.entity.CarrierSla;
import org.yes.cart.service.domain.CarrierSlaService;
import org.yes.cart.shoppingcart.DeliveryCostCalculationStrategy;
import org.yes.cart.shoppingcart.MutableShoppingCart;
import org.yes.cart.shoppingcart.Total;
import org.yes.cart.util.ShopCodeContext;

import java.util.Map;

/**
 * User: denispavlov
 * Date: 13-10-20
 * Time: 6:07 PM
 */
public class DefaultDeliveryCostCalculationStrategy implements DeliveryCostCalculationStrategy {

    private static final Total ZERO_TOTAL = new TotalImpl();

    private final CarrierSlaService carrierSlaService;

    private final Map<String, DeliveryCostCalculationStrategy> subStrategies;

    public DefaultDeliveryCostCalculationStrategy(final CarrierSlaService carrierSlaService,
                                                  final Map<String, DeliveryCostCalculationStrategy> subStrategies) {
        this.carrierSlaService = carrierSlaService;
        this.subStrategies = subStrategies;
    }

    /** {@inheritDoc} */
    public Total calculate(final MutableShoppingCart cart) {

        cart.removeShipping();

        if (cart.getCarrierSlaId() != null) {
            final CarrierSla carrierSla = carrierSlaService.getById(cart.getCarrierSlaId());
            if (carrierSla != null) {

                final DeliveryCostCalculationStrategy strategy = subStrategies.get(carrierSla.getSlaType());
                if (strategy == null) {

                    ShopCodeContext.getLog(this).error("CarrierSla.slaType [{}] is not mapped to DeliveryCostCalculationStrategy", carrierSla.getSlaType());

                } else {

                    return strategy.calculate(cart);

                }

            }
        }
        return ZERO_TOTAL;

    }

}
