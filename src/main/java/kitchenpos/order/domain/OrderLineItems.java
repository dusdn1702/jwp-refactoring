package kitchenpos.order.domain;

import java.util.List;
import java.util.stream.Collectors;

public class OrderLineItems {
    private final List<OrderLineItem> lineItems;

    public OrderLineItems(List<OrderLineItem> lineItems) {
        this.lineItems = lineItems;
    }

    public boolean isEmpty() {
        return lineItems.isEmpty();
    }

    public List<Long> menuIdsFromOrderLineItem() {
        return lineItems.stream()
                .map(OrderLineItem::getMenuId)
                .collect(Collectors.toList());
    }

    public boolean isNotSameSize(long size) {
        return lineItems.size() != size;
    }

    public List<OrderLineItem> getLineItems() {
        return lineItems;
    }
}
