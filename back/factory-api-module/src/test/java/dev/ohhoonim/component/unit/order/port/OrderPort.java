package dev.ohhoonim.component.unit.order.port;

import java.util.Optional;
import dev.ohhoonim.component.unit.order.model.Order;
import dev.ohhoonim.component.unit.order.model.OrderId;

public interface OrderPort {

    void save(Order order);

    Optional<Order> findById(OrderId id);

}
