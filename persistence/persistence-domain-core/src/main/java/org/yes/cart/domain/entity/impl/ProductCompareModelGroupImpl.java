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

package org.yes.cart.domain.entity.impl;

import org.yes.cart.domain.entity.ProductCompareModelAttribute;
import org.yes.cart.domain.entity.ProductCompareModelGroup;
import org.yes.cart.domain.i18n.I18NModel;

import java.util.*;

/**
 * User: denispavlov
 * Date: 24/05/2020
 * Time: 08:49
 */
public class ProductCompareModelGroupImpl implements ProductCompareModelGroup {

    private final String code;
    private final I18NModel displayNames;
    private final List<ProductCompareModelAttribute> attributes = new ArrayList<>();
    private final Map<String, ProductCompareModelAttribute> map = new HashMap<>();


    public ProductCompareModelGroupImpl(final String code, final I18NModel displayNames) {
        this.code = code;
        this.displayNames = displayNames;
    }


    /** {@inheritDoc} */
    @Override
    public String getCode() {
        return this.code;
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, String> getDisplayNames() {
        return this.displayNames.getAllValues();
    }

    /** {@inheritDoc} */
    @Override
    public String getDisplayName(final String lang) {
        return this.displayNames.getValue(lang);
    }

    /** {@inheritDoc} */
    @Override
    public List<ProductCompareModelAttribute> getAttributes() {
        return Collections.unmodifiableList(this.attributes);
    }

    /** {@inheritDoc} */
    @Override
    public ProductCompareModelAttribute getAttribute(final String code) {
        return this.map.get(code);
    }

    /**
     * Append items to internal list.
     *
     * @param value value to append
     */
    public void addAttribute(final ProductCompareModelAttribute value) {
        if (!this.map.containsKey(value.getCode())) {
            this.map.put(value.getCode(), value);
            this.attributes.add(value);
        }
    }

}
