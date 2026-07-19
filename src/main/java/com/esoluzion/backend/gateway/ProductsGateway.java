package com.esoluzion.backend.gateway;

import com.esoluzion.backend.model.ProductDetail;

import java.util.List;
import java.util.Optional;

public interface ProductsGateway {
    List<String> getSimilarIds(String productId);

    Optional<ProductDetail> getProductDetail(String productId);
}
