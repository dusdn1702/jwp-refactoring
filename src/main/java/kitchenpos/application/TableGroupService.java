package kitchenpos.application;

import kitchenpos.dao.JpaOrderDao;
import kitchenpos.dao.JpaOrderTableDao;
import kitchenpos.dao.JpaTableGroupDao;
import kitchenpos.domain.OrderStatus;
import kitchenpos.domain.OrderTable;
import kitchenpos.domain.TableGroup;
import kitchenpos.dto.TableGroupRequest;
import kitchenpos.dto.TableGroupResponse;
import kitchenpos.exception.KitchenposException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

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
        OrderTables orderTables = new OrderTables(tableGroup.getOrderTables());
        OrderTables savedOrderTables = makeValidOrderTables(orderTables);

        tableGroup.tableCreatedAt(LocalDateTime.now());
        TableGroup savedTableGroup = tableGroupDao.save(tableGroup);
        saveTables(savedOrderTables, savedTableGroup);

        return TableGroupResponse.of(savedTableGroup);
    }

    private OrderTables makeValidOrderTables(OrderTables orderTables) {
        if (orderTables.isValidSize()) {
            throw new KitchenposException(KitchenposException.ILLEGAL_TABLE_SIZE_MINIMUM);
        }

        List<Long> orderTableIds = orderTables.getOrderTableIds();
        OrderTables savedOrderTables = new OrderTables(orderTableDao.findAllByIdIn(orderTableIds));

        if (orderTables.isNotSameSize(savedOrderTables)) {
            throw new KitchenposException(ILLEGAL_TABLE_SIZE);
        }

        if (!orderTables.isAllEmpty()) {
            throw new KitchenposException(NOT_EMPTY_TABLE_TO_CREATE);
        }

        return savedOrderTables;
    }

    private void saveTables(OrderTables orderTables, TableGroup tableGroup) {
        List<OrderTable> tables = orderTables.getTables();
        orderTableDao.saveAll(tables);
        tableGroup.addAllOrderTables(tables);
    }

    @Transactional
    public void ungroup(final Long tableGroupId) {
        TableGroup tableGroup = tableGroupDao.findById(tableGroupId)
                .orElseThrow(() -> new KitchenposException(ILLEGAL_TABLE_GROUP_ID));
        OrderTables orderTables = new OrderTables(tableGroup.getOrderTables());
        List<Long> orderTableIds = orderTables.getOrderTableIds();

        if (orderDao.existsByOrderTable_IdInAndOrderStatusIn(
                orderTableIds,
                Arrays.asList(OrderStatus.COOKING.name(), OrderStatus.MEAL.name()))) {
            throw new KitchenposException(IMPOSSIBLE_TABLE_STATUS);
        }

        for (OrderTable orderTable : orderTables.getTables()) {
            orderTableDao.save(orderTable);
        }
    }
}
