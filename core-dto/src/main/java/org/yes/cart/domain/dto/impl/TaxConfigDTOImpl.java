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
import org.yes.cart.domain.dto.TaxConfigDTO;

import java.time.Instant;

/**
 * User: denispavlov
 * Date: 28/10/2014
 * Time: 17:26
 */
@Dto
public class TaxConfigDTOImpl implements TaxConfigDTO {

    @DtoField(value = "taxConfigId", readOnly = true)
    private long taxConfigId;

    @DtoField(value = "tax.taxId", readOnly = true)
    private long taxId;
    @DtoField(value = "productCode")
    private String productCode;
    @DtoField(value = "stateCode")
    private String stateCode;
    @DtoField(value = "countryCode")
    private String countryCode;

    @DtoField(value = "guid", readOnly = true)
    private String guid;


    @DtoField(readOnly = true)
    private Instant createdTimestamp;
    @DtoField(readOnly = true)
    private Instant updatedTimestamp;
    @DtoField(readOnly = true)
    private String createdBy;
    @DtoField(readOnly = true)
    private String updatedBy;

    /** {@inheritDoc} */
    @Override
    public long getId() {
        return taxConfigId;
    }

    /** {@inheritDoc} */
    @Override
    public String getGuid() {
        return guid;
    }

    /** {@inheritDoc} */
    @Override
    public void setGuid(final String guid) {
        this.guid = guid;
    }

    /** {@inheritDoc} */
    @Override
    public long getTaxConfigId() {
        return taxConfigId;
    }

    /** {@inheritDoc} */
    @Override
    public void setTaxConfigId(final long taxConfigId) {
        this.taxConfigId = taxConfigId;
    }

    /** {@inheritDoc} */
    @Override
    public long getTaxId() {
        return taxId;
    }

    /** {@inheritDoc} */
    @Override
    public void setTaxId(final long taxId) {
        this.taxId = taxId;
    }

    /** {@inheritDoc} */
    @Override
    public String getProductCode() {
        return productCode;
    }

    /** {@inheritDoc} */
    @Override
    public void setProductCode(final String productCode) {
        this.productCode = productCode;
    }

    /** {@inheritDoc} */
    @Override
    public String getStateCode() {
        return stateCode;
    }

    /** {@inheritDoc} */
    @Override
    public void setStateCode(final String stateCode) {
        this.stateCode = stateCode;
    }

    /** {@inheritDoc} */
    @Override
    public String getCountryCode() {
        return countryCode;
    }

    /** {@inheritDoc} */
    @Override
    public void setCountryCode(final String countryCode) {
        this.countryCode = countryCode;
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

}
