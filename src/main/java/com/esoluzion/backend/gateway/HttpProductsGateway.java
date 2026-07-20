package com.esoluzion.backend.gateway;

import com.esoluzion.backend.exception.ProductNotFoundException;
import com.esoluzion.backend.model.ProductDetail;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class HttpProductsGateway implements ProductsGateway {

    private static final int DEFAULT_CONNECT_TIMEOUT_MS = 500;
    private static final int DEFAULT_READ_TIMEOUT_MS = 1200;

    private final String baseUrl;
    private final RestTemplate restTemplate;
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
        this.restTemplate = new RestTemplate(requestFactory);
    }

    HttpProductsGateway(String baseUrl, ObjectMapper objectMapper) {
        this(baseUrl, DEFAULT_CONNECT_TIMEOUT_MS, DEFAULT_READ_TIMEOUT_MS, objectMapper);
    }

    @Override
    public List<String> getSimilarIds(String productId) {
        try {
            String url = baseUrl + "/product/" + productId + "/similarids";
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
            JsonNode node = objectMapper.readTree(response.getBody());
            List<String> ids = new ArrayList<>();
            for (JsonNode item : node) {
                ids.add(item.asText());
            }
            return ids;
        } catch (HttpClientErrorException.NotFound e) {
            throw new ProductNotFoundException(productId);
        } catch (HttpStatusCodeException e) {
            throw new IllegalStateException("Unexpected similarids status: " + e.getStatusCode().value(), e);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to fetch similar IDs", e);
        }
    }

    @Override
    public Optional<ProductDetail> getProductDetail(String productId) {
        try {
            String url = baseUrl + "/product/" + productId;
            ResponseEntity<ProductDetail> response = restTemplate.getForEntity(url, ProductDetail.class);
            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                return Optional.empty();
            }
            return Optional.of(response.getBody());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
