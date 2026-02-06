package dev.ohhoonim.component.payload;

import dev.ohhoonim.component.unit.EntityId;

public record Page(
		Integer totalCount,
		Integer limit,
		EntityId lastSeenKey) implements Payload{

	public Page {
		if (limit == null || limit.equals(0)) {
			limit = 10;
		}
	}

	public Page() {
		this(null, null, null);
	}

	public Page(EntityId lastSeenKey) {
		this(null, 20, lastSeenKey);
	}

}