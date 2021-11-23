package kitchenpos.domain;

import java.util.List;
import java.util.stream.Collectors;

public class OrderLineItems {
    private final List<OrderLineItem> orderLineItems;

    public OrderLineItems(List<OrderLineItem> orderLineItems) {
        this.orderLineItems = orderLineItems;
    }

    public boolean isEmpty() {
        return orderLineItems.isEmpty();
    }

    public List<Long> menuIdsFromOrderLineItem() {
        return orderLineItems.stream()
                .map(OrderLineItem::getMenuId)
                .collect(Collectors.toList());
    }

    public boolean isNotSameSize(long size) {
        return orderLineItems.size() != size;
    }

    public List<OrderLineItem> getOrderLineItems() {
        return orderLineItems;
    }
}
