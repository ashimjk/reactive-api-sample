package com.ashimjk.reactiveapi;

import com.ashimjk.reactiveapi.product.model.Product;
import com.ashimjk.reactiveapi.product.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class ReactiveApiSampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReactiveApiSampleApplication.class, args);
    }

    @Bean
    CommandLineRunner init(ReactiveMongoOperations operations, ProductRepository productRepository) {
        return args -> {
            Flux<Product> productFlux =
                    Flux.just(
                                new Product(null, "Big Latte", 2.99),
                                new Product(null, "Big Decaf", 2.49),
                                new Product(null, "Green Tea", 1.99)
                        )
                        .flatMap(productRepository::save);

            operations.collectionExists(Product.class)
                      .flatMap(exists -> exists
                              ? operations.dropCollection(Product.class)
                              : operations.createCollection(Product.class))
                      .thenMany(productFlux)
                      .thenMany(productRepository.findAll())
                      .subscribe();
        };
    }

}
