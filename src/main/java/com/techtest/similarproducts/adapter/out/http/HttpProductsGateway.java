package com.techtest.similarproducts.adapter.out.http;

import com.techtest.similarproducts.application.port.out.ProductsPort;
import com.techtest.similarproducts.domain.exception.ProductNotFoundException;
import com.techtest.similarproducts.domain.exception.UpstreamServiceException;
import com.techtest.similarproducts.domain.model.ProductDetail;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class HttpProductsGateway implements ProductsPort {

    private static final int DEFAULT_CONNECT_TIMEOUT_MS = 500;
    private static final int DEFAULT_READ_TIMEOUT_MS = 1200;

    private final String baseUrl;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public HttpProductsGateway(@Value("${external.api.base-url}") String baseUrl,
                               @Value("${external.api.connect-timeout-ms}") int connectTimeoutMs,
                               @Value("${external.api.read-timeout-ms}") int readTimeoutMs,
                               ObjectMapper objectMapper) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.objectMapper = objectMapper;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);
        this.restClient = RestClient.builder().requestFactory(requestFactory).baseUrl(this.baseUrl).build();
    }

    HttpProductsGateway(String baseUrl, ObjectMapper objectMapper) {
        this(baseUrl, DEFAULT_CONNECT_TIMEOUT_MS, DEFAULT_READ_TIMEOUT_MS, objectMapper);
    }

    @Override
    public List<String> getSimilarIds(String productId) {
        try {
            String body = restClient.get().uri("/product/{id}/similarids", productId)
                    .retrieve().body(String.class);
            JsonNode node = objectMapper.readTree(body);
            if (!node.isArray()) {
                throw new UpstreamServiceException("Invalid similar IDs response");
            }
            List<String> ids = new ArrayList<>();
            for (JsonNode item : node) {
                ids.add(item.asText());
            }
            return ids;
        } catch (HttpClientErrorException.NotFound e) {
            throw new ProductNotFoundException(productId);
        } catch (HttpStatusCodeException e) {
            throw new UpstreamServiceException("Unexpected similarids status: " + e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new UpstreamServiceException("Similar products service unavailable", e);
        } catch (UpstreamServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new UpstreamServiceException("Failed to fetch similar IDs", e);
        }
    }

    @Override
    public Optional<ProductDetail> getProductDetail(String productId) {
        try {
            return Optional.ofNullable(restClient.get().uri("/product/{id}", productId)
                    .retrieve().body(ProductDetail.class));
        } catch (HttpStatusCodeException | ResourceAccessException e) {
            return Optional.empty();
        }
    }
}
