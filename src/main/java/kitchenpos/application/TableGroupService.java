package kitchenpos.application;

import kitchenpos.dao.*;
import kitchenpos.domain.OrderStatus;
import kitchenpos.domain.OrderTable;
import kitchenpos.domain.TableGroup;
import kitchenpos.dto.TableGroupRequest;
import kitchenpos.dto.TableGroupResponse;
import kitchenpos.exception.KitchenposException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static kitchenpos.exception.KitchenposException.*;

@Service
public class TableGroupService {
    private final JpaOrderDao orderDao;
    private final JpaOrderTableDao orderTableDao;
    private final JpaTableGroupDao tableGroupDao;

    public TableGroupService(final JpaOrderDao orderDao, final JpaOrderTableDao orderTableDao, final JpaTableGroupDao tableGroupDao) {
        this.orderDao = orderDao;
        this.orderTableDao = orderTableDao;
        this.tableGroupDao = tableGroupDao;
    }

    @Transactional
    public TableGroupResponse create(final TableGroupRequest tableGroupRequest) {
        TableGroup tableGroup = TableGroup.of(tableGroupRequest);

        final List<OrderTable> orderTables = tableGroup.getOrderTables();

        if (CollectionUtils.isEmpty(orderTables) || orderTables.size() < 2) {
            throw new KitchenposException(KitchenposException.ILLEGAL_TABLE_SIZE_MINIMUM);
        }

        final List<Long> orderTableIds = orderTables.stream()
                .map(OrderTable::getId)
                .collect(Collectors.toList());

        final List<OrderTable> savedOrderTables = orderTableDao.findAllByIdIn(orderTableIds);

        if (orderTables.size() != savedOrderTables.size()) {
            throw new KitchenposException(ILLEGAL_TABLE_SIZE);
        }

        for (final OrderTable savedOrderTable : savedOrderTables) {
            if (!savedOrderTable.isEmpty()) {
                throw new KitchenposException(NOT_EMPTY_TABLE_TO_CREATE);
            }
        }
        tableGroup.tableCreatedAt(LocalDateTime.now());

        final TableGroup savedTableGroup = tableGroupDao.save(tableGroup);

        for (OrderTable savedOrderTable : savedOrderTables) {
            savedOrderTable = new OrderTable(savedOrderTable.getNumberOfGuests(), savedOrderTable.isEmpty());
            orderTableDao.save(savedOrderTable);
        }
        savedTableGroup.addAllOrderTables(savedOrderTables);

        return TableGroupResponse.of(savedTableGroup);
    }

    @Transactional
    public void ungroup(final Long tableGroupId) {
        TableGroup tableGroup = tableGroupDao.findById(tableGroupId)
                .orElseThrow(() -> new KitchenposException("12312"));
        final List<OrderTable> orderTables = tableGroup.getOrderTables();

        final List<Long> orderTableIds = orderTables.stream()
                .map(OrderTable::getId)
                .collect(Collectors.toList());

        if (orderDao.existsByOrderTable_IdInAndOrderStatusIn(
                orderTableIds, Arrays.asList(OrderStatus.COOKING.name(), OrderStatus.MEAL.name()))) {
            throw new KitchenposException(IMPOSSIBLE_TABLE_STATUS);
        }

        for (final OrderTable orderTable : orderTables) {
            orderTableDao.save(orderTable);
        }
    }
}
