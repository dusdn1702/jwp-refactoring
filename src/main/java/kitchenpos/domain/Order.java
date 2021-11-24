package kitchenpos.domain;

import kitchenpos.dto.OrderResponse;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_table_id", foreignKey = @ForeignKey(name = "fk_orders_order_table"))
    private OrderTable orderTable;

    @Column(nullable = false)
    private String orderStatus;

    @Column(nullable = false)
    private LocalDateTime orderedTime;

    @OneToMany(mappedBy = "order")
    private List<OrderLineItem> orderLineItems;

    protected Order() {
    }

    public Order(OrderTable orderTable, String orderStatus, LocalDateTime orderedTime) {
        this.orderTable = orderTable;
        this.orderStatus = orderStatus;
        this.orderedTime = orderedTime;
    }

    public Order(Long id, OrderTable orderTable, String orderStatus, LocalDateTime orderedTime) {
        this(orderTable, orderStatus, orderedTime);
        this.id = id;
    }

    public static Order of(OrderResponse orderResponse) {
        OrderTable orderTable = OrderTable.of(orderResponse.getOrderTableResponse());
        return new Order(orderResponse.getId(), orderTable, orderResponse.getOrderStatus(), orderResponse.getOrderedTime());
    }

    public void addAllOrderLineItems(OrderLineItems orderLineItems) {
        this.orderLineItems = orderLineItems.getLineItems();
    }

    public void changeOrderStatus(String statusName) {
        this.orderStatus = statusName;
    }

    public void makeOrderIn(OrderStatus orderStatus, LocalDateTime orderedTime) {
        this.orderStatus = orderStatus.name();
        this.orderedTime = orderedTime;
    }

    public boolean sameOrderStatus(OrderStatus status) {
        return orderStatus.equals(status.name());
    }

    public Long getId() {
        return id;
    }

    public OrderTable getOrderTable() {
        return orderTable;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public LocalDateTime getOrderedTime() {
        return orderedTime;
    }

    public List<OrderLineItem> getOrderLineItems() {
        return orderLineItems;
    }

    public long getOrderTableId() {
        return orderTable.getId();
    }
}
