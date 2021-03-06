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

package org.yes.cart.search.dao.support;

import org.yes.cart.domain.entity.Attribute;

import java.util.Set;

/**
 * User: denispavlov
 * Date: 16/11/2014
 * Time: 17:07
 */
public interface NavigatableAttributesSupport {

    /**
     * Get all navigatable attribute codes (+ brands, price, query and tag).
     *
     * @return set of attribute codes.
     */
    Set<String> getAllNavigatableAttributeCodes();

    /**
     * Get all searchable attribute codes (navigatable are not included).
     *
     * @return set of attribute codes.
     */
    Set<String> getAllSearchableAttributeCodes();

    /**
     * Get all searchable primary attribute codes.
     *
     * @return set of attribute codes.
     */
    Set<String> getAllSearchablePrimaryAttributeCodes();

    /**
     * Get all storable attribute codes.
     *
     * @return set of attribute codes.
     */
    Set<String> getAllStorableAttributeCodes();

    /**
     * Get attribute by given code.
     *
     * @param attributeCode given  code
     * @return instance {@link Attribute} if fount, otherwise null
     */
    Attribute getByAttributeCode(String attributeCode);

}
