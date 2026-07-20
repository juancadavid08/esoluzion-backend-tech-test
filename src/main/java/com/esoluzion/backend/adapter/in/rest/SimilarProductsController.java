package com.esoluzion.backend.adapter.in.rest;

import com.esoluzion.backend.application.port.in.GetSimilarProductsUseCase;
import com.esoluzion.backend.domain.model.ProductDetail;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/product")
@Validated
public class SimilarProductsController {

    private final GetSimilarProductsUseCase getSimilarProducts;

    public SimilarProductsController(GetSimilarProductsUseCase getSimilarProducts) {
        this.getSimilarProducts = getSimilarProducts;
    }

    @GetMapping("/{productId}/similar")
    public List<ProductDetail> getSimilarProducts(
            @PathVariable @NotBlank @Size(max = 100)
            @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "must contain only letters, numbers, '_' or '-'")
            String productId) {
        return getSimilarProducts.getSimilarProducts(productId);
    }
}
