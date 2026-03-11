// src/main/java/com/example/order/shared/exceptions/OrderNotFoundException.java
package com.example.order.shared.exceptions;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(String message) {
        super(message);
    }
}