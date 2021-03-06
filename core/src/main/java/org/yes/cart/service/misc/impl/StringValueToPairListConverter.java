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

package org.yes.cart.service.misc.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.yes.cart.domain.misc.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Igor Azarny iazarny@yahoo.com
 * Date: 09-May-2011
 * Time: 14:12:54
 */
public class StringValueToPairListConverter implements Converter<String, List> {



    /**
     * Convert string , that hold comma separated [key-]value into list of string pairs.
     * Example of string: R-Red,G-Green,B-Blue or Dog,Cat,Bird
     * @param values comma separated values
     * @return list of string pairs
     */
    @Override
    public List convert(final String values) {
        return getOptions(values);
    }


    /**
     * Convert string , that hold comma separated [key-]value into list of string pairs.
     * Example of string: R-Red,G-Green,B-Blue or Dog,Cat,Bird
     * @param values comma separated values
     * @return list of string pairs
     */
    List<Pair<String, String>> getOptions(final String values) {
        final List<Pair<String, String>> res = new ArrayList<>();
        if (StringUtils.isNotBlank(values)) {
            final String [] entries = StringUtils.split(values,',');
            for (String entry : entries) {
                final String [] keyValue = StringUtils.splitPreserveAllTokens(entry,'-');
                if (keyValue.length > 1) {
                    res.add(new Pair<>(keyValue[0], keyValue[1]));
                } else {
                    res.add(new Pair<>(keyValue[0], keyValue[0]));
                }
            }
        }
        return res;
    }


}
