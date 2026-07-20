package com.techtest.similarproducts.application.port.out;

import com.techtest.similarproducts.domain.model.ProductDetail;

import java.util.List;
import java.util.Optional;

public interface ProductsPort {
    List<String> getSimilarIds(String productId);

    Optional<ProductDetail> getProductDetail(String productId);
}
