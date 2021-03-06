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

package org.yes.cart.domain.dto.impl;

import com.inspiresoftware.lib.dto.geda.annotations.Dto;
import com.inspiresoftware.lib.dto.geda.annotations.DtoField;
import com.inspiresoftware.lib.dto.geda.annotations.DtoVirtualField;
import org.yes.cart.domain.dto.SkuPriceDTO;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Default implementation.
 * <p/>
 * User: dogma
 * Date: Jan 22, 2011
 * Time: 11:17:34 PM
 */
@Dto
public class SkuPriceDTOImpl implements SkuPriceDTO {

    private static final long serialVersionUID = 20100620L;

    @DtoField(value = "skuPriceId", readOnly = true)
    private long skuPriceId;

    @DtoField(value = "regularPrice")
    private BigDecimal regularPrice;

    @DtoField(value = "minimalPrice")
    private BigDecimal minimalPrice;

    @DtoField(value = "salePrice")
    private BigDecimal salePrice;

    @DtoField(value = "salefrom")
    private LocalDateTime salefrom;

    @DtoField(value = "saleto")
    private LocalDateTime saleto;

    @DtoField(
            value = "shop",
            converter = "shopId2Shop",
            entityBeanKeys = "org.yes.cart.domain.entity.Shop"
    )
    private long shopId;

    @DtoField(value = "quantity")
    private BigDecimal quantity;

    @DtoField(value = "priceUponRequest")
    private boolean priceUponRequest;

    @DtoField(value = "priceOnOffer")
    private boolean priceOnOffer;

    @DtoField(value = "currency")
    private String currency;

    @DtoField(value = "skuCode", readOnly = true)
    private String skuCode;

    @DtoVirtualField(converter = "priceSkuCodeToName", readOnly = true)
    private String skuName;

    @DtoField(value = "tag")
    private String tag;

    @DtoField(value = "pricingPolicy")
    private String pricingPolicy;

    @DtoField(value = "supplier")
    private String supplier;

    @DtoField(value = "ref")
    private String ref;

    @DtoField(value = "autoGenerated", readOnly = true)
    private boolean autoGenerated;


    @DtoField(readOnly = true)
    private Instant createdTimestamp;
    @DtoField(readOnly = true)
    private Instant updatedTimestamp;
    @DtoField(readOnly = true)
    private String createdBy;
    @DtoField(readOnly = true)
    private String updatedBy;

    /** {@inheritDoc}*/
    @Override
    public String getSkuName() {
        return skuName;
    }

    /** {@inheritDoc}*/
    @Override
    public void setSkuName(final String skuName) {
        this.skuName = skuName;
    }

    /** {@inheritDoc}*/
    @Override
    public String getSkuCode() {
        return skuCode;
    }

    /** {@inheritDoc}*/
    @Override
    public void setSkuCode(final String code) {
        this.skuCode = code;
    }

    /** {@inheritDoc}*/
    @Override
    public long getShopId() {
        return shopId;
    }

    /** {@inheritDoc}*/
    @Override
    public void setShopId(final long shopId) {
        this.shopId = shopId;
    }

    /** {@inheritDoc}*/
    @Override
    public String getCurrency() {
        return currency;
    }

    /** {@inheritDoc}*/
    @Override
    public void setCurrency(final String currency) {
        this.currency = currency;
    }

    /** {@inheritDoc}*/
    @Override
    public BigDecimal getSalePrice() {
        return salePrice;
    }

    /** {@inheritDoc}*/
    @Override
    public void setSalePrice(final BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    /** {@inheritDoc}*/
    @Override
    public long getSkuPriceId() {
        return skuPriceId;
    }

     /** {@inheritDoc}*/
    @Override
    public long getId() {
        return skuPriceId;
    }

    /** {@inheritDoc}*/
    @Override
    public void setSkuPriceId(final long skuPriceId) {
        this.skuPriceId = skuPriceId;
    }

    /** {@inheritDoc}*/
    @Override
    public BigDecimal getRegularPrice() {
        return regularPrice;
    }

    /** {@inheritDoc}*/
    @Override
    public void setRegularPrice(final BigDecimal regularPrice) {
        this.regularPrice = regularPrice;
    }

    /** {@inheritDoc}*/
    @Override
    public BigDecimal getMinimalPrice() {
        return minimalPrice;
    }

    /** {@inheritDoc}*/
    @Override
    public void setMinimalPrice(final BigDecimal minimalPrice) {
        this.minimalPrice = minimalPrice;
    }

    /** {@inheritDoc}*/
    @Override
    public LocalDateTime getSalefrom() {
        return salefrom;
    }

    /** {@inheritDoc}*/
    @Override
    public void setSalefrom(final LocalDateTime salefrom) {
        this.salefrom = salefrom;
    }

    /** {@inheritDoc}*/
    @Override
    public LocalDateTime getSaleto() {
        return saleto;
    }

    /** {@inheritDoc}*/
    @Override
    public void setSaleto(final LocalDateTime saleto) {
        this.saleto = saleto;
    }

    /** {@inheritDoc}*/
    @Override
    public BigDecimal getQuantity() {
        return quantity;
    }

    /** {@inheritDoc}*/
    @Override
    public void setQuantity(final BigDecimal quantity) {
        this.quantity = quantity;
    }

    /** {@inheritDoc}*/
    @Override
    public boolean isPriceUponRequest() {
        return priceUponRequest;
    }

    /** {@inheritDoc}*/
    @Override
    public void setPriceUponRequest(final boolean priceUponRequest) {
        this.priceUponRequest = priceUponRequest;
    }

    /** {@inheritDoc}*/
    @Override
    public boolean isPriceOnOffer() {
        return priceOnOffer;
    }

    /** {@inheritDoc}*/
    @Override
    public void setPriceOnOffer(final boolean priceOnOffer) {
        this.priceOnOffer = priceOnOffer;
    }

    /** {@inheritDoc}*/
    @Override
    public String getTag() {
        return tag;
    }

    /** {@inheritDoc}*/
    @Override
    public void setTag(final String tag) {
        this.tag = tag;
    }

    /** {@inheritDoc}*/
    @Override
    public String getPricingPolicy() {
        return pricingPolicy;
    }

    /** {@inheritDoc}*/
    @Override
    public void setPricingPolicy(final String pricingPolicy) {
        this.pricingPolicy = pricingPolicy;
    }

    /** {@inheritDoc}*/
    @Override
    public String getSupplier() {
        return supplier;
    }

    /** {@inheritDoc}*/
    @Override
    public void setSupplier(final String supplier) {
        this.supplier = supplier;
    }

    /** {@inheritDoc}*/
    @Override
    public String getRef() {
        return ref;
    }

    /** {@inheritDoc}*/
    @Override
    public void setRef(final String ref) {
        this.ref = ref;
    }

    /** {@inheritDoc}*/
    @Override
    public boolean isAutoGenerated() {
        return autoGenerated;
    }

    /** {@inheritDoc}*/
    @Override
    public void setAutoGenerated(final boolean autoGenerated) {
        this.autoGenerated = autoGenerated;
    }

    /** {@inheritDoc} */
    @Override
    public Instant getCreatedTimestamp() {
        return createdTimestamp;
    }

    /** {@inheritDoc} */
    @Override
    public void setCreatedTimestamp(final Instant createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    /** {@inheritDoc} */
    @Override
    public Instant getUpdatedTimestamp() {
        return updatedTimestamp;
    }

    /** {@inheritDoc} */
    @Override
    public void setUpdatedTimestamp(final Instant updatedTimestamp) {
        this.updatedTimestamp = updatedTimestamp;
    }

    /** {@inheritDoc} */
    @Override
    public String getCreatedBy() {
        return createdBy;
    }

    /** {@inheritDoc} */
    @Override
    public void setCreatedBy(final String createdBy) {
        this.createdBy = createdBy;
    }

    /** {@inheritDoc} */
    @Override
    public String getUpdatedBy() {
        return updatedBy;
    }

    /** {@inheritDoc} */
    @Override
    public void setUpdatedBy(final String updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof SkuPriceDTOImpl)) return false;

        final SkuPriceDTOImpl that = (SkuPriceDTOImpl) o;

        if (skuPriceId != that.skuPriceId) return false;
        if (shopId != that.shopId) return false;
        if (priceUponRequest != that.priceUponRequest) return false;
        if (priceOnOffer != that.priceOnOffer) return false;
        if (autoGenerated != that.autoGenerated) return false;
        if (regularPrice != null ? !regularPrice.equals(that.regularPrice) : that.regularPrice != null) return false;
        if (minimalPrice != null ? !minimalPrice.equals(that.minimalPrice) : that.minimalPrice != null) return false;
        if (salePrice != null ? !salePrice.equals(that.salePrice) : that.salePrice != null) return false;
        if (salefrom != null ? !salefrom.equals(that.salefrom) : that.salefrom != null) return false;
        if (saleto != null ? !saleto.equals(that.saleto) : that.saleto != null) return false;
        if (quantity != null ? !quantity.equals(that.quantity) : that.quantity != null) return false;
        if (currency != null ? !currency.equals(that.currency) : that.currency != null) return false;
        if (skuCode != null ? !skuCode.equals(that.skuCode) : that.skuCode != null) return false;
        if (skuName != null ? !skuName.equals(that.skuName) : that.skuName != null) return false;
        if (tag != null ? !tag.equals(that.tag) : that.tag != null) return false;
        if (pricingPolicy != null ? !pricingPolicy.equals(that.pricingPolicy) : that.pricingPolicy != null)
            return false;
        return ref != null ? ref.equals(that.ref) : that.ref == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (skuPriceId ^ (skuPriceId >>> 32));
        result = 31 * result + (regularPrice != null ? regularPrice.hashCode() : 0);
        result = 31 * result + (minimalPrice != null ? minimalPrice.hashCode() : 0);
        result = 31 * result + (salePrice != null ? salePrice.hashCode() : 0);
        result = 31 * result + (salefrom != null ? salefrom.hashCode() : 0);
        result = 31 * result + (saleto != null ? saleto.hashCode() : 0);
        result = 31 * result + (int) (shopId ^ (shopId >>> 32));
        result = 31 * result + (quantity != null ? quantity.hashCode() : 0);
        result = 31 * result + (priceUponRequest ? 1 : 0);
        result = 31 * result + (priceOnOffer ? 1 : 0);
        result = 31 * result + (currency != null ? currency.hashCode() : 0);
        result = 31 * result + (skuCode != null ? skuCode.hashCode() : 0);
        result = 31 * result + (skuName != null ? skuName.hashCode() : 0);
        result = 31 * result + (tag != null ? tag.hashCode() : 0);
        result = 31 * result + (pricingPolicy != null ? pricingPolicy.hashCode() : 0);
        result = 31 * result + (supplier != null ? supplier.hashCode() : 0);
        result = 31 * result + (ref != null ? ref.hashCode() : 0);
        result = 31 * result + (autoGenerated ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SkuPriceDTOImpl{" +
                "skuPriceId=" + skuPriceId +
                ", regularPrice=" + regularPrice +
                ", minimalPrice=" + minimalPrice +
                ", salePrice=" + salePrice +
                ", salefrom=" + salefrom +
                ", saleto=" + saleto +
                ", shopId=" + shopId +
                ", quantity=" + quantity +
                ", priceUponRequest=" + priceUponRequest +
                ", priceOnOffer=" + priceOnOffer +
                ", currency='" + currency + '\'' +
                ", skuCode='" + skuCode + '\'' +
                ", skuName='" + skuName + '\'' +
                ", tag='" + tag + '\'' +
                ", pricingPolicy='" + pricingPolicy + '\'' +
                ", supplier='" + supplier + '\'' +
                ", ref='" + ref + '\'' +
                ", autoGenerated=" + autoGenerated +
                '}';
    }
}
