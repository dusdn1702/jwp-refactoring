package kitchenpos.table.application;

import kitchenpos.order.repository.JpaOrderDao;
import kitchenpos.table.repository.JpaOrderTableDao;
import kitchenpos.order.domain.OrderStatus;
import kitchenpos.table.domain.OrderTable;
import kitchenpos.table.dto.OrderTableRequest;
import kitchenpos.table.dto.OrderTableResponse;
import kitchenpos.exception.KitchenposException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static kitchenpos.exception.KitchenposException.*;

@Service
public class TableService {
    private final JpaOrderDao orderDao;
    private final JpaOrderTableDao orderTableDao;

    public TableService(final JpaOrderDao orderDao, final JpaOrderTableDao orderTableDao) {
        this.orderDao = orderDao;
        this.orderTableDao = orderTableDao;
    }

    @Transactional
    public OrderTableResponse create(final OrderTableRequest orderTableRequest) {
        OrderTable orderTable = new OrderTable(orderTableRequest.getNumberOfGuests(), orderTableRequest.isEmpty());
        OrderTable savedOrderTable = orderTableDao.save(orderTable);
        return OrderTableResponse.of(savedOrderTable);
    }

    public List<OrderTableResponse> list() {
        List<OrderTable> orderTables = orderTableDao.findAll();

        return orderTables.stream()
                .map(OrderTableResponse::of)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderTableResponse changeEmpty(final Long orderTableId, final OrderTableRequest orderTableRequest) {
        OrderTable savedOrderTable = orderTableDao.findById(orderTableId)
                .orElseThrow(() -> new KitchenposException(ILLEGAL_ORDER_TABLE_ID));
        OrderTable orderTable = new OrderTable(orderTableRequest.getNumberOfGuests(), orderTableRequest.isEmpty());

        if (orderDao.existsByOrderTable_IdAndOrderStatusIn(
                orderTableId, Arrays.asList(OrderStatus.COOKING.name(), OrderStatus.MEAL.name()))) {
            throw new KitchenposException(IMPOSSIBLE_TABLE_STATUS);
        }

        savedOrderTable.makeEmpty(orderTable.isEmpty());

        OrderTable changedOrderTable = orderTableDao.save(savedOrderTable);
        return OrderTableResponse.of(changedOrderTable);
    }

    @Transactional
    public OrderTableResponse changeNumberOfGuests(final Long orderTableId, final OrderTableRequest orderTableRequest) {
        OrderTable orderTable = new OrderTable(orderTableRequest.getNumberOfGuests(), orderTableRequest.isEmpty());
        if (orderTable.isInvalidGuestNumbers()) {
            throw new KitchenposException(IMPOSSIBLE_NUMBER_OF_GUESTS);
        }

        OrderTable savedOrderTable = orderTableDao.findById(orderTableId)
                .orElseThrow(() -> new KitchenposException(ILLEGAL_ORDER_TABLE_ID));
        if (savedOrderTable.isEmpty()) {
            throw new KitchenposException(EMPTY_ORDER_TABLE);
        }

        savedOrderTable.changeNumberOfGuests(orderTable);
        OrderTable changedOrderTable = orderTableDao.save(savedOrderTable);

        return OrderTableResponse.of(changedOrderTable);
    }
}
