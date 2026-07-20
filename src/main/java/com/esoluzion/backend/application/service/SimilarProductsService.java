package com.esoluzion.backend.application.service;

import com.esoluzion.backend.application.port.in.GetSimilarProductsUseCase;
import com.esoluzion.backend.application.port.out.ProductsPort;
import com.esoluzion.backend.domain.model.ProductDetail;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Service
public class SimilarProductsService implements GetSimilarProductsUseCase {

    private final ProductsPort productsGateway;
    private final Executor detailExecutor;
    private final long detailTimeoutMs;
    private final int maxSimilarIds;

    public SimilarProductsService(ProductsPort productsGateway,
                                  @Qualifier("similarProductsExecutor") Executor detailExecutor,
                                  @Value("${similar-products.detail-timeout-ms}") long detailTimeoutMs,
                                  @Value("${similar-products.max-similar-ids}") int maxSimilarIds) {
        this.productsGateway = productsGateway;
        this.detailExecutor = detailExecutor;
        this.detailTimeoutMs = detailTimeoutMs;
        this.maxSimilarIds = maxSimilarIds;
    }

    @Override
    public List<ProductDetail> getSimilarProducts(String productId) {
        List<String> similarIds = productsGateway.getSimilarIds(productId).stream()
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .limit(maxSimilarIds)
            .toList();

        List<CompletableFuture<Optional<ProductDetail>>> futures = similarIds.stream()
                .map(this::fetchDetailAsync)
            .toList();

        List<ProductDetail> result = new ArrayList<>();
        for (CompletableFuture<Optional<ProductDetail>> future : futures) {
            future.join().ifPresent(result::add);
        }
        return result;
    }

    private CompletableFuture<Optional<ProductDetail>> fetchDetailAsync(String similarId) {
        return CompletableFuture
                .supplyAsync(() -> productsGateway.getProductDetail(similarId), detailExecutor)
                .completeOnTimeout(Optional.empty(), detailTimeoutMs, TimeUnit.MILLISECONDS)
                .exceptionally(ignored -> Optional.empty());
    }
}
