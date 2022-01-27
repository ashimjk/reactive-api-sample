package com.ashimjk.reactiveapi.product.repository;

import com.ashimjk.reactiveapi.product.model.Product;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends ReactiveMongoRepository<Product, String> {
}
