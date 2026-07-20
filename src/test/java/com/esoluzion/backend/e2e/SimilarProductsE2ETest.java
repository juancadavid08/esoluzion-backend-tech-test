package com.esoluzion.backend.e2e;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SimilarProductsE2ETest {
    private static final MockWebServer UPSTREAM = startUpstream();

    @LocalServerPort
    private int port;

    private final TestRestTemplate client = new TestRestTemplate();

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("external.api.base-url", () -> UPSTREAM.url("/").toString());
    }

    @AfterAll
    static void stopUpstream() throws IOException {
        UPSTREAM.shutdown();
    }

    @Test
    void returnsSimilarProductsInUpstreamOrder() {
        dispatchWith("[\"2\",\"3\"]", 200, false);
        ResponseEntity<String> response = get("1");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"id\":\"2\"").contains("\"id\":\"3\"");
    }

    @Test
    void returnsEmptyListWhenThereAreNoSimilarIds() {
        dispatchWith("[]", 200, false);
        ResponseEntity<String> response = get("empty");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("[]");
    }

    @Test
    void skipsAFailedDetailAndKeepsSuccessfulOnes() {
        dispatchWith("[\"2\",\"missing\"]", 200, true);
        ResponseEntity<String> response = get("partial");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"id\":\"2\"").doesNotContain("missing");
    }

    @Test
    void mapsMissingSourceProductToStructured404() {
        dispatchWith("", 404, false);
        ResponseEntity<String> response = get("404");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("PRODUCT_NOT_FOUND");
    }

    @Test
    void mapsUpstreamFailureToStructured502() {
        dispatchWith("", 500, false);
        ResponseEntity<String> response = get("failure");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody()).contains("UPSTREAM_ERROR");
    }

    private ResponseEntity<String> get(String productId) {
        return client.getForEntity("http://localhost:" + port + "/product/" + productId + "/similar", String.class);
    }

    private void dispatchWith(String ids, int idsStatus, boolean missingDetail) {
        UPSTREAM.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                String path = request.getPath();
                if (path != null && path.endsWith("/similarids")) {
                    return json(idsStatus, ids);
                }
                if (missingDetail && path != null && path.endsWith("/missing")) {
                    return new MockResponse().setResponseCode(500);
                }
                String id = path == null ? "unknown" : path.substring(path.lastIndexOf('/') + 1);
                return json(200, "{\"id\":\"" + id + "\",\"name\":\"Product " + id
                        + "\",\"price\":10,\"availability\":true}");
            }
        });
    }

    private static MockResponse json(int status, String body) {
        return new MockResponse().setResponseCode(status).setHeader("Content-Type", "application/json").setBody(body);
    }

    private static MockWebServer startUpstream() {
        MockWebServer server = new MockWebServer();
        try {
            server.start();
            return server;
        } catch (IOException exception) {
            throw new IllegalStateException("Could not start mock upstream", exception);
        }
    }
}
