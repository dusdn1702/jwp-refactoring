package kitchenpos.application;

import kitchenpos.dao.*;
import kitchenpos.domain.Order;
import kitchenpos.domain.OrderLineItem;
import kitchenpos.domain.OrderStatus;
import kitchenpos.domain.OrderTable;
import kitchenpos.exception.KitchenposException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static kitchenpos.exception.KitchenposException.*;

@Service
public class OrderService {
    private final JpaMenuDao menuDao;
    private final JpaOrderDao orderDao;
    private final JpaOrderLineItemDao orderLineItemDao;
    private final JpaOrderTableDao orderTableDao;

    public OrderService(
            final JpaMenuDao menuDao,
            final JpaOrderDao orderDao,
            final JpaOrderLineItemDao orderLineItemDao,
            final JpaOrderTableDao orderTableDao
    ) {
        this.menuDao = menuDao;
        this.orderDao = orderDao;
        this.orderLineItemDao = orderLineItemDao;
        this.orderTableDao = orderTableDao;
    }

    @Transactional
    public Order create(final Order order) {
        final List<OrderLineItem> orderLineItems = order.getOrderLineItems();

        if (CollectionUtils.isEmpty(orderLineItems)) {
            throw new KitchenposException(EMPTY_ORDER_LINE_ITEMS);
        }

        final List<Long> menuIds = orderLineItems.stream()
                .map(OrderLineItem::getMenuId)
                .collect(Collectors.toList());

        if (orderLineItems.size() != menuDao.countByIdIn(menuIds)) {
            throw new KitchenposException(ILLEGAL_ITEM_SIZE);
        }

        order.setId(null);

        final OrderTable orderTable = orderTableDao.findById(order.getOrderTableId())
                .orElseThrow(() -> new KitchenposException(ILLEGAL_ORDER_TABLE_ID));

        if (orderTable.isEmpty()) {
            throw new KitchenposException(EMPTY_ORDER_TABLE);
        }

        order.setOrderTableId(orderTable.getId());
        order.setOrderStatus(OrderStatus.COOKING.name());
        order.setOrderedTime(LocalDateTime.now());

        final Order savedOrder = orderDao.save(order);

        final Long orderId = savedOrder.getId();
        final List<OrderLineItem> savedOrderLineItems = new ArrayList<>();
        for (final OrderLineItem orderLineItem : orderLineItems) {
            orderLineItem.setOrderId(orderId);
            savedOrderLineItems.add(orderLineItemDao.save(orderLineItem));
        }
        savedOrder.setOrderLineItems(savedOrderLineItems);

        return savedOrder;
    }

    public List<Order> list() {
        final List<Order> orders = orderDao.findAll();

        for (final Order order : orders) {
            order.setOrderLineItems(orderLineItemDao.findAllByOrder_Id(order.getId()));
        }

        return orders;
    }

    @Transactional
    public Order changeOrderStatus(final Long orderId, final Order order) {
        final Order savedOrder = orderDao.findById(orderId)
                .orElseThrow(() -> new KitchenposException(ILLEGAL_ORDER_ID));

        if (Objects.equals(OrderStatus.COMPLETION.name(), savedOrder.getOrderStatus())) {
            throw new KitchenposException(SAME_ORDER_STATUS);
        }

        final OrderStatus orderStatus = OrderStatus.valueOf(order.getOrderStatus());
        savedOrder.setOrderStatus(orderStatus.name());

        orderDao.save(savedOrder);

        savedOrder.setOrderLineItems(orderLineItemDao.findAllByOrder_Id(orderId));

        return savedOrder;
    }
}
