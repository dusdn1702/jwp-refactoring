package kitchenpos.application;

import kitchenpos.dao.JpaOrderDao;
import kitchenpos.dao.JpaOrderTableDao;
import kitchenpos.domain.OrderStatus;
import kitchenpos.domain.OrderTable;
import kitchenpos.exception.KitchenposException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
    public OrderTable create(final OrderTable orderTable) {
        return orderTableDao.save(orderTable);
    }

    public List<OrderTable> list() {
        return orderTableDao.findAll();
    }

    @Transactional
    public OrderTable changeEmpty(final Long orderTableId, final OrderTable orderTable) {
        final OrderTable savedOrderTable = orderTableDao.findById(orderTableId)
                .orElseThrow(() -> new KitchenposException(ILLEGAL_ORDER_TABLE_ID));

        if (Objects.nonNull(savedOrderTable.getTableGroup())) {
            throw new KitchenposException(IMPOSSIBLE_TABLE_GROUP_ID);
        }

        if (orderDao.existsByOrderTable_IdAndOrderStatusIn(
                orderTableId, Arrays.asList(OrderStatus.COOKING.name(), OrderStatus.MEAL.name()))) {
            throw new KitchenposException(IMPOSSIBLE_TABLE_STATUS);
        }

        savedOrderTable.makeEmpty(orderTable.isEmpty());

        return orderTableDao.save(savedOrderTable);
    }

    @Transactional
    public OrderTable changeNumberOfGuests(final Long orderTableId, final OrderTable orderTable) {
        final int numberOfGuests = orderTable.getNumberOfGuests();

        if (numberOfGuests < 0) {
            throw new KitchenposException(IMPOSSIBLE_NUMBER_OF_GUESTS);
        }

        final OrderTable savedOrderTable = orderTableDao.findById(orderTableId)
                .orElseThrow(() -> new KitchenposException(ILLEGAL_ORDER_TABLE_ID));

        if (savedOrderTable.isEmpty()) {
            throw new KitchenposException(EMPTY_ORDER_TABLE);
        }

        savedOrderTable.changeNumberOfGuests(numberOfGuests);

        return orderTableDao.save(savedOrderTable);
    }
}
