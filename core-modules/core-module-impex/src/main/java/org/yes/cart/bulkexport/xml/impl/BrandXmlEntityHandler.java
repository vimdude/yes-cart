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

package org.yes.cart.bulkexport.xml.impl;

import org.yes.cart.bulkcommon.model.ImpExTuple;
import org.yes.cart.bulkcommon.xml.XmlValueAdapter;
import org.yes.cart.bulkexport.xml.XmlExportDescriptor;
import org.yes.cart.domain.entity.Brand;
import org.yes.cart.service.async.JobStatusListener;

import java.io.OutputStreamWriter;

/**
 * User: denispavlov
 * Date: 26/10/2018
 * Time: 08:08
 */
public class BrandXmlEntityHandler extends AbstractXmlEntityHandler<Brand> {

    public BrandXmlEntityHandler() {
        super("brands");
    }

    @Override
    public void handle(final JobStatusListener statusListener,
                       final XmlExportDescriptor xmlExportDescriptor,
                       final ImpExTuple<String, Brand> tuple,
                       final XmlValueAdapter xmlValueAdapter,
                       final String fileToExport,
                       final OutputStreamWriter writer) throws Exception {

        handleInternal(tagBrand(null, tuple.getData()), writer, statusListener);

    }


    Tag tagBrand(final Tag parent, final Brand brand) {

        return tag(parent, "brand")
                .attr("id", brand.getBrandId())
                .attr("guid", brand.getGuid())
                    .tagCdata("name", brand.getName())
                    .tagCdata("description", brand.getDescription())
                    .tagExt(brand)
                    .tagTime(brand)
                .end();

    }

}
