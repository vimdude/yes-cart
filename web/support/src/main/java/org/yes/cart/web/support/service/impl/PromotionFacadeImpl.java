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

package org.yes.cart.web.support.service.impl;

import org.yes.cart.service.domain.PromotionService;
import org.yes.cart.web.support.service.PromotionFacade;

/**
 * User: denispavlov
 * Date: 13-10-19
 * Time: 11:21 PM
 */
public class PromotionFacadeImpl implements PromotionFacade {

    private final PromotionService promotionService;

    public PromotionFacadeImpl(final PromotionService promotionService) {
        this.promotionService = promotionService;
    }
}
