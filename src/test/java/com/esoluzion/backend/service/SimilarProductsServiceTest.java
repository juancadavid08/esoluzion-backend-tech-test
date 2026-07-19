package com.esoluzion.backend.service;

import com.esoluzion.backend.gateway.ProductsGateway;
import com.esoluzion.backend.model.ProductDetail;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SimilarProductsServiceTest {

    @Test
    void shouldKeepOrderAndSkipMissingProducts() {
        ProductsGateway gateway = mock(ProductsGateway.class);
        when(gateway.getSimilarIds("1")).thenReturn(List.of("2", "3", "4"));
        when(gateway.getProductDetail("2")).thenReturn(Optional.of(new ProductDetail("2", "Dress", BigDecimal.valueOf(19.99), true)));
        when(gateway.getProductDetail("3")).thenReturn(Optional.empty());
        when(gateway.getProductDetail("4")).thenReturn(Optional.of(new ProductDetail("4", "Boots", BigDecimal.valueOf(39.99), true)));

        SimilarProductsService service = new SimilarProductsService(gateway);
        List<ProductDetail> result = service.getSimilarProducts("1");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo("2");
        assertThat(result.get(1).getId()).isEqualTo("4");
    }
}
