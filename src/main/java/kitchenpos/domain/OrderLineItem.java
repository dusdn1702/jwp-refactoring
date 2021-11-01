package kitchenpos.domain;

import javax.persistence.*;

@Entity
@Table(name = "order_line_item")
public class OrderLineItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;

    @ManyToOne
    @JoinColumn(name = "order_id", foreignKey = @ForeignKey(name = "fk_order_line_item_orders"))
    private Order order;

    @ManyToOne
    @JoinColumn(name = "menu_id", foreignKey = @ForeignKey(name = "fk_order_line_item_menu"))
    private Menu menu;

    @Column(nullable = false)
    private long quantity;

    public Long getSeq() {
        return seq;
    }

    public void setSeq(final Long seq) {
        this.seq = seq;
    }

    public Long getOrderId() {
        return order.getId();
    }

    public void setOrderId(final Long orderId) {
        this.order = new Order();
        this.order.setId(orderId);
    }

    public Long getMenuId() {
        return menu.getId();
    }

    public void setMenuId(final Long menuId) {
        this.menu.setId(menuId);
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(final long quantity) {
        this.quantity = quantity;
    }
}
