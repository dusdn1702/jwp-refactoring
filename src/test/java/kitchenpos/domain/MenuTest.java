package kitchenpos.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MenuTest {
    @ParameterizedTest
    @ValueSource(ints = {0, 100, 1000000})
    void isInvalidPrice(int price) {
        Menu menu = new Menu(1L, "자장면", BigDecimal.valueOf(price), null, null);
        assertThat(menu.isInvalidPrice()).isFalse();
    }

    @Test
    void isValidPrice() {
        Menu menu1 = new Menu(1L, "자장면", null, null, null);
        assertThat(menu1.isInvalidPrice()).isTrue();

        Menu menu2 = new Menu(1L, "자장면", BigDecimal.valueOf(-1000), null, null);
        assertThat(menu2.isInvalidPrice()).isTrue();
    }

    @Test
    void isPriceCheaperThan() {
        Menu menu = new Menu(1L, "자장면", BigDecimal.valueOf(1000), null, null);
        boolean expensive = menu.isPriceCheaperThan(BigDecimal.valueOf(1001));
        boolean cheap = menu.isPriceCheaperThan(BigDecimal.valueOf(999));

        assertThat(expensive).isFalse();
        assertThat(cheap).isTrue();
    }


}
