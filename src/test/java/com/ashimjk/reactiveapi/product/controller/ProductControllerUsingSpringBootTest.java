package com.ashimjk.reactiveapi.product.controller;

import com.ashimjk.reactiveapi.product.model.Product;
import com.ashimjk.reactiveapi.product.model.ProductEvent;
import com.ashimjk.reactiveapi.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest
class ProductControllerUsingSpringBootTest {

    private WebTestClient client;

    private List<Product> expectedList;

    @Autowired private ProductRepository repository;

    @BeforeEach
    void setup() {
        client = WebTestClient
                .bindToController(new ProductController(repository))
                .configureClient()
                .baseUrl("/controller/products")
                .build();

        expectedList = repository.findAll().collectList().block();
    }

    @Test
    void shouldGetAllProducts() {
        client.get()
              .uri("/")
              .accept(APPLICATION_JSON)
              .exchange()
              .expectStatus().isOk()
              .expectBodyList(Product.class)
              .isEqualTo(expectedList);
    }

    @Test
    void shouldGetProductById() {
        Product expectedProduct = expectedList.get(0);

        client.get()
              .uri("/{id}", expectedProduct.getId())
              .accept(APPLICATION_JSON)
              .exchange()
              .expectStatus().isOk()
              .expectBody(Product.class)
              .isEqualTo(expectedProduct);
    }

    @Test
    void shouldReturnNotFoundStatus_forGetProductById() {
        client.get()
              .uri("/{id}", 1)
              .exchange()
              .expectStatus().isNotFound();
    }

    @Test
    void shouldGetProductEvents() {
        ProductEvent expectedEvent = new ProductEvent("0", "Product Event");

        FluxExchangeResult<ProductEvent> result =
                client.get()
                      .uri("/events")
                      .accept(MediaType.TEXT_EVENT_STREAM)
                      .exchange()
                      .expectStatus().isOk()
                      .returnResult(ProductEvent.class);

        StepVerifier.create(result.getResponseBody())
                    .expectNext(expectedEvent)
                    .expectNextCount(2)
                    .consumeNextWith(event -> assertEquals("3", event.getEventId()))
                    .thenCancel()
                    .verify();
    }

}