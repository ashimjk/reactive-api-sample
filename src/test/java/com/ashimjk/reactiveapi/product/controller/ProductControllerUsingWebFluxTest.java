package com.ashimjk.reactiveapi.product.controller;

import com.ashimjk.reactiveapi.product.model.Product;
import com.ashimjk.reactiveapi.product.model.ProductEvent;
import com.ashimjk.reactiveapi.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@WebFluxTest(ProductController.class)
class ProductControllerUsingWebFluxTest {

    @Autowired private WebTestClient client;
    @MockBean private ProductRepository repository;
    @MockBean private CommandLineRunner commandLineRunner;

    private List<Product> expectedList;

    @BeforeEach
    void setup() {
        this.expectedList = List.of(new Product("1", "Big Latte", 2.99));
    }

    @Test
    void shouldGetAllProducts() {
        when(repository.findAll()).thenReturn(Flux.fromIterable(this.expectedList));

        client.get()
              .uri("/controller/products/")
              .accept(APPLICATION_JSON)
              .exchange()
              .expectStatus().isOk()
              .expectBodyList(Product.class)
              .isEqualTo(expectedList);
    }

    @Test
    void shouldGetProductById() {
        Product expectedProduct = this.expectedList.get(0);
        when(repository.findById(expectedProduct.getId())).thenReturn(Mono.just(expectedProduct));

        client.get()
              .uri("/controller/products/{id}", expectedProduct.getId())
              .accept(APPLICATION_JSON)
              .exchange()
              .expectStatus().isOk()
              .expectBody(Product.class)
              .isEqualTo(expectedProduct);
    }

    @Test
    void shouldReturnNotFoundStatus_forGetProductById() {
        String id = "aaa";
        when(repository.findById(id)).thenReturn(Mono.empty());

        client.get()
              .uri("/controller/products/{id}", id)
              .exchange()
              .expectStatus().isNotFound();
    }

    @Test
    void shouldGetProductEvents() {
        ProductEvent expectedEvent = new ProductEvent("0", "Product Event");

        FluxExchangeResult<ProductEvent> result =
                client.get()
                      .uri("/controller/products/events")
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