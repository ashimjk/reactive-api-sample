package com.ashimjk.reactiveapi.product.reactive;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class EndpointRoutes {

    @Bean
    RouterFunction<ServerResponse> routes(ProductHandler handler) {
        // Approach - 1
        // return route()
        //         .GET("/routes/products/events", accept(TEXT_EVENT_STREAM), handler::getProductEvents)
        //         .GET("/routes/products/{id}", accept(APPLICATION_JSON), handler::getProduct)
        //         .GET("/routes/products", accept(APPLICATION_JSON), handler::getAllProducts)
        //         .POST("/routes/products", accept(APPLICATION_JSON), handler::saveProduct)
        //         .PUT("/routes/products/{id}", accept(APPLICATION_JSON), handler::updateProduct)
        //         .DELETE("/routes/products/{id}", accept(APPLICATION_JSON), handler::deleteProduct)
        //         .DELETE("/routes/products", accept(APPLICATION_JSON), handler::deleteAllProducts)
        //         .build();

        // Approach - 2
        return route()
                .path("/routes/products",
                      builder ->
                              builder.nest(
                                             accept(APPLICATION_JSON)
                                                     .or(contentType(APPLICATION_JSON))
                                                     .or(accept(TEXT_EVENT_STREAM)),
                                             nestedBuilder ->
                                                     nestedBuilder
                                                             .GET("/events", handler::getProductEvents)
                                                             .GET("{id}", handler::getProduct)
                                                             .GET(handler::getAllProducts)
                                                             .PUT("{id}", handler::updateProduct)
                                                             .POST(handler::saveProduct)
                                     )
                                     .DELETE("{id}", handler::deleteProduct)
                                     .DELETE(handler::deleteAllProducts)
                ).build();

    }

}
