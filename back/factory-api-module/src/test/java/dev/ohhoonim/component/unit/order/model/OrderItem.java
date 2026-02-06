package dev.ohhoonim.component.unit.order.model;

public record OrderItem(String productId, long price, int quantity) {
    public long getSubTotal() {
        return price * quantity;
    }
}