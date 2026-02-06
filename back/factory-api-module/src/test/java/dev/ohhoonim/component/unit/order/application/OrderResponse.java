package dev.ohhoonim.component.unit.order.application;

import dev.ohhoonim.component.unit.order.model.Order;

public record OrderResponse(String orderId, String status, long totalAmount) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(order.getId().toValue(), order.getStatus().name(), 
                order.getTotalAmount());
    }
}
