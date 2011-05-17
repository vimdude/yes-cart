package org.yes.cart.service.impl;

import org.yes.cart.service.CategoryImageRetrieveStrategy;
import org.yes.cart.constants.Constants;
import org.yes.cart.service.domain.ProductService;
import org.yes.cart.domain.entity.Category;
import org.yes.cart.domain.entity.Product;
import org.yes.cart.domain.entity.AttrValue;

/**
 * User: Igor Azarny iazarny@yahoo.com
 * Date: 2011-May-17
 * Time: 16:22:01
 */
public class CategoryImageRetrieveStrategyRandomProductImpl  implements CategoryImageRetrieveStrategy {

    /**
     * Get image repository url pattern.
     *
     * @return image repository url pattern
     */
    public String getImageRepositoryUrlPattern() {
        return Constants.PRODUCT_IMAGE_REPOSITORY_URL_PATTERN;
    }

    private final ProductService productService;
    private final String attributeCode;

    /**
     * Construct category image retreive strategy
     * @param productService product service to get random product in category.
     */
    public CategoryImageRetrieveStrategyRandomProductImpl(
            final ProductService productService,
            final String attributeCode) {
        this.productService = productService;
        this.attributeCode = attributeCode;
    }

    /** {@inheritDoc} */
    public String getImageName(final Category category) {
        Product product = productService.getRandomProductByCategory(category);
        if (product != null) {
            AttrValue attrValue = product.getAttributeByCode(attributeCode);
            if (attrValue != null) {
                return attrValue.getVal();
            }
        }
        return Constants.NO_IMAGE;
    }
    
}
