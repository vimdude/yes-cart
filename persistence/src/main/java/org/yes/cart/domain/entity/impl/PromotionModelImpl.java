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
package org.yes.cart.domain.entity.impl;

import org.yes.cart.domain.entity.PromotionModel;
import org.yes.cart.domain.i18n.I18NModel;

import java.util.Date;

/**
 * User: denispavlov
 * Date: 06/10/2015
 * Time: 14:35
 */
public class PromotionModelImpl implements PromotionModel {

    private final String code;
    private final String couponCode;
    private final String type;
    private final String action;
    private final String context;
    private final I18NModel name;
    private final I18NModel description;
    private final Date activeFrom;
    private final Date activeTo;

    public PromotionModelImpl(final String code,
                              final String couponCode,
                              final String type,
                              final String action,
                              final String context,
                              final I18NModel name,
                              final I18NModel description,
                              final Date activeFrom,
                              final Date activeTo) {
        this.code = code;
        this.couponCode = couponCode;
        this.type = type;
        this.action = action;
        this.context = context;
        this.name = name;
        this.description = description;
        this.activeFrom = activeFrom;
        this.activeTo = activeTo;
    }

    /**
     * {@inheritDoc}
     */
    public String getCode() {
        return code;
    }

    /**
     * {@inheritDoc}
     */
    public String getCouponCode() {
        return couponCode;
    }

    /**
     * {@inheritDoc}
     */
    public String getType() {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    public String getAction() {
        return action;
    }

    /**
     * {@inheritDoc}
     */
    public String getContext() {
        return context;
    }

    /**
     * {@inheritDoc}
     */
    public I18NModel getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    public I18NModel getDescription() {
        return description;
    }

    /**
     * {@inheritDoc}
     */
    public Date getActiveFrom() {
        return activeFrom;
    }

    /**
     * {@inheritDoc}
     */
    public Date getActiveTo() {
        return activeTo;
    }

}