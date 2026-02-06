package dev.ohhoonim.component.unit.order.port;

import java.util.Optional;
import dev.ohhoonim.component.unit.order.model.ProductVo;

public interface ProductPort {

    Optional<ProductVo> findById(String productId);

}
