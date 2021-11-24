package kitchenpos.product.domain;

import kitchenpos.table.domain.OrderTable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderTableTest {
    @Test
    void makeEmpty() {
        OrderTable orderTable = new OrderTable(3, false);

        orderTable.makeEmpty(true);

        assertThat(orderTable.isEmpty()).isTrue();
    }

    @Test
    void changeNumberOfGuests() {
        OrderTable orderTable = new OrderTable(3, false);
        OrderTable changedOrderTable = new OrderTable(4, false);

        orderTable.changeNumberOfGuests(changedOrderTable);

        assertThat(orderTable.getNumberOfGuests()).isEqualTo(changedOrderTable.getNumberOfGuests());
    }

    @Test
    void isValidGuestNumbers() {
        OrderTable orderTable = new OrderTable(3, false);

        assertThat(orderTable.isInvalidGuestNumbers()).isFalse();
    }

    @Test
    void isInvalidGuestNumbers() {
        OrderTable orderTable = new OrderTable(-1, false);

        assertThat(orderTable.isInvalidGuestNumbers()).isTrue();
    }
}
