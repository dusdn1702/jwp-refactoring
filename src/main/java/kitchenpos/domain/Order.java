package kitchenpos.domain;

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

    public void addAllOrderLineItems(List<OrderLineItem> orderLineItems) {
        this.orderLineItems = orderLineItems;
    }

    public void changeOrderStatus(String statusName) {
        this.orderStatus = statusName;
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

    public void makeOrderIn(OrderTable orderTable, OrderStatus orderStatus, LocalDateTime orderedTime) {
        this.orderTable = orderTable;
        this.orderStatus = orderStatus.name();
        this.orderedTime = orderedTime;
    }
}
