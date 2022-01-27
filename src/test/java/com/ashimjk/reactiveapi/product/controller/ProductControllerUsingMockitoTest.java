package com.ashimjk.reactiveapi.product.controller;

import com.ashimjk.reactiveapi.product.model.Product;
import com.ashimjk.reactiveapi.product.model.ProductEvent;
import com.ashimjk.reactiveapi.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

@ExtendWith(MockitoExtension.class)
class ProductControllerUsingMockitoTest {

    @Mock private ProductRepository repository;

    private WebTestClient client;
    private List<Product> expectedList;

    @BeforeEach
    void setup() {
        client =
                WebTestClient
                        .bindToController(new ProductController(repository))
                        .configureClient()
                        .baseUrl("/controller/products")
                        .build();

        this.expectedList = List.of(new Product("1", "Big Latte", 2.99));
    }

    @Test
    void shouldGetAllProducts() {
        when(repository.findAll()).thenReturn(Flux.fromIterable(this.expectedList));

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
        Product expectedProduct = this.expectedList.get(0);
        when(repository.findById(expectedProduct.getId())).thenReturn(Mono.just(expectedProduct));

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
        String id = "aaa";
        when(repository.findById(id)).thenReturn(Mono.empty());

        client.get()
              .uri("/{id}", id)
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