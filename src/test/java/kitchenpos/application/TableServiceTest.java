package kitchenpos.application;

import kitchenpos.dao.JpaOrderDao;
import kitchenpos.dao.JpaOrderTableDao;
import kitchenpos.domain.OrderTable;
import kitchenpos.domain.TableGroup;
import kitchenpos.exception.KitchenposException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static kitchenpos.exception.KitchenposException.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TableServiceTest extends ServiceTest{
    @InjectMocks
    private TableService tableService;

    @Mock
    private JpaOrderDao orderDao;

    @Mock
    private JpaOrderTableDao orderTableDao;

    @BeforeEach
    void setUp() {
        orderTable = new OrderTable(1L, 3, false);
        orderTable2 = new OrderTable(1L, 6, true);
    }

    @Test
    @DisplayName("주문 테이블을 생성하면 id와 테이블그룹이 연결되지 않은 테이블이 만들어진다.")
    void create() {
        when(orderTableDao.save(any(OrderTable.class)))
                .thenReturn(orderTable);
        OrderTable actual = tableService.create(orderTable);

        assertThat(actual.getId()).isNotNull();
        assertThat(actual).usingRecursiveComparison()
                .isEqualTo(orderTable);
    }

    @Test
    @DisplayName("모든 주문 테이블을 조회한다.")
    void list() {
        orderTable2 = new OrderTable(null, 0, true);

        List<OrderTable> orderTables = new ArrayList<>();
        orderTables.add(orderTable);
        orderTables.add(orderTable2);

        when(orderTableDao.findAll())
                .thenReturn(orderTables);

        List<OrderTable> actual = tableService.list();
        assertThat(actual).hasSize(2);
        assertThat(actual).usingRecursiveComparison()
                .isEqualTo(orderTables);
    }

    @Test
    @DisplayName("주문 테이블을 빈 테이블로 전환한다.")
    void changeEmpty() {
        when(orderTableDao.findById(anyLong()))
                .thenReturn(Optional.of(orderTable));
        when(orderDao.existsByOrderTable_IdAndOrderStatusIn(anyLong(), anyList()))
                .thenReturn(false);
        when(orderTableDao.save(any(OrderTable.class)))
                .thenReturn(orderTable2);

        assertThat(orderTable.isEmpty()).isFalse();
        OrderTable actual = tableService.changeEmpty(1L, orderTable2);
        assertThat(actual.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("주문테이블이 존재하지 않으면 예외가 발생한다.")
    void changeEmptyExceptionTable() {
        when(orderTableDao.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> tableService.changeEmpty(1L, orderTable2))
                .isInstanceOf(KitchenposException.class)
                .hasMessage(ILLEGAL_ORDER_TABLE_ID);
    }

    @Test
    @DisplayName("요리중이거나 식사중인 테이블은 비울 수 없다.")
    void changeEmptyExceptionStatus() {
        when(orderTableDao.findById(anyLong()))
                .thenReturn(Optional.of(orderTable));
        when(orderDao.existsByOrderTable_IdAndOrderStatusIn(anyLong(), anyList()))
                .thenReturn(true);

        assertThatThrownBy(() -> tableService.changeEmpty(1L, orderTable2))
                .isInstanceOf(KitchenposException.class)
                .hasMessage(IMPOSSIBLE_TABLE_STATUS);
    }

    @Test
    @DisplayName("주문 테이블의 손님 수를 변경한다.")
    void changeNumberOfGuests() {
        when(orderTableDao.findById(anyLong()))
                .thenReturn(Optional.of(orderTable));
        assertThat(orderTable.getNumberOfGuests()).isEqualTo(3);

        orderTable.changeNumberOfGuests(orderTable2.getNumberOfGuests());
        when(orderTableDao.save(any(OrderTable.class)))
                .thenReturn(orderTable);
        OrderTable actual = tableService.changeEmpty(1L, orderTable2);
        assertThat(actual.getNumberOfGuests()).isEqualTo(6);
    }

    @Test
    @DisplayName("주문 테이블의 손님 수가 0보다 작으면 에러가 발생한다.")
    void changeNumberOfGuestsExceptionNegative() {
        orderTable.changeNumberOfGuests(-1);
        assertThatThrownBy(() -> tableService.changeNumberOfGuests(1L, orderTable))
                .isInstanceOf(KitchenposException.class)
                .hasMessage(IMPOSSIBLE_NUMBER_OF_GUESTS);
    }

    @Test
    @DisplayName("손님 수 변경 시 주문 테이블이 존재하지 않으면 에러가 발생한다.")
    void changeNumberOfGuestsExceptionIllegalTable() {
        when(orderTableDao.findById(anyLong()))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> tableService.changeNumberOfGuests(1L, orderTable2))
                .isInstanceOf(KitchenposException.class)
                .hasMessage(ILLEGAL_ORDER_TABLE_ID);
    }

    @Test
    @DisplayName("손님 수 변경 시 주문 테이블이 비어있으면 에러가 발생한다.")
    void changeNumberOfGuestsExceptionEmptyTable() {
        orderTable.makeEmpty(true);
        when(orderTableDao.findById(anyLong()))
                .thenReturn(Optional.of(orderTable));
        assertThatThrownBy(() -> tableService.changeNumberOfGuests(1L, orderTable2))
                .isInstanceOf(KitchenposException.class)
                .hasMessage(EMPTY_ORDER_TABLE);
    }
}
