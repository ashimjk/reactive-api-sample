package com.ashimjk.reactiveapi.product.controller;

import com.ashimjk.reactiveapi.product.model.Product;
import com.ashimjk.reactiveapi.product.model.ProductEvent;
import com.ashimjk.reactiveapi.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/controller/products")
public class ProductController {

    private final ProductRepository productRepository;

    @GetMapping
    public Flux<Product> products() {
        return productRepository.findAll();
    }

    @GetMapping("{id}")
    public Mono<ResponseEntity<Product>> productById(@PathVariable String id) {
        return productRepository
                .findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Product> createProduct(@RequestBody Product product) {
        return productRepository.save(product);
    }

    @PutMapping("{id}")
    public Mono<ResponseEntity<Product>> updateProduct(
            @PathVariable String id,
            @RequestBody Product product
    ) {
        return productRepository
                .findById(id)
                .flatMap(existingProduct -> {
                    existingProduct.setName(product.getName());
                    existingProduct.setPrice(product.getPrice());
                    return productRepository.save(existingProduct);
                })
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("{id}")
    public Mono<ResponseEntity<Void>> deleteProduct(@PathVariable String id) {
        return productRepository
                .findById(id)
                .flatMap(p -> productRepository.deleteById(p.getId()))
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .defaultIfEmpty(ResponseEntity.noContent().build());
    }

    @DeleteMapping
    public Mono<Void> deleteAllProduct() {
        return productRepository.deleteAll();
    }

    @GetMapping(value = "events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ProductEvent> events() {
        return Flux.interval(Duration.ofSeconds(1))
                   .map(val -> new ProductEvent(val.toString(), "Product Event"));
    }

}
