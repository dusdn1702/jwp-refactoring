package kitchenpos.product.dto;

import kitchenpos.product.domain.Product;

import java.math.BigDecimal;

public class ProductResponse {
    private long id;
    private String name;
    private BigDecimal price;

    public ProductResponse() {
    }

    public ProductResponse(long id, String name, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public static ProductResponse of(Product savedProduct) {
        return new ProductResponse(savedProduct.getId(), savedProduct.getName(), savedProduct.getPrice());
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
