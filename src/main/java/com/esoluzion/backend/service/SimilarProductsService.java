package com.esoluzion.backend.service;

import com.esoluzion.backend.gateway.ProductsGateway;
import com.esoluzion.backend.model.ProductDetail;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class SimilarProductsService {

    private static final long DETAIL_TIMEOUT_MS = 1500;
    private final ProductsGateway productsGateway;
    private final Executor detailExecutor = Executors.newFixedThreadPool(16);

    public SimilarProductsService(ProductsGateway productsGateway) {
        this.productsGateway = productsGateway;
    }

    public List<ProductDetail> getSimilarProducts(String productId) {
        List<String> similarIds = productsGateway.getSimilarIds(productId);
        List<CompletableFuture<Optional<ProductDetail>>> futures = similarIds.stream()
                .map(this::fetchDetailAsync)
            .collect(Collectors.toList());

        List<ProductDetail> result = new ArrayList<>();
        for (CompletableFuture<Optional<ProductDetail>> future : futures) {
            future.join().ifPresent(result::add);
        }
        return result;
    }

    private CompletableFuture<Optional<ProductDetail>> fetchDetailAsync(String similarId) {
        return CompletableFuture
                .supplyAsync(() -> productsGateway.getProductDetail(similarId), detailExecutor)
                .completeOnTimeout(Optional.empty(), DETAIL_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .exceptionally(ignored -> Optional.empty());
    }
}
