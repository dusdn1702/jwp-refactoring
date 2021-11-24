package kitchenpos.product.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProductTest {
    @Test
    void calculateTotal() {
        int price = 1000;
        kitchenpos.product.domain.Product product = new kitchenpos.product.domain.Product("상품", BigDecimal.valueOf(price));

        int quantity = 10;
        BigDecimal result = product.calculateTotal(quantity);

        assertThat(result.intValue()).isEqualTo(price * quantity);
    }
}
