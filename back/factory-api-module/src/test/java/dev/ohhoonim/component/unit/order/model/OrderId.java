package dev.ohhoonim.component.unit.order.model;

import com.github.f4b6a3.ulid.Ulid;
import com.github.f4b6a3.ulid.UlidCreator;
import dev.ohhoonim.component.unit.EntityId;
import dev.ohhoonim.component.unit.EntityId.Creator;

public record OrderId(String value) implements EntityId {

    public OrderId {
        if (value == null || !Ulid.isValid(value)) {
            throw new IllegalArgumentException("올바른 OrderId 형식이 아닙니다.");
        }
    }

    public static final Creator<OrderId> Creator = new Creator<>() {
        @Override
        public OrderId from(String value) {
            return new OrderId(value);
        }

        @Override
        public OrderId generate() {
            return new OrderId(UlidCreator.getUlid().toString());
        }
    };

    @Override
    public String getRawValue() {
        return value;
    }
}