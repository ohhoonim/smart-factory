package dev.ohhoonim.component.unit.order.application;

public interface OrderPlaceActivity {
    OrderResponse execute(OrderPlaceRequest request, String operator);
}
