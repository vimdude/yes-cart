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

package org.yes.cart.web.resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yes.cart.domain.entity.Customer;
import org.yes.cart.domain.entity.CustomerOrder;
import org.yes.cart.domain.entity.Shop;
import org.yes.cart.shoppingcart.ShoppingCart;
import org.yes.cart.web.application.ApplicationDirector;
import org.yes.cart.web.support.service.CheckoutServiceFacade;
import org.yes.cart.web.support.service.CustomerServiceFacade;

import java.io.ByteArrayOutputStream;

/**
 * User: denispavlov
 * Date: 29/10/2015
 * Time: 22:00
 */
public class OrderReceiptPdfResource extends AbstractDynamicResource {

    private static final Logger LOG = LoggerFactory.getLogger(OrderReceiptPdfResource.class);

    private final CheckoutServiceFacade checkoutServiceFacade;
    private final CustomerServiceFacade customerServiceFacade;

    public OrderReceiptPdfResource(final CheckoutServiceFacade checkoutServiceFacade,
                                   final CustomerServiceFacade customerServiceFacade) {
        super("application/pdf");
        this.checkoutServiceFacade = checkoutServiceFacade;
        this.customerServiceFacade = customerServiceFacade;
    }

    @Override
    protected byte[] getData(final Attributes attributes) {

        String ordernum = attributes.getParameters().get("ordernum").toString();
        String guestEmail = null;
        if (StringUtils.isBlank(ordernum)) {
            ordernum = attributes.getRequest().getPostParameters().getParameterValue("ordernum").toString();
            guestEmail = attributes.getRequest().getPostParameters().getParameterValue("guestEmail").toString();
        }

        if (StringUtils.isNotBlank(ordernum)) {

            final CustomerOrder order = checkoutServiceFacade.findByReference(ordernum);

            if (order != null) {
                final ShoppingCart cart = ApplicationDirector.getShoppingCart();
                final Shop shop = ApplicationDirector.getCurrentShop();
                final boolean loggedIn = cart != null && cart.getLogonState() == ShoppingCart.LOGGED_IN;
                final Customer customer = loggedIn ? customerServiceFacade.getCustomerByLogin(shop, cart.getCustomerLogin()) : null;

                final boolean ownsOrder = (customer != null && customer.getCustomerId() == order.getCustomer().getCustomerId())
                        || (!loggedIn && guestEmail != null && guestEmail.equalsIgnoreCase(order.getEmail()));

                if (ownsOrder) {

                    try {
                        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        checkoutServiceFacade.printOrderByReference(ordernum, baos);
                        baos.close();
                        return baos.toByteArray();
                    } catch (Exception exp) {
                        LOG.error("Receipt for " + ordernum
                                + " error, caused by " + exp.getMessage(), exp);
                    }
                    
                }
            }
        }

        return new byte[0];

    }
}
