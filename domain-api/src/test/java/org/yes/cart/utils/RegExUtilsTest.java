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

package org.yes.cart.utils;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * User: denispavlov
 * Date: 25/08/2018
 * Time: 11:18
 */
public class RegExUtilsTest {
    @Test
    public void testGetInstance() throws Exception {

        assertTrue("Must be null safe", RegExUtils.getInstance(null).matches("null matches any string"));
        assertTrue("Must be miss-configuration proof", RegExUtils.getInstance("   ").matches("empty matches any string"));
        assertTrue("Must match regex as positive", RegExUtils.getInstance("\\d+").matches("111111"));
        assertFalse("Must not match regex as negative", RegExUtils.getInstance("\\d+").matches("111111a"));

    }

}