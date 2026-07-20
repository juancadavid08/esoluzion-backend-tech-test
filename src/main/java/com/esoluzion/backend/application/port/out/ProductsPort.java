package com.esoluzion.backend.application.port.out;

import com.esoluzion.backend.domain.model.ProductDetail;

import java.util.List;
import java.util.Optional;

public interface ProductsPort {
    List<String> getSimilarIds(String productId);

    Optional<ProductDetail> getProductDetail(String productId);
}
