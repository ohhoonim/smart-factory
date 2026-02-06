package dev.ohhoonim.component.unit.order.infra;

import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import dev.ohhoonim.component.unit.order.model.ProductVo;
import dev.ohhoonim.component.unit.order.port.ProductPort;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductJdbcAdapter implements ProductPort {
    private final JdbcClient jdbcClient;

    @Override
    public Optional<ProductVo> findById(String productId) {
        String sql = "SELECT id, name, price FROM products WHERE id = :id";

        return jdbcClient.sql(sql).param("id", productId)
                .query((rs, rowNum) -> new ProductVo(rs.getString("id"), rs.getString("name"),
                        rs.getLong("price")))
                .optional();
    }
}
