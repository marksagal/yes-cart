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

package org.yes.cart.domain.dto;

import org.yes.cart.domain.entity.Identifiable;

/**
 * DTO for {@link org.yes.cart.domain.entity.CustomerWishList}
 * <p/>
 * User: Igor Azarny iazarny@yahoo.com
 * Date: 07-May-2011
 * Time: 11:12:54
 */
public interface CustomerWishListDTO extends Identifiable {


    /**
     * Primary key value.
     *
     * @return key value.
     */
    long getCustomerwishlistId();

    /**
     * Set key value
     *
     * @param customerwishlistId value to set.
     */
    void setCustomerwishlistId(long customerwishlistId);


    /**
     * @return sku primary key
     */
    long getSkuId();

    /**
     * Set primary key value.
     *
     * @param skuId primary key value.
     */
    void setSkuId(long skuId);

    /**
     * Get the sku code.
     *
     * @return sku code
     */
    String getSkuCode();

    /**
     * Stock keeping unit code.
     * Limitation code must not contains underscore
     *
     * @param skuCode sku code
     */
    void setSkuCode(String skuCode);


    /**
     * Get the sku name.
     *
     * @return sku name
     */
    String getSkuName();

    /**
     * Stock keeping unit name.
     * sku name
     *
     * @param skuName sku name
     */
    void setSkuName(String skuName);


    /**
     * Get type of wish list item.
     *
     * @return type of wish list item
     */
    String getWlType();

    /**
     * Set type of wish list item
     *
     * @param wlType type of wish list item to set.
     */
    void setWlType(final String wlType);


    /**
     * Get customer id .
     *
     * @return customer id
     */
    long getCustomerId();


    /**
     * Set customer id.
     *
     * @param customerId customer id
     */
    void setCustomerId(long customerId);


}
