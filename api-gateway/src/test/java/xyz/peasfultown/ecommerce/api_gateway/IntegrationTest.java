package xyz.peasfultown.ecommerce.api_gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.common.net.HttpHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@EnableWireMock
class IntegrationTest {
    @Autowired
    private WebTestClient webTestClient;

    @InjectWireMock
    private WireMockServer wiremock;

    @Autowired
    private ObjectMapper oMapper;

    private static final String USER_ID = UUID.randomUUID().toString();
    private static final String CUSTOMER_TOKEN = JwtTestHelper.generateToken(USER_ID, "CUSTOMER");
    private static final String ADMIN_TOKEN = JwtTestHelper.generateToken(USER_ID, "ADMIN");
    private static final String EXPIRED_TOKEN = JwtTestHelper.generateExpiredToken(USER_ID, "CUSTOMER");


    @BeforeEach
    void setup() {
        wiremock.resetAll();
    }

    @Test
    void shouldRouteRegisterRequest_toAuthService() throws Exception {
        // tell WireMock what to return when gateway forwards the request
        Map<String, String> wireMockResponse = new HashMap<>();
        wireMockResponse.put("accessToken", "token");
        wireMockResponse.put("refreshToken", "refToken");
        wiremock.stubFor(WireMock.post(urlEqualTo("/api/v1/auth/register"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody(oMapper.writeValueAsString(wireMockResponse))));

        Map<String, String> testClientResponse = new HashMap<>();
        testClientResponse.put("email", "user@example.com");
        testClientResponse.put("password", "password");

        webTestClient.post()
                .uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(oMapper.writeValueAsString(testClientResponse))
                .exchange()
                .expectStatus().isCreated();

        // verify gateway actually forwarded the request
        wiremock.verify(postRequestedFor(urlEqualTo("/api/v1/auth/register")));
    }

    @Test
    void getProducts_shouldReturn200_withoutJwt() throws Exception {
        wiremock.stubFor(WireMock.get(urlEqualTo("/api/v1/products"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")));

        webTestClient.get()
                .uri("/api/v1/products")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void postProducts_shouldReturn201_withValidToken() throws Exception {
        wiremock.stubFor(post(urlEqualTo("/api/v1/products"))
                .willReturn(aResponse()
                        .withStatus(201)));

        webTestClient.post()
                .uri("/api/v1/products")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_TOKEN)
                .exchange()
                .expectStatus().isCreated()
        ;
    }

    @Test
    void postProducts_shouldReturn403_whenNotAdmin() throws Exception {
        webTestClient.post()
                .uri("/api/v1/products")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + CUSTOMER_TOKEN)
                .exchange()
                .expectStatus().isForbidden()
        ;
    }

    @Test
    void postProducts_shouldReturn401_withoutJwt() throws Exception {
        webTestClient.post()
                .uri("/api/v1/products")
                .exchange()
                .expectStatus().isUnauthorized()
        ;
    }

    @Test
    void accessingOtherRoutes_returns401_withoutJwt() throws Exception {
        webTestClient.get()
                .uri("/api/v1/orders")
                .exchange()
                .expectStatus().isUnauthorized()
        ;
    }

    @Test
    void accessingOtherRoutes_returns401_withExpiredJwt() throws Exception {
        webTestClient.get()
                .uri("/api/v1/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + EXPIRED_TOKEN)
                .exchange()
                .expectStatus().isUnauthorized()
        ;
    }
}
