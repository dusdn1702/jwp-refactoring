package kitchenpos.order.dto;

import kitchenpos.order.domain.Order;
import kitchenpos.table.dto.OrderTableResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class OrderResponse {
    private Long id;
    private OrderTableResponse orderTableResponse;
    private String orderStatus;
    private LocalDateTime orderedTime;
    private List<OrderLineItemResponse> orderLineItemResponses;

    public OrderResponse() {
    }

    public OrderResponse(Long id, OrderTableResponse orderTableResponse, String orderStatus, LocalDateTime orderedTime, List<OrderLineItemResponse> orderLineItemResponses) {
        this.id = id;
        this.orderTableResponse = orderTableResponse;
        this.orderStatus = orderStatus;
        this.orderedTime = orderedTime;
        this.orderLineItemResponses = orderLineItemResponses;
    }

    public static OrderResponse of(Order order) {
        OrderTableResponse orderTableResponse = OrderTableResponse.of(order.getOrderTable());
        List<OrderLineItemResponse> orderLineItemResponses = order.getOrderLineItems().stream()
                .map(orderLineItem -> new OrderLineItemResponse(orderLineItem.getSeq(), orderLineItem.getQuantity()))
                .collect(Collectors.toList());
        return new OrderResponse(order.getId(), orderTableResponse, order.getOrderStatus(), order.getOrderedTime(), orderLineItemResponses);
    }

    public Long getId() {
        return id;
    }

    public OrderTableResponse getOrderTableResponse() {
        return orderTableResponse;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public LocalDateTime getOrderedTime() {
        return orderedTime;
    }

    public List<OrderLineItemResponse> getOrderLineItemResponses() {
        return orderLineItemResponses;
    }
}
