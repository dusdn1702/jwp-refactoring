package kitchenpos.order.dto;

import kitchenpos.order.domain.OrderLineItem;

public class OrderLineItemRequest {
    private long quantity;
    private long menuId;

    public OrderLineItemRequest() {
    }

    public OrderLineItemRequest(long quantity, long menuId) {
        this.quantity = quantity;
        this.menuId = menuId;
    }

    public static OrderLineItemRequest of(OrderLineItem orderLineItem) {
        return new OrderLineItemRequest(
                orderLineItem.getQuantity(),
                orderLineItem.getMenuId()
        );
    }

    public long getQuantity() {
        return quantity;
    }

    public long getMenuId() {
        return menuId;
    }
}
