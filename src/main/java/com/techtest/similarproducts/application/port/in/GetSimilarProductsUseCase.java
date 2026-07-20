package com.techtest.similarproducts.application.port.in;

import com.techtest.similarproducts.domain.model.ProductDetail;

import java.util.List;

public interface GetSimilarProductsUseCase {
    List<ProductDetail> getSimilarProducts(String productId);
}
