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

package org.yes.cart.web.service.rest;

import org.hamcrest.CustomMatchers;
import org.hamcrest.core.StringContains;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MvcResult;
import org.yes.cart.domain.entity.Mail;
import org.yes.cart.domain.entity.ShoppingCartState;
import org.yes.cart.domain.ro.*;
import org.yes.cart.domain.ro.xml.XMLParamsRO;
import org.yes.cart.service.domain.ShoppingCartStateService;
import org.yes.cart.shoppingcart.ShoppingCart;
import org.yes.cart.shoppingcart.ShoppingCartCommand;
import org.yes.cart.shoppingcart.support.tokendriven.CartRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.CustomMockMvcResultHandlers.print;

/**
 * User: denispavlov
 * Date: 01/04/2015
 * Time: 12:25
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:testApplicationContext.xml")
@WebAppConfiguration
public class CheckoutSuiteTest extends AbstractSuiteTest {

    private final Pattern ADDRESS_ID_JSON = Pattern.compile("\"addressId\":([0-9]*),");
    private final Pattern ADDRESS_ID_XML = Pattern.compile("address-id=\"([0-9]*)\"");

    @Autowired
    private ShoppingCartStateService shoppingCartStateService;

    @Autowired
    private CartRepository cartRepository;

    @Test
    public void testCheckoutJson() throws Exception {

        reindex();

        final String email = "bob.doe@checkout-json.com";

        assertFalse("Customer not yet registered", hasEmails(email));

        final byte[] regBody = toJsonBytesRegistrationDetails(email);


        final MvcResult regResult =
                mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .locale(LOCALE)
                        .content(regBody))
                        .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().string(StringContains.containsString("uuid")))
                    .andExpect(header().string(X_CW_TOKEN, CustomMatchers.isNotBlank()))
                    .andReturn();

        final String uuid = regResult.getResponse().getHeader(X_CW_TOKEN);

        final ShoppingCartState state = shoppingCartStateService.findByGuid(uuid);
        assertNotNull(uuid, state);
        assertEquals(email, state.getCustomerLogin());

        final ShoppingCart cart = cartRepository.getShoppingCart(uuid);
        assertNotNull(uuid, cart);
        assertEquals(email, cart.getCustomerLogin());

        mockMvc.perform(get("/auth/check")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(StringContains.containsString("\"authenticated\":true")))
            .andExpect(header().string(X_CW_TOKEN, uuid));

        final byte[] shippingAddress = toJsonBytesAddressDetails("S", "UA-UA", "UA");

        final MvcResult shipAddress = mockMvc.perform(post("/customer/addressbook")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .locale(LOCALE)
                    .header(X_CW_TOKEN, uuid)
                    .content(shippingAddress))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("UA-UA")))
                .andExpect(header().string(X_CW_TOKEN, uuid))
                .andReturn();

        final Matcher matcherS = ADDRESS_ID_JSON.matcher(shipAddress.getResponse().getContentAsString());
        matcherS.find();
        final String shippingAddressId = matcherS.group(1);

        mockMvc.perform(get("/cart/options/addresses/S")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .locale(LOCALE)
                    .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("UA")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        final byte[] billingAddress = toJsonBytesAddressDetails("B", "GB-GB", "GB");

        final MvcResult billAddress = mockMvc.perform(post("/customer/addressbook")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .locale(LOCALE)
                    .header(X_CW_TOKEN, uuid)
                    .content(billingAddress))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("GB")))
                .andExpect(header().string(X_CW_TOKEN, uuid))
                .andReturn();

        final Matcher matcherB = ADDRESS_ID_JSON.matcher(billAddress.getResponse().getContentAsString());
        matcherB.find();
        final String billingAddressId = matcherB.group(1);

        mockMvc.perform(get("/cart/options/addresses/B")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .locale(LOCALE)
                    .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("GB")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        mockMvc.perform(post("/cart/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("\"checkoutBlocked\":true")))
                .andExpect(content().string(StringContains.containsString("\"messageKey\":\"emptyCart\"")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        final byte[] addToCart = toJsonBytes(new XMLParamsRO(new HashMap<String, String>() {{
            put(ShoppingCartCommand.CMD_ADDTOCART, "BENDER-ua");
            put(ShoppingCartCommand.CMD_P_SUPPLIER, "WAREHOUSE_2");
        }}));

        mockMvc.perform(post("/cart")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .locale(LOCALE)
                    .header(X_CW_TOKEN, uuid)
                    .content(addToCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("BENDER-ua")))
                .andExpect(content().string(StringContains.containsString("currencyCode\":\"EUR\"")))
                .andExpect(content().string(StringContains.containsString("deliveryAddressId\":null")))
                .andExpect(content().string(StringContains.containsString("billingAddressId\":null")))
                .andExpect(content().string(StringContains.containsString("separateBillingAddress\":false")))
                .andExpect(content().string(StringContains.containsString("shopId\":10,")))
                .andExpect(content().string(StringContains.containsString("customerShopId\":10,")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        mockMvc.perform(post("/cart/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("\"checkoutBlocked\":false")))
                .andExpect(header().string(X_CW_TOKEN, uuid));



        final AddressOptionRO shipAddressOptionRO = new AddressOptionRO();
        shipAddressOptionRO.setAddressId(shippingAddressId);
        final byte[] setShippingCart = toJsonBytes(shipAddressOptionRO);

        mockMvc.perform(post("/cart/options/addresses/S")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .locale(LOCALE)
                    .header(X_CW_TOKEN, uuid)
                    .content(setShippingCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("deliveryAddressId\":" + shippingAddressId)))
                .andExpect(content().string(StringContains.containsString("billingAddressId\":null")))
                .andExpect(content().string(StringContains.containsString("separateBillingAddress\":true")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        final AddressOptionRO billAddressOptionRO = new AddressOptionRO();
        billAddressOptionRO.setAddressId(billingAddressId);
        final byte[] setBillingCart = toJsonBytes(billAddressOptionRO);

        mockMvc.perform(post("/cart/options/addresses/B")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .locale(LOCALE)
                    .header(X_CW_TOKEN, uuid)
                    .content(setBillingCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("deliveryAddressId\":" + shippingAddressId)))
                .andExpect(content().string(StringContains.containsString("billingAddressId\":" + billingAddressId)))
                .andExpect(content().string(StringContains.containsString("separateBillingAddress\":true")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        final AddressOptionRO billAddressOptionROSame = new AddressOptionRO();
        billAddressOptionROSame.setAddressId(billingAddressId);
        billAddressOptionROSame.setShippingSameAsBilling(true);
        final byte[] setBillingSameCart = toJsonBytes(billAddressOptionROSame);

        mockMvc.perform(post("/cart/options/addresses/B")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .locale(LOCALE)
                    .header(X_CW_TOKEN, uuid)
                    .content(setBillingSameCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("deliveryAddressId\":" + billingAddressId)))
                .andExpect(content().string(StringContains.containsString("billingAddressId\":" + billingAddressId)))
                .andExpect(content().string(StringContains.containsString("separateBillingAddress\":false")))
                .andExpect(content().string(StringContains.containsString("carrierSlaId\":{}")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        mockMvc.perform(get("/cart/options/shipping")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .locale(LOCALE)
                    .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("\"supplier\":\"WAREHOUSE_2")))
                .andExpect(content().string(StringContains.containsString("\"carrierId\":1")))
                .andExpect(content().string(StringContains.containsString("\"carrierslaId\":4")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        final ShippingOptionRO shippingOptionRO = new ShippingOptionRO();
        shippingOptionRO.setShippingMethods(new ShippingOptionCarrierSelectionsRO());
        shippingOptionRO.getShippingMethods().setSelected(new ArrayList<>());
        final ShippingOptionCarrierSelectionRO shippingOptionCarrierSelectionRO = new ShippingOptionCarrierSelectionRO();
        shippingOptionCarrierSelectionRO.setCarrierSlaId("4");
        shippingOptionCarrierSelectionRO.setSupplier("WAREHOUSE_2");
        shippingOptionRO.getShippingMethods().getSelected().add(shippingOptionCarrierSelectionRO);

        final byte[] setCarrierCart = toJsonBytes(shippingOptionRO);

        mockMvc.perform(post("/cart/options/shipping")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .locale(LOCALE)
                    .header(X_CW_TOKEN, uuid)
                    .content(setCarrierCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("deliveryAddressId\":" + billingAddressId)))
                .andExpect(content().string(StringContains.containsString("billingAddressId\":" + billingAddressId)))
                .andExpect(content().string(StringContains.containsString("separateBillingAddress\":false")))
                .andExpect(content().string(StringContains.containsString("carrierSlaId\":{\"WAREHOUSE_2\":4}")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        final byte[] addMessageToCart = toJsonBytes(new XMLParamsRO(new HashMap<String, String>() {{
            put(ShoppingCartCommand.CMD_SETORDERMSG, "My Message");
        }}));

        mockMvc.perform(post("/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(addMessageToCart))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(StringContains.containsString("\"orderMessage\":\"My Message\"")))
            .andExpect(header().string(X_CW_TOKEN, uuid));

        mockMvc.perform(get("/cart/options/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(StringContains.containsString("testPaymentGatewayLabel")))
            .andExpect(content().string(StringContains.containsString("courierPaymentGatewayLabel")))
            .andExpect(header().string(X_CW_TOKEN, uuid));

        final PaymentGatewayOptionRO pgOption = new PaymentGatewayOptionRO();
        pgOption.setPgLabel("courierPaymentGatewayLabel");

        final byte[] pgOptionBody = toJsonBytes(pgOption);

        mockMvc.perform(post("/cart/options/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(pgOptionBody))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(StringContains.containsString("courierPaymentGatewayLabel")))
            .andExpect(header().string(X_CW_TOKEN, uuid));

        final OrderDeliveryOptionRO delOption = new OrderDeliveryOptionRO();

        final byte[] delOptionBody = toJsonBytes(delOption);

        mockMvc.perform(post("/order/preview")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(delOptionBody))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(StringContains.containsString("success\":true")))
            .andExpect(content().string(StringContains.containsString("customerorderId\":")))
            .andExpect(header().string(X_CW_TOKEN, uuid));


        final MvcResult placed = mockMvc.perform(post("/order/place")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .locale(LOCALE)
                    .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("success\":true")))
                .andExpect(content().string(StringContains.containsString("customerorderId\":")))
                .andExpect(header().string(X_CW_TOKEN, CustomMatchers.isNotBlank()))
                .andReturn();

        final String tokenAfterPlacing = placed.getResponse().getHeader(X_CW_TOKEN);
        assertFalse(tokenAfterPlacing.equals(uuid));

        final Mail newOrder = getEmailBySubjectLike( "New order", email);
        assertNotNull("New order email", newOrder);
        assertTrue(newOrder.getHtmlVersion().contains("Dear <b>Bob Doe</b>!<br>"));
        assertTrue(newOrder.getHtmlVersion().contains("New order"));
        assertTrue(newOrder.getHtmlVersion().contains("BENDER-ua"));
        assertTrue(newOrder.getHtmlVersion().contains("<td align=\"right\"><b>109.99</b></td>"));
        assertTrue(newOrder.getHtmlVersion().contains("My Message"));
        assertTrue(newOrder.getHtmlVersion().contains("https://www.gadget.yescart.org/order?order="));


        mockMvc.perform(get("/customer/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .locale(LOCALE)
                    .header(X_CW_TOKEN, tokenAfterPlacing))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("customerorderId\":")))
                .andExpect(header().string(X_CW_TOKEN, tokenAfterPlacing));

        mockMvc.perform(get("/customer/orders/2014-01-01")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .locale(LOCALE)
                    .header(X_CW_TOKEN, tokenAfterPlacing))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("customerorderId\":")))
                .andExpect(content().string(StringContains.containsString("shopId\":10,")))
                .andExpect(header().string(X_CW_TOKEN, tokenAfterPlacing));


    }


    @Test
    public void testSubCheckoutJson() throws Exception {

        reindex();

        final String email = "bob.doe@sub-checkout-json.com";

        assertFalse("Customer not yet registered", hasEmails(email));
        
        final byte[] regBody = toJsonBytesSubRegistrationDetails(email);


        final MvcResult regResult =
                mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .locale(LOCALE)
                        .content(regBody))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(content().string(StringContains.containsString("uuid")))
                        .andExpect(header().string(X_CW_TOKEN, CustomMatchers.isNotBlank()))
                        .andReturn();

        final String uuid = regResult.getResponse().getHeader(X_CW_TOKEN);

        final ShoppingCartState state = shoppingCartStateService.findByGuid(uuid);
        assertNotNull(uuid, state);
        assertEquals(email, state.getCustomerLogin());

        final ShoppingCart cart = cartRepository.getShoppingCart(uuid);
        assertNotNull(uuid, cart);
        assertEquals(email, cart.getCustomerLogin());

        mockMvc.perform(get("/auth/check")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("\"authenticated\":true")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        final byte[] shippingAddress = toJsonBytesAddressDetails("S", "UA-UA", "UA");

        final MvcResult shipAddress = mockMvc.perform(post("/customer/addressbook")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(shippingAddress))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("UA-UA")))
                .andExpect(header().string(X_CW_TOKEN, uuid))
                .andReturn();

        final Matcher matcherS = ADDRESS_ID_JSON.matcher(shipAddress.getResponse().getContentAsString());
        matcherS.find();
        final String shippingAddressId = matcherS.group(1);

        mockMvc.perform(get("/cart/options/addresses/S")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("UA")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        final byte[] billingAddress = toJsonBytesAddressDetails("B", "GB-GB", "GB");

        final MvcResult billAddress = mockMvc.perform(post("/customer/addressbook")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(billingAddress))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("GB")))
                .andExpect(header().string(X_CW_TOKEN, uuid))
                .andReturn();

        final Matcher matcherB = ADDRESS_ID_JSON.matcher(billAddress.getResponse().getContentAsString());
        matcherB.find();
        final String billingAddressId = matcherB.group(1);

        mockMvc.perform(get("/cart/options/addresses/B")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("GB")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        mockMvc.perform(post("/cart/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("\"checkoutBlocked\":true")))
                .andExpect(content().string(StringContains.containsString("\"messageKey\":\"emptyCart\"")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        final byte[] addToCart = toJsonBytes(new XMLParamsRO(new HashMap<String, String>() {{
            put(ShoppingCartCommand.CMD_ADDTOCART, "BENDER-ua");
            put(ShoppingCartCommand.CMD_P_SUPPLIER, "WAREHOUSE_2");
        }}));

        mockMvc.perform(post("/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(addToCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("BENDER-ua")))
                .andExpect(content().string(StringContains.containsString("currencyCode\":\"EUR\"")))
                .andExpect(content().string(StringContains.containsString("deliveryAddressId\":null")))
                .andExpect(content().string(StringContains.containsString("billingAddressId\":null")))
                .andExpect(content().string(StringContains.containsString("separateBillingAddress\":false")))
                .andExpect(content().string(StringContains.containsString("shopId\":10,")))
                .andExpect(content().string(StringContains.containsString("customerShopId\":1010,")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        mockMvc.perform(post("/cart/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("\"checkoutBlocked\":false")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        final AddressOptionRO shipAddressOptionRO = new AddressOptionRO();
        shipAddressOptionRO.setAddressId(shippingAddressId);
        final byte[] setShippingCart = toJsonBytes(shipAddressOptionRO);

        mockMvc.perform(post("/cart/options/addresses/S")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(setShippingCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("deliveryAddressId\":" + shippingAddressId)))
                .andExpect(content().string(StringContains.containsString("billingAddressId\":null")))
                .andExpect(content().string(StringContains.containsString("separateBillingAddress\":true")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        final AddressOptionRO billAddressOptionRO = new AddressOptionRO();
        billAddressOptionRO.setAddressId(billingAddressId);
        final byte[] setBillingCart = toJsonBytes(billAddressOptionRO);

        mockMvc.perform(post("/cart/options/addresses/B")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(setBillingCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("deliveryAddressId\":" + shippingAddressId)))
                .andExpect(content().string(StringContains.containsString("billingAddressId\":" + billingAddressId)))
                .andExpect(content().string(StringContains.containsString("separateBillingAddress\":true")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        final AddressOptionRO billAddressOptionROSame = new AddressOptionRO();
        billAddressOptionROSame.setAddressId(billingAddressId);
        billAddressOptionROSame.setShippingSameAsBilling(true);
        final byte[] setBillingSameCart = toJsonBytes(billAddressOptionROSame);

        mockMvc.perform(post("/cart/options/addresses/B")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(setBillingSameCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("deliveryAddressId\":" + billingAddressId)))
                .andExpect(content().string(StringContains.containsString("billingAddressId\":" + billingAddressId)))
                .andExpect(content().string(StringContains.containsString("separateBillingAddress\":false")))
                .andExpect(content().string(StringContains.containsString("carrierSlaId\":{}")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        mockMvc.perform(get("/cart/options/shipping")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("\"supplier\":\"WAREHOUSE_2")))
                .andExpect(content().string(StringContains.containsString("\"carrierId\":1")))
                .andExpect(content().string(StringContains.containsString("\"carrierslaId\":4")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        final ShippingOptionRO shippingOptionRO = new ShippingOptionRO();
        shippingOptionRO.setShippingMethods(new ShippingOptionCarrierSelectionsRO());
        shippingOptionRO.getShippingMethods().setSelected(new ArrayList<>());
        final ShippingOptionCarrierSelectionRO shippingOptionCarrierSelectionRO = new ShippingOptionCarrierSelectionRO();
        shippingOptionCarrierSelectionRO.setCarrierSlaId("4");
        shippingOptionCarrierSelectionRO.setSupplier("WAREHOUSE_2");
        shippingOptionRO.getShippingMethods().getSelected().add(shippingOptionCarrierSelectionRO);


        final byte[] setCarrierCart = toJsonBytes(shippingOptionRO);

        mockMvc.perform(post("/cart/options/shipping")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(setCarrierCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("deliveryAddressId\":" + billingAddressId)))
                .andExpect(content().string(StringContains.containsString("billingAddressId\":" + billingAddressId)))
                .andExpect(content().string(StringContains.containsString("separateBillingAddress\":false")))
                .andExpect(content().string(StringContains.containsString("carrierSlaId\":{\"WAREHOUSE_2\":4}")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        final byte[] addMessageToCart = toJsonBytes(new XMLParamsRO(new HashMap<String, String>() {{
            put(ShoppingCartCommand.CMD_SETORDERMSG, "My Message");
        }}));

        mockMvc.perform(post("/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(addMessageToCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("\"orderMessage\":\"My Message\"")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        mockMvc.perform(get("/cart/options/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("testPaymentGatewayLabel")))
                .andExpect(content().string(StringContains.containsString("courierPaymentGatewayLabel")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        final PaymentGatewayOptionRO pgOption = new PaymentGatewayOptionRO();
        pgOption.setPgLabel("courierPaymentGatewayLabel");

        final byte[] pgOptionBody = toJsonBytes(pgOption);

        mockMvc.perform(post("/cart/options/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(pgOptionBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("courierPaymentGatewayLabel")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        final OrderDeliveryOptionRO delOption = new OrderDeliveryOptionRO();

        final byte[] delOptionBody = toJsonBytes(delOption);

        mockMvc.perform(post("/order/preview")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(delOptionBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("success\":true")))
                .andExpect(content().string(StringContains.containsString("customerorderId\":")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        final MvcResult placed = mockMvc.perform(post("/order/place")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("success\":true")))
                .andExpect(content().string(StringContains.containsString("customerorderId\":")))
                .andExpect(header().string(X_CW_TOKEN, CustomMatchers.isNotBlank()))
                .andReturn();

        final String tokenAfterPlacing = placed.getResponse().getHeader(X_CW_TOKEN);
        assertFalse(tokenAfterPlacing.equals(uuid));

        final Mail newOrder = getEmailBySubjectLike( "New order", email);
        assertNotNull("New order email", newOrder);
        assertTrue(newOrder.getHtmlVersion().contains("Dear <b>Bob Doe</b>!<br>"));
        assertTrue(newOrder.getHtmlVersion().contains("New order"));
        assertTrue(newOrder.getHtmlVersion().contains("BENDER-ua"));
        assertTrue(newOrder.getHtmlVersion().contains("<td align=\"right\"><b>109.99</b></td>"));
        assertTrue(newOrder.getHtmlVersion().contains("My Message"));
        assertTrue(newOrder.getHtmlVersion().contains("https://www.gadget.yescart.org/order?order="));


        mockMvc.perform(get("/customer/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, tokenAfterPlacing))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("customerorderId\":")))
                .andExpect(header().string(X_CW_TOKEN, tokenAfterPlacing));

        mockMvc.perform(get("/customer/orders/2014-01-01")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, tokenAfterPlacing))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("customerorderId\":")))
                .andExpect(content().string(StringContains.containsString("shopId\":1010,")))
                .andExpect(header().string(X_CW_TOKEN, tokenAfterPlacing));



    }


    @Test
    public void testCheckoutNoLoginJson() throws Exception {

        reindex();

        final MvcResult authResult = mockMvc.perform(get("/auth/check")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("\"authenticated\":false")))
                .andReturn();

        final String uuid = authResult.getResponse().getHeader(X_CW_TOKEN);

        final ShoppingCartState state = shoppingCartStateService.findByGuid(uuid);
        assertNotNull(uuid, state);
        assertNull(state.getCustomerLogin());

        final ShoppingCart cart = cartRepository.getShoppingCart(uuid);
        assertNotNull(uuid, cart);
        assertNull(cart.getCustomerLogin());

        final byte[] shippingAddress = toJsonBytesAddressDetails("S", "UA-UA", "UA");

        mockMvc.perform(post("/customer/addressbook")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(shippingAddress))
                .andDo(print())
                .andExpect(status().is4xxClientError());


        mockMvc.perform(get("/cart/options/addresses/S")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("[]")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        final byte[] billingAddress = toJsonBytesAddressDetails("B", "GB-GB", "GB");

        mockMvc.perform(post("/customer/addressbook")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(billingAddress))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        mockMvc.perform(get("/cart/options/addresses/B")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("[]")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        mockMvc.perform(post("/cart/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("\"checkoutBlocked\":true")))
                .andExpect(content().string(StringContains.containsString("\"messageKey\":\"emptyCart\"")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        final byte[] addToCart = toJsonBytes(new XMLParamsRO(new HashMap<String, String>() {{
            put(ShoppingCartCommand.CMD_ADDTOCART, "BENDER-ua");
            put(ShoppingCartCommand.CMD_P_SUPPLIER, "WAREHOUSE_2");
        }}));

        mockMvc.perform(post("/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(addToCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("BENDER-ua")))
                .andExpect(content().string(StringContains.containsString("currencyCode\":\"EUR\"")))
                .andExpect(content().string(StringContains.containsString("deliveryAddressId\":null")))
                .andExpect(content().string(StringContains.containsString("billingAddressId\":null")))
                .andExpect(content().string(StringContains.containsString("separateBillingAddress\":false")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        mockMvc.perform(post("/cart/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("\"checkoutBlocked\":false")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        final AddressOptionRO shipAddressOptionRO = new AddressOptionRO();
        shipAddressOptionRO.setAddressId("10");
        final byte[] setShippingCart = toJsonBytes(shipAddressOptionRO);

        mockMvc.perform(post("/cart/options/addresses/S")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(setShippingCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("deliveryAddressId\":null")))
                .andExpect(content().string(StringContains.containsString("billingAddressId\":null")))
                .andExpect(content().string(StringContains.containsString("separateBillingAddress\":false")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        final AddressOptionRO billAddressOptionRO = new AddressOptionRO();
        billAddressOptionRO.setAddressId("10");
        final byte[] setBillingCart = toJsonBytes(billAddressOptionRO);

        mockMvc.perform(post("/cart/options/addresses/B")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(setBillingCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("deliveryAddressId\":null")))
                .andExpect(content().string(StringContains.containsString("billingAddressId\":null")))
                .andExpect(content().string(StringContains.containsString("separateBillingAddress\":false")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        final AddressOptionRO billAddressOptionROSame = new AddressOptionRO();
        billAddressOptionROSame.setAddressId("10");
        billAddressOptionROSame.setShippingSameAsBilling(true);
        final byte[] setBillingSameCart = toJsonBytes(billAddressOptionROSame);

        mockMvc.perform(post("/cart/options/addresses/B")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(setBillingSameCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("deliveryAddressId\":null")))
                .andExpect(content().string(StringContains.containsString("billingAddressId\":null")))
                .andExpect(content().string(StringContains.containsString("separateBillingAddress\":false")))
                .andExpect(content().string(StringContains.containsString("carrierSlaId\":{}")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        mockMvc.perform(get("/cart/options/shipping")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("\"carrierId\":1")))
                .andExpect(content().string(StringContains.containsString("\"carrierslaId\":4")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        final ShippingOptionRO shippingOptionRO = new ShippingOptionRO();
        shippingOptionRO.setShippingMethods(new ShippingOptionCarrierSelectionsRO());
        shippingOptionRO.getShippingMethods().setSelected(new ArrayList<>());
        final ShippingOptionCarrierSelectionRO shippingOptionCarrierSelectionRO = new ShippingOptionCarrierSelectionRO();
        shippingOptionCarrierSelectionRO.setCarrierSlaId("4");
        shippingOptionCarrierSelectionRO.setSupplier("WAREHOUSE_2");
        shippingOptionRO.getShippingMethods().getSelected().add(shippingOptionCarrierSelectionRO);


        final byte[] setCarrierCart = toJsonBytes(shippingOptionRO);

        mockMvc.perform(post("/cart/options/shipping")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(setCarrierCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("deliveryAddressId\":null")))
                .andExpect(content().string(StringContains.containsString("billingAddressId\":null")))
                .andExpect(content().string(StringContains.containsString("separateBillingAddress\":false")))
                .andExpect(content().string(StringContains.containsString("carrierSlaId\":{}")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        final byte[] addMessageToCart = toJsonBytes(new XMLParamsRO(new HashMap<String, String>() {{
            put(ShoppingCartCommand.CMD_SETORDERMSG, "My Message");
        }}));

        mockMvc.perform(post("/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(addMessageToCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("\"orderMessage\":\"My Message\"")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        mockMvc.perform(get("/cart/options/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("[]")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        final PaymentGatewayOptionRO pgOption = new PaymentGatewayOptionRO();
        pgOption.setPgLabel("courierPaymentGatewayLabel");

        final byte[] pgOptionBody = toJsonBytes(pgOption);

        mockMvc.perform(post("/cart/options/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(pgOptionBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("paymentGatewayLabel\":null")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        final OrderDeliveryOptionRO delOption = new OrderDeliveryOptionRO();

        final byte[] delOptionBody = toJsonBytes(delOption);

        mockMvc.perform(post("/order/preview")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .locale(LOCALE)
                    .header(X_CW_TOKEN, uuid)
                    .content(delOptionBody))
                    .andDo(print())
                    .andExpect(status().is4xxClientError());

        mockMvc.perform(post("/order/place")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .locale(LOCALE)
                    .header(X_CW_TOKEN, uuid))
                    .andDo(print())
                    .andExpect(status().is4xxClientError());


        mockMvc.perform(get("/customer/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .locale(LOCALE)
                    .header(X_CW_TOKEN, uuid))
                    .andDo(print())
                    .andExpect(status().is4xxClientError());

        mockMvc.perform(get("/customer/orders/2014-01-01")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().is4xxClientError());



    }



    @Test
    public void testGuestCheckoutJson() throws Exception {

        reindex();

        final String email = "bob.doe@guest-json.com";

        assertFalse("Customer not yet registered", hasEmails(email));
        
        final byte[] regBody = toJsonBytesGuestDetails(email);


        final MvcResult regResult =
                mockMvc.perform(post("/auth/guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .locale(LOCALE)
                        .content(regBody))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(content().string(StringContains.containsString("uuid")))
                        .andExpect(header().string(X_CW_TOKEN, CustomMatchers.isNotBlank()))
                        .andReturn();

        final String uuid = regResult.getResponse().getHeader(X_CW_TOKEN);


        final ShoppingCartState state = shoppingCartStateService.findByGuid(uuid);
        assertNotNull(uuid, state);
        assertNull(state.getCustomerLogin());

        final ShoppingCart cart = cartRepository.getShoppingCart(uuid);
        assertNotNull(uuid, cart);
        assertNull(cart.getCustomerLogin());


        mockMvc.perform(get("/auth/check")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("\"authenticated\":false")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        final byte[] shippingAddress = toJsonBytesAddressDetails("S", "UA-UA", "UA");

        final MvcResult shipAddress = mockMvc.perform(post("/customer/addressbook")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(shippingAddress))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("UA-UA")))
                .andExpect(header().string(X_CW_TOKEN, uuid))
                .andReturn();

        final Matcher matcherS = ADDRESS_ID_JSON.matcher(shipAddress.getResponse().getContentAsString());
        matcherS.find();
        final String shippingAddressId = matcherS.group(1);

        mockMvc.perform(get("/cart/options/addresses/S")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("UA")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        final byte[] billingAddress = toJsonBytesAddressDetails("B", "GB-GB", "GB");

        final MvcResult billAddress = mockMvc.perform(post("/customer/addressbook")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(billingAddress))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("GB")))
                .andExpect(header().string(X_CW_TOKEN, uuid))
                .andReturn();

        final Matcher matcherB = ADDRESS_ID_JSON.matcher(billAddress.getResponse().getContentAsString());
        matcherB.find();
        final String billingAddressId = matcherB.group(1);

        mockMvc.perform(get("/cart/options/addresses/B")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("GB")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        mockMvc.perform(post("/cart/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("\"checkoutBlocked\":true")))
                .andExpect(content().string(StringContains.containsString("\"messageKey\":\"emptyCart\"")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        final byte[] addToCart = toJsonBytes(new XMLParamsRO(new HashMap<String, String>() {{
            put(ShoppingCartCommand.CMD_ADDTOCART, "BENDER-ua");
            put(ShoppingCartCommand.CMD_P_SUPPLIER, "WAREHOUSE_2");
        }}));

        mockMvc.perform(post("/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(addToCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("BENDER-ua")))
                .andExpect(content().string(StringContains.containsString("currencyCode\":\"EUR\"")))
                .andExpect(content().string(StringContains.containsString("deliveryAddressId\":null")))
                .andExpect(content().string(StringContains.containsString("billingAddressId\":null")))
                .andExpect(content().string(StringContains.containsString("separateBillingAddress\":false")))
                .andExpect(content().string(StringContains.containsString("shopId\":10,")))
                .andExpect(content().string(StringContains.containsString("customerShopId\":10,")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        mockMvc.perform(post("/cart/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("\"checkoutBlocked\":false")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        final AddressOptionRO shipAddressOptionRO = new AddressOptionRO();
        shipAddressOptionRO.setAddressId(shippingAddressId);
        final byte[] setShippingCart = toJsonBytes(shipAddressOptionRO);

        mockMvc.perform(post("/cart/options/addresses/S")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(setShippingCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("deliveryAddressId\":" + shippingAddressId)))
                .andExpect(content().string(StringContains.containsString("billingAddressId\":null")))
                .andExpect(content().string(StringContains.containsString("separateBillingAddress\":true")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        final AddressOptionRO billAddressOptionRO = new AddressOptionRO();
        billAddressOptionRO.setAddressId(billingAddressId);
        final byte[] setBillingCart = toJsonBytes(billAddressOptionRO);

        mockMvc.perform(post("/cart/options/addresses/B")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(setBillingCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("deliveryAddressId\":" + shippingAddressId)))
                .andExpect(content().string(StringContains.containsString("billingAddressId\":" + billingAddressId)))
                .andExpect(content().string(StringContains.containsString("separateBillingAddress\":true")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        final AddressOptionRO billAddressOptionROSame = new AddressOptionRO();
        billAddressOptionROSame.setAddressId(billingAddressId);
        billAddressOptionROSame.setShippingSameAsBilling(true);
        final byte[] setBillingSameCart = toJsonBytes(billAddressOptionROSame);

        mockMvc.perform(post("/cart/options/addresses/B")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(setBillingSameCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("deliveryAddressId\":" + billingAddressId)))
                .andExpect(content().string(StringContains.containsString("billingAddressId\":" + billingAddressId)))
                .andExpect(content().string(StringContains.containsString("separateBillingAddress\":false")))
                .andExpect(content().string(StringContains.containsString("carrierSlaId\":{}")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        mockMvc.perform(get("/cart/options/shipping")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("\"carrierId\":1")))
                .andExpect(content().string(StringContains.containsString("\"carrierslaId\":4")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        final ShippingOptionRO shippingOptionRO = new ShippingOptionRO();
        shippingOptionRO.setShippingMethods(new ShippingOptionCarrierSelectionsRO());
        shippingOptionRO.getShippingMethods().setSelected(new ArrayList<>());
        final ShippingOptionCarrierSelectionRO shippingOptionCarrierSelectionRO = new ShippingOptionCarrierSelectionRO();
        shippingOptionCarrierSelectionRO.setCarrierSlaId("4");
        shippingOptionCarrierSelectionRO.setSupplier("WAREHOUSE_2");
        shippingOptionRO.getShippingMethods().getSelected().add(shippingOptionCarrierSelectionRO);

        final byte[] setCarrierCart = toJsonBytes(shippingOptionRO);

        mockMvc.perform(post("/cart/options/shipping")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(setCarrierCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("deliveryAddressId\":" + billingAddressId)))
                .andExpect(content().string(StringContains.containsString("billingAddressId\":" + billingAddressId)))
                .andExpect(content().string(StringContains.containsString("separateBillingAddress\":false")))
                .andExpect(content().string(StringContains.containsString("carrierSlaId\":{\"WAREHOUSE_2\":4}")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        final byte[] addMessageToCart = toJsonBytes(new XMLParamsRO(new HashMap<String, String>() {{
            put(ShoppingCartCommand.CMD_SETORDERMSG, "My Message");
        }}));

        mockMvc.perform(post("/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(addMessageToCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("\"orderMessage\":\"My Message\"")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        mockMvc.perform(get("/cart/options/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("testPaymentGatewayLabel")))
                .andExpect(content().string(StringContains.containsString("courierPaymentGatewayLabel")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        final PaymentGatewayOptionRO pgOption = new PaymentGatewayOptionRO();
        pgOption.setPgLabel("courierPaymentGatewayLabel");

        final byte[] pgOptionBody = toJsonBytes(pgOption);

        mockMvc.perform(post("/cart/options/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(pgOptionBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("courierPaymentGatewayLabel")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        final OrderDeliveryOptionRO delOption = new OrderDeliveryOptionRO();

        final byte[] delOptionBody = toJsonBytes(delOption);

        mockMvc.perform(post("/order/preview")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(delOptionBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("success\":true")))
                .andExpect(content().string(StringContains.containsString("customerorderId\":")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        final MvcResult placed = mockMvc.perform(post("/order/place")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("success\":true")))
                .andExpect(content().string(StringContains.containsString("customerorderId\":")))
                .andExpect(content().string(StringContains.containsString("shopId\":10,")))
                .andExpect(header().string(X_CW_TOKEN, CustomMatchers.isNotBlank()))
                .andReturn();

        final String tokenAfterPlacing = placed.getResponse().getHeader(X_CW_TOKEN);
        assertFalse(tokenAfterPlacing.equals(uuid));

        final Mail newOrder = getEmailBySubjectLike( "New order", email);
        assertNotNull("New order email", newOrder);
        assertTrue(newOrder.getHtmlVersion().contains("Dear <b>Bob Doe</b>!<br>"));
        assertTrue(newOrder.getHtmlVersion().contains("New order"));
        assertTrue(newOrder.getHtmlVersion().contains("BENDER-ua"));
        assertTrue(newOrder.getHtmlVersion().contains("<td align=\"right\"><b>109.99</b></td>"));
        assertTrue(newOrder.getHtmlVersion().contains("My Message"));
        assertTrue(newOrder.getHtmlVersion().contains("https://www.gadget.yescart.org/order?order="));


    }


    @Test
    public void testCheckoutXML() throws Exception {

        reindex();

        final String email = "bob.doe@checkout-xml.com";

        assertFalse("Customer not yet registered", hasEmails(email));
        
        final byte[] regBody = toJsonBytesRegistrationDetails(email);


        final MvcResult regResult =
                mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_XML)
                        .locale(LOCALE)
                        .content(regBody))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().string(StringContains.containsString("uuid")))
                    .andExpect(header().string(X_CW_TOKEN, CustomMatchers.isNotBlank()))
                    .andReturn();

        final String uuid = regResult.getResponse().getHeader(X_CW_TOKEN);

        final ShoppingCartState state = shoppingCartStateService.findByGuid(uuid);
        assertNotNull(uuid, state);
        assertEquals(email, state.getCustomerLogin());

        final ShoppingCart cart = cartRepository.getShoppingCart(uuid);
        assertNotNull(uuid, cart);
        assertEquals(email, cart.getCustomerLogin());

        mockMvc.perform(get("/auth/check")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(StringContains.containsString("<authenticated>true</authenticated>")))
            .andExpect(header().string(X_CW_TOKEN, uuid));

        final byte[] shippingAddress = toJsonBytesAddressDetails("S", "UA-UA", "UA");

        final MvcResult shipAddress = mockMvc.perform(post("/customer/addressbook")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_XML)
                    .locale(LOCALE)
                    .header(X_CW_TOKEN, uuid)
                    .content(shippingAddress))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("UA-UA")))
                .andExpect(header().string(X_CW_TOKEN, uuid))
                .andReturn();

        final Matcher matcherS = ADDRESS_ID_XML.matcher(shipAddress.getResponse().getContentAsString());
        matcherS.find();
        final String shippingAddressId = matcherS.group(1);


        mockMvc.perform(get("/cart/options/addresses/S")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_XML)
                    .locale(LOCALE)
                    .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("UA")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        final byte[] billingAddress = toJsonBytesAddressDetails("B", "GB-GB", "GB");

        final MvcResult billAddress = mockMvc.perform(post("/customer/addressbook")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_XML)
                    .locale(LOCALE)
                    .header(X_CW_TOKEN, uuid)
                    .content(billingAddress))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("GB")))
                .andExpect(header().string(X_CW_TOKEN, uuid))
                .andReturn();

        final Matcher matcherB = ADDRESS_ID_XML.matcher(billAddress.getResponse().getContentAsString());
        matcherB.find();
        final String billingAddressId = matcherB.group(1);

        mockMvc.perform(get("/cart/options/addresses/B")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_XML)
                    .locale(LOCALE)
                    .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("GB")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        mockMvc.perform(post("/cart/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("checkout-blocked=\"true\"")))
                .andExpect(content().string(StringContains.containsString("message-key=\"emptyCart\"")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        final byte[] addToCart = toJsonBytes(new XMLParamsRO(new HashMap<String, String>() {{
            put(ShoppingCartCommand.CMD_ADDTOCART, "BENDER-ua");
            put(ShoppingCartCommand.CMD_P_SUPPLIER, "WAREHOUSE_2");
        }}));

        mockMvc.perform(post("/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(addToCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("BENDER-ua")))
                .andExpect(content().string(StringContains.containsString("currency=\"EUR\"")))
                .andExpect(content().string(StringContains.containsString("separate-billing-address=\"false\"")))
                .andExpect(content().string(StringContains.containsString("shop-id=\"10\"")))
                .andExpect(content().string(StringContains.containsString("customer-shop-id=\"10\"")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        mockMvc.perform(post("/cart/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("checkout-blocked=\"false\"")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        final AddressOptionRO shipAddressOptionRO = new AddressOptionRO();
        shipAddressOptionRO.setAddressId(shippingAddressId);
        final byte[] setShippingCart = toJsonBytes(shipAddressOptionRO);

        mockMvc.perform(post("/cart/options/addresses/S")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(setShippingCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("delivery-address-id=\"" + shippingAddressId)))
                .andExpect(content().string(StringContains.containsString("separate-billing-address=\"true")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        final AddressOptionRO billAddressOptionRO = new AddressOptionRO();
        billAddressOptionRO.setAddressId(billingAddressId);
        final byte[] setBillingCart = toJsonBytes(billAddressOptionRO);

        mockMvc.perform(post("/cart/options/addresses/B")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_XML)
                    .locale(LOCALE)
                    .header(X_CW_TOKEN, uuid)
                    .content(setBillingCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("delivery-address-id=\"" + shippingAddressId)))
                .andExpect(content().string(StringContains.containsString("billing-address-id=\"" + billingAddressId)))
                .andExpect(content().string(StringContains.containsString("separate-billing-address=\"true")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        final AddressOptionRO billAddressOptionROSame = new AddressOptionRO();
        billAddressOptionROSame.setAddressId(billingAddressId);
        billAddressOptionROSame.setShippingSameAsBilling(true);
        final byte[] setBillingSameCart = toJsonBytes(billAddressOptionROSame);

        mockMvc.perform(post("/cart/options/addresses/B")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(setBillingSameCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("delivery-address-id=\"" + billingAddressId)))
                .andExpect(content().string(StringContains.containsString("billing-address-id=\"" + billingAddressId)))
                .andExpect(content().string(StringContains.containsString("separate-billing-address=\"false")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        mockMvc.perform(get("/cart/options/shipping")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_XML)
                    .locale(LOCALE)
                    .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("carrier-id=\"1")))
                .andExpect(content().string(StringContains.containsString("carriersla-id=\"4")))
                .andExpect(content().string(StringContains.containsString("supplier>WAREHOUSE_2")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        final ShippingOptionRO shippingOptionRO = new ShippingOptionRO();
        shippingOptionRO.setShippingMethods(new ShippingOptionCarrierSelectionsRO());
        shippingOptionRO.getShippingMethods().setSelected(new ArrayList<>());
        final ShippingOptionCarrierSelectionRO shippingOptionCarrierSelectionRO = new ShippingOptionCarrierSelectionRO();
        shippingOptionCarrierSelectionRO.setCarrierSlaId("4");
        shippingOptionCarrierSelectionRO.setSupplier("WAREHOUSE_2");
        shippingOptionRO.getShippingMethods().getSelected().add(shippingOptionCarrierSelectionRO);

        final byte[] setCarrierCart = toJsonBytes(shippingOptionRO);

        mockMvc.perform(post("/cart/options/shipping")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_XML)
                    .locale(LOCALE)
                    .header(X_CW_TOKEN, uuid)
                    .content(setCarrierCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("delivery-address-id=\"" + billingAddressId)))
                .andExpect(content().string(StringContains.containsString("billing-address-id=\"" + billingAddressId)))
                .andExpect(content().string(StringContains.containsString("separate-billing-address=\"false")))
                .andExpect(content().string(StringContains.containsString("carrier-sla-ids><selection supplier-code=\"WAREHOUSE_2\">4<")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        final byte[] addMessageToCart = toJsonBytes(new XMLParamsRO(new HashMap<String, String>() {{
            put(ShoppingCartCommand.CMD_SETORDERMSG, "My Message");
        }}));

        mockMvc.perform(post("/cart")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_XML)
                    .locale(LOCALE)
                    .header(X_CW_TOKEN, uuid)
                    .content(addMessageToCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("order-message>My Message</")))
                .andExpect(header().string(X_CW_TOKEN, uuid));



        mockMvc.perform(get("/cart/options/payment")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_XML)
                    .locale(LOCALE)
                    .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("testPaymentGatewayLabel")))
                .andExpect(content().string(StringContains.containsString("courierPaymentGatewayLabel")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        final PaymentGatewayOptionRO pgOption = new PaymentGatewayOptionRO();
        pgOption.setPgLabel("courierPaymentGatewayLabel");

        final byte[] pgOptionBody = toJsonBytes(pgOption);

        mockMvc.perform(post("/cart/options/payment")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_XML)
                    .locale(LOCALE)
                    .header(X_CW_TOKEN, uuid)
                .content(pgOptionBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("courierPaymentGatewayLabel")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        final OrderDeliveryOptionRO delOption = new OrderDeliveryOptionRO();

        final byte[] delOptionBody = toJsonBytes(delOption);

        mockMvc.perform(post("/order/preview")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_XML)
                    .locale(LOCALE)
                    .header(X_CW_TOKEN, uuid)
                .content(delOptionBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("success=\"true")))
                .andExpect(content().string(StringContains.containsString("customer-order-id=\"")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        final MvcResult placed = mockMvc.perform(post("/order/place")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_XML)
                    .locale(LOCALE)
                    .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("success=\"true")))
                .andExpect(content().string(StringContains.containsString("customer-order-id=\"")))
                .andExpect(header().string(X_CW_TOKEN, CustomMatchers.isNotBlank()))
                .andReturn();

        final String tokenAfterPlacing = placed.getResponse().getHeader(X_CW_TOKEN);
        assertFalse(tokenAfterPlacing.equals(uuid));

        final Mail newOrder = getEmailBySubjectLike( "New order", email);
        assertNotNull("New order email", newOrder);
        assertTrue(newOrder.getHtmlVersion().contains("Dear <b>Bob Doe</b>!<br>"));
        assertTrue(newOrder.getHtmlVersion().contains("New order"));
        assertTrue(newOrder.getHtmlVersion().contains("BENDER-ua"));
        assertTrue(newOrder.getHtmlVersion().contains("<td align=\"right\"><b>109.99</b></td>"));
        assertTrue(newOrder.getHtmlVersion().contains("My Message"));
        assertTrue(newOrder.getHtmlVersion().contains("https://www.gadget.yescart.org/order?order="));


        mockMvc.perform(get("/customer/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_XML)
                    .locale(LOCALE)
                    .header(X_CW_TOKEN, tokenAfterPlacing))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("customer-order-id=\"")))
                .andExpect(header().string(X_CW_TOKEN, tokenAfterPlacing));

        mockMvc.perform(get("/customer/orders/2014-01-01")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, tokenAfterPlacing))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("customer-order-id=\"")))
                .andExpect(content().string(StringContains.containsString("shop-id=\"10\"")))
                .andExpect(header().string(X_CW_TOKEN, tokenAfterPlacing));


    }



    @Test
    public void testSubCheckoutXML() throws Exception {

        reindex();

        final String email = "bob.doe@sub-checkout-xml.com";

        assertFalse("Customer not yet registered", hasEmails(email));
        
        final byte[] regBody = toJsonBytesSubRegistrationDetails(email);


        final MvcResult regResult =
                mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_XML)
                        .locale(LOCALE)
                        .content(regBody))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(content().string(StringContains.containsString("uuid")))
                        .andExpect(header().string(X_CW_TOKEN, CustomMatchers.isNotBlank()))
                        .andReturn();

        final String uuid = regResult.getResponse().getHeader(X_CW_TOKEN);

        final ShoppingCartState state = shoppingCartStateService.findByGuid(uuid);
        assertNotNull(uuid, state);
        assertEquals(email, state.getCustomerLogin());

        final ShoppingCart cart = cartRepository.getShoppingCart(uuid);
        assertNotNull(uuid, cart);
        assertEquals(email, cart.getCustomerLogin());

        mockMvc.perform(get("/auth/check")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("<authenticated>true</authenticated>")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        final byte[] shippingAddress = toJsonBytesAddressDetails("S", "UA-UA", "UA");

        final MvcResult shipAddress = mockMvc.perform(post("/customer/addressbook")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(shippingAddress))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("UA-UA")))
                .andExpect(header().string(X_CW_TOKEN, uuid))
                .andReturn();

        final Matcher matcherS = ADDRESS_ID_XML.matcher(shipAddress.getResponse().getContentAsString());
        matcherS.find();
        final String shippingAddressId = matcherS.group(1);


        mockMvc.perform(get("/cart/options/addresses/S")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("UA")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        final byte[] billingAddress = toJsonBytesAddressDetails("B", "GB-GB", "GB");

        final MvcResult billAddress = mockMvc.perform(post("/customer/addressbook")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(billingAddress))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("GB")))
                .andExpect(header().string(X_CW_TOKEN, uuid))
                .andReturn();

        final Matcher matcherB = ADDRESS_ID_XML.matcher(billAddress.getResponse().getContentAsString());
        matcherB.find();
        final String billingAddressId = matcherB.group(1);

        mockMvc.perform(get("/cart/options/addresses/B")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("GB")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        mockMvc.perform(post("/cart/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("checkout-blocked=\"true\"")))
                .andExpect(content().string(StringContains.containsString("message-key=\"emptyCart\"")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        final byte[] addToCart = toJsonBytes(new XMLParamsRO(new HashMap<String, String>() {{
            put(ShoppingCartCommand.CMD_ADDTOCART, "BENDER-ua");
            put(ShoppingCartCommand.CMD_P_SUPPLIER, "WAREHOUSE_2");
        }}));

        mockMvc.perform(post("/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(addToCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("BENDER-ua")))
                .andExpect(content().string(StringContains.containsString("currency=\"EUR\"")))
                .andExpect(content().string(StringContains.containsString("shop-id=\"10\"")))
                .andExpect(content().string(StringContains.containsString("customer-shop-id=\"1010\"")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        mockMvc.perform(post("/cart/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("checkout-blocked=\"false\"")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        final AddressOptionRO shipAddressOptionRO = new AddressOptionRO();
        shipAddressOptionRO.setAddressId(shippingAddressId);
        final byte[] setShippingCart = toJsonBytes(shipAddressOptionRO);

        mockMvc.perform(post("/cart/options/addresses/S")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(setShippingCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("delivery-address-id=\"" + shippingAddressId)))
                .andExpect(content().string(StringContains.containsString("separate-billing-address=\"true")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        final AddressOptionRO billAddressOptionRO = new AddressOptionRO();
        billAddressOptionRO.setAddressId(billingAddressId);
        final byte[] setBillingCart = toJsonBytes(billAddressOptionRO);

        mockMvc.perform(post("/cart/options/addresses/B")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(setBillingCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("delivery-address-id=\"" + shippingAddressId)))
                .andExpect(content().string(StringContains.containsString("billing-address-id=\"" + billingAddressId)))
                .andExpect(content().string(StringContains.containsString("separate-billing-address=\"true")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        final AddressOptionRO billAddressOptionROSame = new AddressOptionRO();
        billAddressOptionROSame.setAddressId(billingAddressId);
        billAddressOptionROSame.setShippingSameAsBilling(true);
        final byte[] setBillingSameCart = toJsonBytes(billAddressOptionROSame);

        mockMvc.perform(post("/cart/options/addresses/B")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(setBillingSameCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("delivery-address-id=\"" + billingAddressId)))
                .andExpect(content().string(StringContains.containsString("billing-address-id=\"" + billingAddressId)))
                .andExpect(content().string(StringContains.containsString("separate-billing-address=\"false")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        mockMvc.perform(get("/cart/options/shipping")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("carrier-id=\"1")))
                .andExpect(content().string(StringContains.containsString("carriersla-id=\"4")))
                .andExpect(content().string(StringContains.containsString("supplier>WAREHOUSE_2")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        final ShippingOptionRO shippingOptionRO = new ShippingOptionRO();
        shippingOptionRO.setShippingMethods(new ShippingOptionCarrierSelectionsRO());
        shippingOptionRO.getShippingMethods().setSelected(new ArrayList<>());
        final ShippingOptionCarrierSelectionRO shippingOptionCarrierSelectionRO = new ShippingOptionCarrierSelectionRO();
        shippingOptionCarrierSelectionRO.setCarrierSlaId("4");
        shippingOptionCarrierSelectionRO.setSupplier("WAREHOUSE_2");
        shippingOptionRO.getShippingMethods().getSelected().add(shippingOptionCarrierSelectionRO);

        final byte[] setCarrierCart = toJsonBytes(shippingOptionRO);

        mockMvc.perform(post("/cart/options/shipping")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(setCarrierCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("delivery-address-id=\"" + billingAddressId)))
                .andExpect(content().string(StringContains.containsString("billing-address-id=\"" + billingAddressId)))
                .andExpect(content().string(StringContains.containsString("separate-billing-address=\"false")))
                .andExpect(content().string(StringContains.containsString("carrier-sla-ids><selection supplier-code=\"WAREHOUSE_2\">4<")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        final byte[] addMessageToCart = toJsonBytes(new XMLParamsRO(new HashMap<String, String>() {{
            put(ShoppingCartCommand.CMD_SETORDERMSG, "My Message");
        }}));

        mockMvc.perform(post("/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(addMessageToCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("order-message>My Message</")))
                .andExpect(header().string(X_CW_TOKEN, uuid));



        mockMvc.perform(get("/cart/options/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("testPaymentGatewayLabel")))
                .andExpect(content().string(StringContains.containsString("courierPaymentGatewayLabel")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        final PaymentGatewayOptionRO pgOption = new PaymentGatewayOptionRO();
        pgOption.setPgLabel("courierPaymentGatewayLabel");

        final byte[] pgOptionBody = toJsonBytes(pgOption);

        mockMvc.perform(post("/cart/options/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(pgOptionBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("courierPaymentGatewayLabel")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        final OrderDeliveryOptionRO delOption = new OrderDeliveryOptionRO();

        final byte[] delOptionBody = toJsonBytes(delOption);

        mockMvc.perform(post("/order/preview")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(delOptionBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("success=\"true")))
                .andExpect(content().string(StringContains.containsString("customer-order-id=\"")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        final MvcResult placed = mockMvc.perform(post("/order/place")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("success=\"true")))
                .andExpect(content().string(StringContains.containsString("customer-order-id=\"")))
                .andExpect(header().string(X_CW_TOKEN, CustomMatchers.isNotBlank()))
                .andReturn();

        final String tokenAfterPlacing = placed.getResponse().getHeader(X_CW_TOKEN);
        assertFalse(tokenAfterPlacing.equals(uuid));

        final Mail newOrder = getEmailBySubjectLike( "New order", email);
        assertNotNull("New order email", newOrder);
        assertTrue(newOrder.getHtmlVersion().contains("Dear <b>Bob Doe</b>!<br>"));
        assertTrue(newOrder.getHtmlVersion().contains("New order"));
        assertTrue(newOrder.getHtmlVersion().contains("BENDER-ua"));
        assertTrue(newOrder.getHtmlVersion().contains("<td align=\"right\"><b>109.99</b></td>"));
        assertTrue(newOrder.getHtmlVersion().contains("My Message"));
        assertTrue(newOrder.getHtmlVersion().contains("https://www.gadget.yescart.org/order?order="));


        mockMvc.perform(get("/customer/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, tokenAfterPlacing))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("customer-order-id=\"")))
                .andExpect(header().string(X_CW_TOKEN, tokenAfterPlacing));

        mockMvc.perform(get("/customer/orders/2014-01-01")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, tokenAfterPlacing))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("customer-order-id=\"")))
                .andExpect(content().string(StringContains.containsString("shop-id=\"1010\"")))
                .andExpect(header().string(X_CW_TOKEN, tokenAfterPlacing));


    }


    @Test
    public void testCheckoutNoLoginXML() throws Exception {

        reindex();

        final MvcResult authResult = mockMvc.perform(get("/auth/check")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("<authenticated>false</authenticated>")))
                .andReturn();

        final String uuid = authResult.getResponse().getHeader(X_CW_TOKEN);

        final ShoppingCartState state = shoppingCartStateService.findByGuid(uuid);
        assertNotNull(uuid, state);
        assertNull(state.getCustomerLogin());

        final ShoppingCart cart = cartRepository.getShoppingCart(uuid);
        assertNotNull(uuid, cart);
        assertNull(cart.getCustomerLogin());

        final byte[] shippingAddress = toJsonBytesAddressDetails("S", "UA-UA", "UA");

        mockMvc.perform(post("/customer/addressbook")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(shippingAddress))
                .andDo(print())
                .andExpect(status().is4xxClientError());


        mockMvc.perform(get("/cart/options/addresses/S")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("<addresses/>")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        final byte[] billingAddress = toJsonBytesAddressDetails("B", "GB-GB", "GB");

        mockMvc.perform(post("/customer/addressbook")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(billingAddress))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        mockMvc.perform(get("/cart/options/addresses/B")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("<addresses/>")))
                .andExpect(header().string(X_CW_TOKEN, uuid));



        final byte[] addToCart = toJsonBytes(new XMLParamsRO(new HashMap<String, String>() {{
            put(ShoppingCartCommand.CMD_ADDTOCART, "BENDER-ua");
            put(ShoppingCartCommand.CMD_P_SUPPLIER, "WAREHOUSE_2");
        }}));

        mockMvc.perform(post("/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(addToCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("BENDER-ua")))
                .andExpect(content().string(StringContains.containsString("currency=\"EUR\"")))
                .andExpect(content().string(StringContains.containsString("separate-billing-address=\"false\"")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        final AddressOptionRO shipAddressOptionRO = new AddressOptionRO();
        shipAddressOptionRO.setAddressId("10");
        final byte[] setShippingCart = toJsonBytes(shipAddressOptionRO);

        mockMvc.perform(post("/cart/options/addresses/S")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(setShippingCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("<order-info billing-address-not-required=\"false\" delivery-address-not-required=\"false\" multiple-delivery=\"false\" separate-billing-address=\"false\" separate-billing-address-force=\"false\"><carrier-sla-ids/><order-details")))
                .andExpect(content().string(StringContains.containsString("<multiple-delivery-available/>")))
                .andExpect(content().string(StringContains.containsString("<entry key=\"blockCheckout\">false</entry>")))
                .andExpect(content().string(StringContains.containsString("<entry key=\"customerType\">B2G</entry>")))
                .andExpect(content().string(StringContains.containsString("<entry key=\"b2bRequireApprove\">false</entry>")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        final AddressOptionRO billAddressOptionRO = new AddressOptionRO();
        billAddressOptionRO.setAddressId("10");
        final byte[] setBillingCart = toJsonBytes(billAddressOptionRO);

        mockMvc.perform(post("/cart/options/addresses/B")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(setBillingCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("<order-info billing-address-not-required=\"false\" delivery-address-not-required=\"false\" multiple-delivery=\"false\" separate-billing-address=\"false\" separate-billing-address-force=\"false\"><carrier-sla-ids/><order-details")))
                .andExpect(content().string(StringContains.containsString("<multiple-delivery-available/>")))
                .andExpect(content().string(StringContains.containsString("<entry key=\"blockCheckout\">false</entry>")))
                .andExpect(content().string(StringContains.containsString("<entry key=\"customerType\">B2G</entry>")))
                .andExpect(content().string(StringContains.containsString("<entry key=\"b2bRequireApprove\">false</entry>")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        final AddressOptionRO billAddressOptionROSame = new AddressOptionRO();
        billAddressOptionROSame.setAddressId("10");
        billAddressOptionROSame.setShippingSameAsBilling(true);
        final byte[] setBillingSameCart = toJsonBytes(billAddressOptionROSame);

        mockMvc.perform(post("/cart/options/addresses/B")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(setBillingSameCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("<order-info billing-address-not-required=\"false\" delivery-address-not-required=\"false\" multiple-delivery=\"false\" separate-billing-address=\"false\" separate-billing-address-force=\"false\"><carrier-sla-ids/><order-details")))
                .andExpect(content().string(StringContains.containsString("<multiple-delivery-available/>")))
                .andExpect(content().string(StringContains.containsString("<entry key=\"blockCheckout\">false</entry>")))
                .andExpect(content().string(StringContains.containsString("<entry key=\"customerType\">B2G</entry>")))
                .andExpect(content().string(StringContains.containsString("<entry key=\"b2bRequireApprove\">false</entry>")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        mockMvc.perform(get("/cart/options/shipping")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("carrier-id=\"1")))
                .andExpect(content().string(StringContains.containsString("carriersla-id=\"4")))
                .andExpect(content().string(StringContains.containsString("supplier>WAREHOUSE_2")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        final ShippingOptionRO shippingOptionRO = new ShippingOptionRO();
        shippingOptionRO.setShippingMethods(new ShippingOptionCarrierSelectionsRO());
        shippingOptionRO.getShippingMethods().setSelected(new ArrayList<>());
        final ShippingOptionCarrierSelectionRO shippingOptionCarrierSelectionRO = new ShippingOptionCarrierSelectionRO();
        shippingOptionCarrierSelectionRO.setCarrierSlaId("4");
        shippingOptionCarrierSelectionRO.setSupplier("WAREHOUSE_2");
        shippingOptionRO.getShippingMethods().getSelected().add(shippingOptionCarrierSelectionRO);

        final byte[] setCarrierCart = toJsonBytes(shippingOptionRO);

        mockMvc.perform(post("/cart/options/shipping")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(setCarrierCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("<order-info billing-address-not-required=\"false\" delivery-address-not-required=\"false\" multiple-delivery=\"false\" separate-billing-address=\"false\" separate-billing-address-force=\"false\"><carrier-sla-ids/><order-details")))
                .andExpect(content().string(StringContains.containsString("<multiple-delivery-available><multi-delivery supplier-code=\"WAREHOUSE_2\">false</multi-delivery></multiple-delivery-available>")))
                .andExpect(content().string(StringContains.containsString("<entry key=\"blockCheckout\">false</entry>")))
                .andExpect(content().string(StringContains.containsString("<entry key=\"customerType\">B2G</entry>")))
                .andExpect(content().string(StringContains.containsString("<entry key=\"b2bRequireApprove\">false</entry>")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        final byte[] addMessageToCart = toJsonBytes(new XMLParamsRO(new HashMap<String, String>() {{
            put(ShoppingCartCommand.CMD_SETORDERMSG, "My Message");
        }}));

        mockMvc.perform(post("/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(addMessageToCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("order-message>My Message</")))
                .andExpect(header().string(X_CW_TOKEN, uuid));



        mockMvc.perform(get("/cart/options/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("<payment-gateways/>")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        final PaymentGatewayOptionRO pgOption = new PaymentGatewayOptionRO();
        pgOption.setPgLabel("courierPaymentGatewayLabel");

        final byte[] pgOptionBody = toJsonBytes(pgOption);

        mockMvc.perform(post("/cart/options/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(pgOptionBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("<order-info billing-address-not-required=\"false\" delivery-address-not-required=\"false\" multiple-delivery=\"false\" separate-billing-address=\"false\" separate-billing-address-force=\"false\"><carrier-sla-ids/><order-details")))
                .andExpect(content().string(StringContains.containsString("<multiple-delivery-available><multi-delivery supplier-code=\"WAREHOUSE_2\">false</multi-delivery></multiple-delivery-available>")))
                .andExpect(content().string(StringContains.containsString("<entry key=\"blockCheckout\">false</entry>")))
                .andExpect(content().string(StringContains.containsString("<entry key=\"customerType\">B2G</entry>")))
                .andExpect(content().string(StringContains.containsString("<entry key=\"b2bRequireApprove\">false</entry>")))
                .andExpect(content().string(StringContains.containsString("<order-message>My Message</order-message></order-info>")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        final OrderDeliveryOptionRO delOption = new OrderDeliveryOptionRO();

        final byte[] delOptionBody = toJsonBytes(delOption);

        mockMvc.perform(post("/order/preview")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(delOptionBody))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        mockMvc.perform(post("/order/place")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        mockMvc.perform(get("/customer/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        mockMvc.perform(get("/customer/orders/2014-01-01")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().is4xxClientError());

    }


    @Test
    public void testGuestCheckoutXML() throws Exception {

        reindex();

        final String email = "bob.doe@guest-xml.com";

        assertFalse("Customer not yet registered", hasEmails(email));
        
        final byte[] regBody = toJsonBytesGuestDetails(email);


        final MvcResult regResult =
                mockMvc.perform(post("/auth/guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_XML)
                        .locale(LOCALE)
                        .content(regBody))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(content().string(StringContains.containsString("uuid")))
                        .andExpect(header().string(X_CW_TOKEN, CustomMatchers.isNotBlank()))
                        .andReturn();

        final String uuid = regResult.getResponse().getHeader(X_CW_TOKEN);

        final ShoppingCartState state = shoppingCartStateService.findByGuid(uuid);
        assertNotNull(uuid, state);
        assertNull(state.getCustomerLogin());

        final ShoppingCart cart = cartRepository.getShoppingCart(uuid);
        assertNotNull(uuid, cart);
        assertNull(cart.getCustomerLogin());


        mockMvc.perform(get("/auth/check")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("<authenticated>false</authenticated>")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        final byte[] shippingAddress = toJsonBytesAddressDetails("S", "UA-UA", "UA");

        final MvcResult shipAddress = mockMvc.perform(post("/customer/addressbook")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(shippingAddress))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("UA-UA")))
                .andExpect(header().string(X_CW_TOKEN, uuid))
                .andReturn();

        final Matcher matcherS = ADDRESS_ID_XML.matcher(shipAddress.getResponse().getContentAsString());
        matcherS.find();
        final String shippingAddressId = matcherS.group(1);


        mockMvc.perform(get("/cart/options/addresses/S")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("UA")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        final byte[] billingAddress = toJsonBytesAddressDetails("B", "GB-GB", "GB");

        final MvcResult billAddress = mockMvc.perform(post("/customer/addressbook")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(billingAddress))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("GB")))
                .andExpect(header().string(X_CW_TOKEN, uuid))
                .andReturn();

        final Matcher matcherB = ADDRESS_ID_XML.matcher(billAddress.getResponse().getContentAsString());
        matcherB.find();
        final String billingAddressId = matcherB.group(1);

        mockMvc.perform(get("/cart/options/addresses/B")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("GB")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        mockMvc.perform(post("/cart/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("checkout-blocked=\"true\"")))
                .andExpect(content().string(StringContains.containsString("message-key=\"emptyCart\"")))
                .andExpect(header().string(X_CW_TOKEN, uuid));



        final byte[] addToCart = toJsonBytes(new XMLParamsRO(new HashMap<String, String>() {{
            put(ShoppingCartCommand.CMD_ADDTOCART, "BENDER-ua");
            put(ShoppingCartCommand.CMD_P_SUPPLIER, "WAREHOUSE_2");
        }}));

        mockMvc.perform(post("/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(addToCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("BENDER-ua")))
                .andExpect(content().string(StringContains.containsString("currency=\"EUR\"")))
                .andExpect(content().string(StringContains.containsString("separate-billing-address=\"false\"")))
                .andExpect(content().string(StringContains.containsString("shop-id=\"10\"")))
                .andExpect(content().string(StringContains.containsString("customer-shop-id=\"10\"")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        mockMvc.perform(post("/cart/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("checkout-blocked=\"false\"")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        final AddressOptionRO shipAddressOptionRO = new AddressOptionRO();
        shipAddressOptionRO.setAddressId(shippingAddressId);
        final byte[] setShippingCart = toJsonBytes(shipAddressOptionRO);

        mockMvc.perform(post("/cart/options/addresses/S")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(setShippingCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("delivery-address-id=\"" + shippingAddressId)))
                .andExpect(content().string(StringContains.containsString("separate-billing-address=\"true")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        final AddressOptionRO billAddressOptionRO = new AddressOptionRO();
        billAddressOptionRO.setAddressId(billingAddressId);
        final byte[] setBillingCart = toJsonBytes(billAddressOptionRO);

        mockMvc.perform(post("/cart/options/addresses/B")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(setBillingCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("delivery-address-id=\"" + shippingAddressId)))
                .andExpect(content().string(StringContains.containsString("billing-address-id=\"" + billingAddressId)))
                .andExpect(content().string(StringContains.containsString("separate-billing-address=\"true")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        final AddressOptionRO billAddressOptionROSame = new AddressOptionRO();
        billAddressOptionROSame.setAddressId(billingAddressId);
        billAddressOptionROSame.setShippingSameAsBilling(true);
        final byte[] setBillingSameCart = toJsonBytes(billAddressOptionROSame);

        mockMvc.perform(post("/cart/options/addresses/B")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(setBillingSameCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("delivery-address-id=\"" + billingAddressId)))
                .andExpect(content().string(StringContains.containsString("billing-address-id=\"" + billingAddressId)))
                .andExpect(content().string(StringContains.containsString("separate-billing-address=\"false")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        mockMvc.perform(get("/cart/options/shipping")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("carrier-id=\"1")))
                .andExpect(content().string(StringContains.containsString("carriersla-id=\"4")))
                .andExpect(content().string(StringContains.containsString("supplier>WAREHOUSE_2")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        final ShippingOptionRO shippingOptionRO = new ShippingOptionRO();
        shippingOptionRO.setShippingMethods(new ShippingOptionCarrierSelectionsRO());
        shippingOptionRO.getShippingMethods().setSelected(new ArrayList<>());
        final ShippingOptionCarrierSelectionRO shippingOptionCarrierSelectionRO = new ShippingOptionCarrierSelectionRO();
        shippingOptionCarrierSelectionRO.setCarrierSlaId("4");
        shippingOptionCarrierSelectionRO.setSupplier("WAREHOUSE_2");
        shippingOptionRO.getShippingMethods().getSelected().add(shippingOptionCarrierSelectionRO);

        final byte[] setCarrierCart = toJsonBytes(shippingOptionRO);

        mockMvc.perform(post("/cart/options/shipping")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(setCarrierCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("delivery-address-id=\"" + billingAddressId)))
                .andExpect(content().string(StringContains.containsString("billing-address-id=\"" + billingAddressId)))
                .andExpect(content().string(StringContains.containsString("separate-billing-address=\"false")))
                .andExpect(content().string(StringContains.containsString("carrier-sla-ids><selection supplier-code=\"WAREHOUSE_2\">4<")))
                .andExpect(header().string(X_CW_TOKEN, uuid));


        final byte[] addMessageToCart = toJsonBytes(new XMLParamsRO(new HashMap<String, String>() {{
            put(ShoppingCartCommand.CMD_SETORDERMSG, "My Message");
        }}));

        mockMvc.perform(post("/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(addMessageToCart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("order-message>My Message</")))
                .andExpect(header().string(X_CW_TOKEN, uuid));



        mockMvc.perform(get("/cart/options/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("testPaymentGatewayLabel")))
                .andExpect(content().string(StringContains.containsString("courierPaymentGatewayLabel")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        final PaymentGatewayOptionRO pgOption = new PaymentGatewayOptionRO();
        pgOption.setPgLabel("courierPaymentGatewayLabel");

        final byte[] pgOptionBody = toJsonBytes(pgOption);

        mockMvc.perform(post("/cart/options/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(pgOptionBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("courierPaymentGatewayLabel")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        final OrderDeliveryOptionRO delOption = new OrderDeliveryOptionRO();

        final byte[] delOptionBody = toJsonBytes(delOption);

        mockMvc.perform(post("/order/preview")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid)
                .content(delOptionBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("success=\"true")))
                .andExpect(content().string(StringContains.containsString("customer-order-id=\"")))
                .andExpect(header().string(X_CW_TOKEN, uuid));

        final MvcResult placed = mockMvc.perform(post("/order/place")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .locale(LOCALE)
                .header(X_CW_TOKEN, uuid))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("success=\"true")))
                .andExpect(content().string(StringContains.containsString("customer-order-id=\"")))
                .andExpect(content().string(StringContains.containsString("shop-id=\"10\"")))
                .andExpect(header().string(X_CW_TOKEN, CustomMatchers.isNotBlank()))
                .andReturn();

        final String tokenAfterPlacing = placed.getResponse().getHeader(X_CW_TOKEN);
        assertFalse(tokenAfterPlacing.equals(uuid));
        
        final Mail newOrder = getEmailBySubjectLike( "New order", email);
        assertNotNull("New order email", newOrder);
        assertTrue(newOrder.getHtmlVersion().contains("Dear <b>Bob Doe</b>!<br>"));
        assertTrue(newOrder.getHtmlVersion().contains("New order"));
        assertTrue(newOrder.getHtmlVersion().contains("BENDER-ua"));
        assertTrue(newOrder.getHtmlVersion().contains("<td align=\"right\"><b>109.99</b></td>"));
        assertTrue(newOrder.getHtmlVersion().contains("My Message"));
        assertTrue(newOrder.getHtmlVersion().contains("https://www.gadget.yescart.org/order?order="));



    }


}
