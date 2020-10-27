/*
 * Copyright 2009 Inspire-Software.com
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

package org.yes.cart.domain.entity;

/**
 * User: denispavlov
 * Date: 14-06-03
 * Time: 5:04 PM
 */
public interface PromotionCouponUsage extends Auditable {

    /**
     * @return promotion PK
     */
    long getPromotioncouponusageId();

    /**
     * @param promotioncouponusageId promotion PK
     */
    void setPromotioncouponusageId(long promotioncouponusageId);

    /**
     * @return coupon for this usage
     */
    String getCouponCode();

    /**
     * @param couponCode coupon for this usage
     */
    void setCouponCode(String couponCode);

    /**
     * @return customer email for customer who used the coupon
     */
    String getCustomerRef();

    /**
     * @param customerRef customer email for customer who used the coupon
     */
    void setCustomerRef(String customerRef);

    /**
     * @return order for which this coupon was used
     */
    CustomerOrder getCustomerOrder();

    /**
     * @param order order for which this coupon was used
     */
    void setCustomerOrder(CustomerOrder order);

}
