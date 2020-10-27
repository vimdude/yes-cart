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

package org.yes.cart.domain.vo;

import com.inspiresoftware.lib.dto.geda.annotations.Dto;
import com.inspiresoftware.lib.dto.geda.annotations.DtoField;

import java.util.ArrayList;
import java.util.List;

@Dto
public class VoCartShoppingContext {

    @DtoField(readOnly = true)
    private long shopId;
    @DtoField(readOnly = true)
    private String shopCode;
    @DtoField(readOnly = true)
    private long customerShopId;
    @DtoField(readOnly = true)
    private String customerShopCode;
    @DtoField(readOnly = true)
    private String countryCode;
    @DtoField(readOnly = true)
    private String stateCode;
    @DtoField(readOnly = true)
    private List<String> customerShops;

    @DtoField(readOnly = true)
    private String customerLogin;
    @DtoField(readOnly = true)
    private String customerName;

    @DtoField(readOnly = true)
    private String managerLogin;
    @DtoField(readOnly = true)
    private String managerName;

    @DtoField(readOnly = true)
    private boolean taxInfoChangeViewEnabled;
    @DtoField(readOnly = true)
    private boolean taxInfoEnabled;
    @DtoField(readOnly = true)
    private boolean taxInfoUseNet;
    @DtoField(readOnly = true)
    private boolean taxInfoShowAmount;

    @DtoField(readOnly = true)
    private boolean hidePrices;

    @DtoField(readOnly = true)
    private List<String> latestViewedSkus;
    @DtoField(readOnly = true)
    private List<String> latestViewedCategories;
    @DtoField(readOnly = true)
    private String resolvedIp;


    public String getResolvedIp() {
        return resolvedIp;
    }

    public void setResolvedIp(final String resolvedIp) {
        this.resolvedIp = resolvedIp;
    }

    
    public List<String> getLatestViewedSkus() {
        return latestViewedSkus;
    }

    public void setLatestViewedSkus(final List<String> latestViewedSkus) {
        this.latestViewedSkus = latestViewedSkus != null ? new ArrayList<>(latestViewedSkus) : new ArrayList<>(0);
    }

    
    public List<String> getLatestViewedCategories() {
        return latestViewedCategories;
    }

    public void setLatestViewedCategories(final List<String> latestViewedCategories) {
        this.latestViewedCategories = latestViewedCategories != null ? new ArrayList<>(latestViewedCategories) : new ArrayList<>(0);
    }


    public String getCustomerLogin() {
        return customerLogin;
    }

    public void setCustomerLogin(final String customerLogin) {
        this.customerLogin = customerLogin;
    }


    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(final String customerName) {
        this.customerName = customerName;
    }


    public String getManagerLogin() {
        return managerLogin;
    }

    public void setManagerLogin(final String managerLogin) {
        this.managerLogin = managerLogin;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(final String managerName) {
        this.managerName = managerName;
    }

    public List<String> getCustomerShops() {
        return customerShops;
    }

    public void setCustomerShops(final List<String> shops) {
        this.customerShops = shops != null ? new ArrayList<>(shops) : new ArrayList<>(0);
    }

    
    public long getShopId() {
        return shopId;
    }

    public void setShopId(final long shopId) {
        this.shopId = shopId;
    }

    
    public String getShopCode() {
        return shopCode;
    }

    public void setShopCode(final String shopCode) {
        this.shopCode = shopCode;
    }

    
    public long getCustomerShopId() {
        return customerShopId;
    }

    public void setCustomerShopId(final long customerShopId) {
        this.customerShopId = customerShopId;
    }

    
    public String getCustomerShopCode() {
        return customerShopCode;
    }

    public void setCustomerShopCode(final String customerShopCode) {
        this.customerShopCode = customerShopCode;
    }

    
    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(final String countryCode) {
        this.countryCode = countryCode;
    }

    
    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(final String stateCode) {
        this.stateCode = stateCode;
    }

    
    public boolean isTaxInfoChangeViewEnabled() {
        return taxInfoChangeViewEnabled;
    }

    public void setTaxInfoChangeViewEnabled(final boolean taxInfoChangeViewEnabled) {
        this.taxInfoChangeViewEnabled = taxInfoChangeViewEnabled;
    }

    
    public boolean isTaxInfoEnabled() {
        return taxInfoEnabled;
    }

    public void setTaxInfoEnabled(final boolean taxInfoEnabled) {
        this.taxInfoEnabled = taxInfoEnabled;
    }

    
    public boolean isTaxInfoUseNet() {
        return taxInfoUseNet;
    }

    public void setTaxInfoUseNet(final boolean taxInfoUseNet) {
        this.taxInfoUseNet = taxInfoUseNet;
    }

    
    public boolean isTaxInfoShowAmount() {
        return taxInfoShowAmount;
    }

    public void setTaxInfoShowAmount(final boolean taxInfoShowAmount) {
        this.taxInfoShowAmount = taxInfoShowAmount;
    }

    
    public boolean isHidePrices() {
        return hidePrices;
    }

    public void setHidePrices(final boolean hidePrices) {
        this.hidePrices = hidePrices;
    }
}
