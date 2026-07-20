package com.esoluzion.backend.application.port.in;

import com.esoluzion.backend.domain.model.ProductDetail;

import java.util.List;

public interface GetSimilarProductsUseCase {
    List<ProductDetail> getSimilarProducts(String productId);
}
