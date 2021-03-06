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

package org.yes.cart.web.service.wicketsupport.impl;

import org.apache.commons.lang.StringUtils;
import org.yes.cart.service.theme.templates.TemplateProcessor;
import org.yes.cart.web.utils.WicketUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * User: denispavlov
 * Date: 25/04/2014
 * Time: 16:37
 */
public class WicketUrlTemplateFunctionProviderImpl implements TemplateProcessor.FunctionProvider {

    private final WicketUtil wicketUtil;
    private final String[] paramName;

    public WicketUrlTemplateFunctionProviderImpl(final WicketUtil wicketUtil) {
        this.wicketUtil = wicketUtil;
        this.paramName = null;
    }

    public WicketUrlTemplateFunctionProviderImpl(final WicketUtil wicketUtil, final String ... paramName) {
        this.wicketUtil = wicketUtil;
        this.paramName = paramName;
    }

    @Override
    public Object doAction(final Object... params) {

        final StringBuilder url = new StringBuilder(wicketUtil.getHttpServletRequest().getContextPath());
        if (params != null && params.length >= 1) {

            final List sections;
            if (params[0] instanceof Collection) {
                sections = new ArrayList((Collection) params[0]);
            } else if (params[0] != null) {
                sections = Collections.singletonList(String.valueOf(params[0]));
            } else {
                sections = Collections.emptyList();
            }

            final int pLength = this.paramName != null ? this.paramName.length : 0;
            if (pLength <= sections.size()) {
                for (int i = 0; i < pLength; i++) {
                    final String sec = (String) sections.get(i);
                    if (StringUtils.isNotBlank(sec)) {
                        url.append('/').append(this.paramName[i]).append('/').append(sec);
                    }
                }
                for (int i = pLength; i < sections.size(); i++) {
                    final String sec = (String) sections.get(i);
                    if (StringUtils.isNotBlank(sec)) {
                        url.append('/').append(sec);
                    }
                }
            }
        }
        if (url.length() == 0) {
            return "/";
        }
        return url.toString();
    }



}
