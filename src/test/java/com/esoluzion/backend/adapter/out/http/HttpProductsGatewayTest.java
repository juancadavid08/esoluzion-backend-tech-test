package com.esoluzion.backend.adapter.out.http;

import com.esoluzion.backend.domain.exception.ProductNotFoundException;
import com.esoluzion.backend.domain.exception.UpstreamServiceException;
import com.esoluzion.backend.domain.model.ProductDetail;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HttpProductsGatewayTest {

    private MockWebServer server;
    private HttpProductsGateway gateway;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        String baseUrl = server.url("/").toString();
        gateway = new HttpProductsGateway(baseUrl, new ObjectMapper());
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void shouldReturnSimilarIds() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("[\"2\",\"3\",\"4\"]"));

        List<String> ids = gateway.getSimilarIds("1");

        assertThat(ids).containsExactly("2", "3", "4");
    }

    @Test
    void shouldThrowNotFoundForMissingProduct() {
        server.enqueue(new MockResponse().setResponseCode(404));

        assertThatThrownBy(() -> gateway.getSimilarIds("404"))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void shouldThrowIllegalStateForUnexpectedSimilarIdsStatus() {
        server.enqueue(new MockResponse().setResponseCode(500));

        assertThatThrownBy(() -> gateway.getSimilarIds("1"))
                .isInstanceOf(UpstreamServiceException.class)
                .hasMessageContaining("Unexpected similarids status: 500");
    }

    @Test
    void shouldReturnProductDetailWhenFound() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"id\":\"2\",\"name\":\"Dress\",\"price\":19.99,\"availability\":true}"));

        Optional<ProductDetail> detail = gateway.getProductDetail("2");

        assertThat(detail).isPresent();
        assertThat(detail.get().getId()).isEqualTo("2");
        assertThat(detail.get().getName()).isEqualTo("Dress");
        assertThat(detail.get().isAvailability()).isTrue();
    }

    @Test
    void shouldReturnEmptyWhenDetailFails() {
        server.enqueue(new MockResponse().setResponseCode(500));

        Optional<ProductDetail> detail = gateway.getProductDetail("2");

        assertThat(detail).isEmpty();
    }
}
