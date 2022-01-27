package com.ashimjk.reactiveapi.product.model;

import lombok.*;

@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ProductEvent {

    private String eventId;
    private String eventType;

}
