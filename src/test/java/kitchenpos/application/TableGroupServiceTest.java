package kitchenpos.application;

import kitchenpos.dao.JpaOrderDao;
import kitchenpos.dao.JpaOrderTableDao;
import kitchenpos.dao.JpaTableGroupDao;
import kitchenpos.domain.OrderTable;
import kitchenpos.domain.TableGroup;
import kitchenpos.exception.KitchenposException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static kitchenpos.exception.KitchenposException.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class TableGroupServiceTest extends ServiceTest {
    @InjectMocks
    private TableGroupService tableGroupService;

    @Mock
    private JpaOrderDao orderDao;

    @Mock
    private JpaOrderTableDao orderTableDao;

    @Mock
    private JpaTableGroupDao tableGroupDao;

    @Test
    @DisplayName("테이블 그룹을 생성한다.")
    void create() {
        for (OrderTable orderTable : tableGroup.getOrderTables()) {
            orderTable.makeEmpty(true);
        }

        when(orderTableDao.findAllByIdIn(anyList()))
                .thenReturn(tableGroup.getOrderTables());
        when(tableGroupDao.save(any(TableGroup.class)))
                .thenReturn(tableGroup);

        TableGroup actual = tableGroupService.create(tableGroup);
        assertThat(actual.getId()).isNotNull();
        assertThat(actual).usingRecursiveComparison()
                .isEqualTo(tableGroup);
    }

    @Test
    @DisplayName("테이블 그룹 내 테이블이 비어있거나 2개 미만이면 에러가 발생한다.")
    void createExceptionTableLessThanTwo() {
        tableGroup.addAllOrderTables(Collections.emptyList());
        assertThatThrownBy(() -> tableGroupService.create(tableGroup))
                .isInstanceOf(KitchenposException.class)
                .hasMessage(ILLEGAL_TABLE_SIZE_MINIMUM);

        tableGroup.addAllOrderTables(Collections.singletonList(orderTable));
        assertThatThrownBy(() -> tableGroupService.create(tableGroup))
                .isInstanceOf(KitchenposException.class)
                .hasMessage(ILLEGAL_TABLE_SIZE_MINIMUM);
    }

    @Test
    @DisplayName("테이블 그룹 내 테이블의 개수가 저장된 개수와 맞지 않으면 에러가 발생한다.")
    void createExceptionTableSize() {
        List<OrderTable> anotherOrderTables = new ArrayList<>();
        anotherOrderTables.add(orderTable);
        anotherOrderTables.add(orderTable2);
        anotherOrderTables.add(orderTable2);
        when(orderTableDao.findAllByIdIn(anyList()))
                .thenReturn(anotherOrderTables);

        assertThatThrownBy(() -> tableGroupService.create(tableGroup))
                .isInstanceOf(KitchenposException.class)
                .hasMessage(ILLEGAL_TABLE_SIZE);
    }

    @Test
    @DisplayName("테이블 그룹 내 테이블이 비어있지 않으면 에러가 발생한다.")
    void createExceptionTableEmpty() {
        orderTable.makeEmpty(false);
        when(orderTableDao.findAllByIdIn(anyList()))
                .thenReturn(tableGroup.getOrderTables());

        assertThatThrownBy(() -> tableGroupService.create(tableGroup))
                .isInstanceOf(KitchenposException.class)
                .hasMessage(NOT_EMPTY_TABLE_TO_CREATE);
    }

    @Test
    @DisplayName("테이블 그룹 내 테이블이 그룹에 속해있으면 에러가 발생한다.")
    void createExceptionTableInGroup() {
        when(orderTableDao.findAllByIdIn(anyList()))
                .thenReturn(tableGroup.getOrderTables());

        assertThatThrownBy(() -> tableGroupService.create(tableGroup))
                .isInstanceOf(KitchenposException.class)
                .hasMessage(NOT_EMPTY_TABLE_TO_CREATE);
    }

    @Test
    @DisplayName("테이블 그룹을 해제하고 테이블을 비운다.")
    void ungroup() {
        when(orderTableDao.findAllByTableGroup_Id(anyLong()))
                .thenReturn(tableGroup.getOrderTables());
        when(orderDao.existsByOrderTableIdInAndOrderStatusIn(anyList(), anyList()))
                .thenReturn(false);

        tableGroupService.ungroup(1L);
        assertThat(orderTable.isEmpty()).isFalse();
        assertThat(orderTable2.isEmpty()).isFalse();
    }

    @Test
    @DisplayName("식사가 완료되지 않았거나 주문이 있는 테이블은 에러가 발생한다.")
    void ungroupExceptionStatus() {
        when(orderTableDao.findAllByTableGroup_Id(anyLong()))
                .thenReturn(tableGroup.getOrderTables());
        when(orderDao.existsByOrderTableIdInAndOrderStatusIn(anyList(), anyList()))
                .thenReturn(true);

        assertThatThrownBy(() -> tableGroupService.ungroup(1L))
                .isInstanceOf(KitchenposException.class)
                .hasMessage(IMPOSSIBLE_TABLE_STATUS);
    }
}
