package kitchenpos.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProductTest {
    @Test
    void calculateTotal() {
        int price = 1000;
        Product product = new Product("상품", BigDecimal.valueOf(price));

        int quantity = 10;
        BigDecimal result = product.calculateTotal(quantity);

        assertThat(result.intValue()).isEqualTo(price * quantity);
    }
}
