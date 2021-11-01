package kitchenpos.domain;

import javax.persistence.*;

@Entity
@Table(name = "menu_product")
public class MenuProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;

    @ManyToOne
    @JoinColumn(name = "menu_id", foreignKey = @ForeignKey(name = "fk_menu_product_menu"))
    private Menu menu;

    @ManyToOne
    @JoinColumn(name = "product_id", foreignKey = @ForeignKey(name = "fk_menu_product_product"))
    private Product product;

    @Column(nullable = false)
    private long quantity;

    public Long getSeq() {
        return seq;
    }

    public void setSeq(final Long seq) {
        this.seq = seq;
    }

    public Long getMenuId() {
        return menu.getId();
    }

    public void setMenuId(final Long menuId) {
        this.menu.setId(menuId);
    }

    public Long getProductId() {
        return product.getId();
    }

    public void setProductId(final Long productId) {
        this.product.setId(productId);
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(final long quantity) {
        this.quantity = quantity;
    }
}
