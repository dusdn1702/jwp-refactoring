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

import static kitchenpos.domain.OrderStatus.COOKING;
import static kitchenpos.exception.KitchenposException.*;

@Service
public class OrderService {
    private final JpaMenuDao menuDao;
    private final JpaOrderDao orderDao;
    private final JpaOrderLineItemDao orderLineItemDao;

    public OrderService(
            final JpaMenuDao menuDao,
            final JpaOrderDao orderDao,
            final JpaOrderLineItemDao orderLineItemDao
    ) {
        this.menuDao = menuDao;
        this.orderDao = orderDao;
        this.orderLineItemDao = orderLineItemDao;
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

        final OrderTable orderTable = order.getOrderTable();

        if (orderTable.isEmpty()) {
            throw new KitchenposException(EMPTY_ORDER_TABLE);
        }

        order.makeOrderIn(orderTable, COOKING, LocalDateTime.now());

        Order savedOrder = orderDao.save(order);

        final List<OrderLineItem> savedOrderLineItems = new ArrayList<>();
        for (OrderLineItem orderLineItem : orderLineItems) {
            orderLineItem.linkOrder(savedOrder);
            OrderLineItem savedOrderLineItem = orderLineItemDao.save(orderLineItem);
            savedOrderLineItems.add(savedOrderLineItem);
        }
        savedOrder.addAllOrderLineItems(savedOrderLineItems);

        return savedOrder;
    }

    public List<Order> list() {
        final List<Order> orders = orderDao.findAll();

        for (final Order order : orders) {
            order.addAllOrderLineItems(orderLineItemDao.findAllByOrder_Id(order.getId()));
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
        savedOrder.changeOrderStatus(orderStatus.name());

        orderDao.save(savedOrder);

        savedOrder.addAllOrderLineItems(orderLineItemDao.findAllByOrder_Id(orderId));

        return savedOrder;
    }
}
