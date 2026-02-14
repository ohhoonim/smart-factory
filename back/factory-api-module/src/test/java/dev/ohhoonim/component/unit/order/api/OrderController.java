package dev.ohhoonim.component.unit.order.api;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import dev.ohhoonim.component.unit.order.application.OrderPlaceRequest;
import dev.ohhoonim.component.unit.order.application.OrderResponse;
import dev.ohhoonim.component.unit.order.application.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 주문 생성 API
     */
    @PostMapping
    public OrderResponse placeOrder(
            @RequestBody @Valid OrderPlaceRequest request,
            @RequestHeader("X-User-Id") String operator
    ) {
        return orderService.place(request, operator);
    }

    /**
     * 주문 확정 API
     */
    @PatchMapping("/{orderId}/confirm")
    public void confirmOrder(
            @PathVariable String orderId,
            @RequestHeader("X-User-Id") String operator
    ) {
        orderService.confirm(orderId, operator);
    }
}
