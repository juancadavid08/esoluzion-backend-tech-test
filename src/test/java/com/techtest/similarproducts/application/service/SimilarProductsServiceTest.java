package com.techtest.similarproducts.application.service;

import com.techtest.similarproducts.application.port.out.ProductsPort;
import com.techtest.similarproducts.domain.model.ProductDetail;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SimilarProductsServiceTest {

    private final Executor executor = Executors.newFixedThreadPool(4);

    @Test
    void shouldKeepOrderAndSkipMissingProducts() {
        ProductsPort gateway = mock(ProductsPort.class);
        when(gateway.getSimilarIds("1")).thenReturn(List.of("2", "3", "4"));
        when(gateway.getProductDetail("2")).thenReturn(Optional.of(new ProductDetail("2", "Dress", BigDecimal.valueOf(19.99), true)));
        when(gateway.getProductDetail("3")).thenReturn(Optional.empty());
        when(gateway.getProductDetail("4")).thenReturn(Optional.of(new ProductDetail("4", "Boots", BigDecimal.valueOf(39.99), true)));

        SimilarProductsService service = new SimilarProductsService(gateway, executor, 1500, 20);
        List<ProductDetail> result = service.getSimilarProducts("1");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo("2");
        assertThat(result.get(1).getId()).isEqualTo("4");
    }

    @Test
    void shouldDeduplicateAndLimitSimilarIds() {
        ProductsPort gateway = mock(ProductsPort.class);
        List<String> ids = Stream.concat(
                IntStream.rangeClosed(1, 30).mapToObj(String::valueOf),
                Stream.of("2", "3", "4"))
            .toList();

        when(gateway.getSimilarIds("1")).thenReturn(ids);
        when(gateway.getProductDetail(anyString())).thenAnswer(invocation -> {
            String id = invocation.getArgument(0);
            return Optional.of(new ProductDetail(id, "Product " + id, BigDecimal.ONE, true));
        });

        SimilarProductsService service = new SimilarProductsService(gateway, executor, 1500, 20);
        List<ProductDetail> result = service.getSimilarProducts("1");

        assertThat(result).hasSize(20);
        assertThat(result.get(0).getId()).isEqualTo("1");
        assertThat(result.get(19).getId()).isEqualTo("20");
    }

    @Test
    void shouldReturnEmptyWhenNoSimilarIds() {
        ProductsPort gateway = mock(ProductsPort.class);
        when(gateway.getSimilarIds("1")).thenReturn(Collections.emptyList());

        SimilarProductsService service = new SimilarProductsService(gateway, executor, 1500, 20);
        List<ProductDetail> result = service.getSimilarProducts("1");

        assertThat(result).isEmpty();
    }

    @Test
    void shouldFilterNullAndBlankIds() {
        ProductsPort gateway = mock(ProductsPort.class);
        when(gateway.getSimilarIds("1")).thenReturn(Arrays.asList("2", null, "", "3"));
        when(gateway.getProductDetail("2")).thenReturn(Optional.of(new ProductDetail("2", "Dress", BigDecimal.valueOf(19.99), true)));
        when(gateway.getProductDetail("3")).thenReturn(Optional.of(new ProductDetail("3", "Boots", BigDecimal.valueOf(39.99), true)));

        SimilarProductsService service = new SimilarProductsService(gateway, executor, 1500, 20);
        List<ProductDetail> result = service.getSimilarProducts("1");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo("2");
        assertThat(result.get(1).getId()).isEqualTo("3");
    }

    @Test
    void shouldSkipProductWhenDetailFetchThrowsException() {
        ProductsPort gateway = mock(ProductsPort.class);
        when(gateway.getSimilarIds("1")).thenReturn(List.of("2", "3"));
        when(gateway.getProductDetail("2")).thenReturn(Optional.of(new ProductDetail("2", "Dress", BigDecimal.valueOf(19.99), true)));
        when(gateway.getProductDetail("3")).thenThrow(new RuntimeException("gateway failure"));

        SimilarProductsService service = new SimilarProductsService(gateway, executor, 1500, 20);
        List<ProductDetail> result = service.getSimilarProducts("1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("2");
    }
}
