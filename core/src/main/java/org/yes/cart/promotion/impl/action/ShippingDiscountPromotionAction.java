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

package org.yes.cart.promotion.impl.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yes.cart.promotion.PromotionAction;
import org.yes.cart.service.order.DeliveryBucket;
import org.yes.cart.shoppingcart.CartItem;
import org.yes.cart.shoppingcart.MutableShoppingCart;
import org.yes.cart.utils.MoneyUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * User: denispavlov
 * Date: 13-10-30
 * Time: 8:15 AM
 */
public class ShippingDiscountPromotionAction extends AbstractShippingPromotionAction implements PromotionAction {

    private static final Logger LOG = LoggerFactory.getLogger(ShippingDiscountPromotionAction.class);

    /** {@inheritDoc} */
    @Override
    public BigDecimal testDiscountValue(final Map<String, Object> context) {
        return getDiscountValue(getRawPromotionActionContext(context));
    }

    private BigDecimal getDiscountValue(final String ctx) {
        try {
            return new BigDecimal(ctx).movePointLeft(2);
        } catch (Exception exp) {
            LOG.error("Unable to parse discount for promotion action context: {}", ctx);
        }
        return BigDecimal.ZERO;
    }

    /** {@inheritDoc} */
    @Override
    public void perform(final Map<String, Object> context) {
        final BigDecimal discount = getDiscountValue(getRawPromotionActionContext(context));
        if (MoneyUtils.isPositive(discount)) {
            final MutableShoppingCart cart = getShoppingCart(context);
            final CartItem shipping = getShipping(context);
            final DeliveryBucket bucket = shipping.getDeliveryBucket();

            // calculate discount relative to sale price
            final BigDecimal saleDiscount = nullSafeItemPriceForCalculation(shipping).multiply(discount);
            final BigDecimal promoPrice;
            if (MoneyUtils.isFirstBiggerThanSecond(shipping.getPrice(), saleDiscount)) {
                // we may have compound discounts so need to use final price
                promoPrice = shipping.getPrice().subtract(saleDiscount).setScale(2, RoundingMode.HALF_DOWN);
            } else {
                promoPrice = MoneyUtils.ZERO;
            }

            cart.setShippingPromotion(shipping.getProductSkuCode(), bucket, promoPrice, getPromotionCode(context));

        }
    }
}
