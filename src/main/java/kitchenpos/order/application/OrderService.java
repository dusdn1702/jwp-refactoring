package kitchenpos.order.application;

import kitchenpos.menu.repository.JpaMenuDao;
import kitchenpos.order.repository.JpaOrderDao;
import kitchenpos.order.repository.JpaOrderLineItemDao;
import kitchenpos.table.domain.OrderTable;
import kitchenpos.table.repository.JpaOrderTableDao;
import kitchenpos.order.domain.*;
import kitchenpos.order.dto.OrderLineItemRequest;
import kitchenpos.order.dto.OrderRequest;
import kitchenpos.order.dto.OrderResponse;
import kitchenpos.exception.KitchenposException;
import kitchenpos.menu.domain.Menu;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static kitchenpos.order.domain.OrderStatus.COOKING;
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
            final JpaOrderTableDao orderTableDao) {
        this.menuDao = menuDao;
        this.orderDao = orderDao;
        this.orderLineItemDao = orderLineItemDao;
        this.orderTableDao = orderTableDao;
    }

    @Transactional
    public OrderResponse create(final OrderRequest orderRequest) {
        OrderTable orderTable = makeValidOrderTable(orderRequest);
        Order order = new Order(orderTable, orderRequest.getOrderStatus(), orderRequest.getOrderedTime());
        OrderLineItems orderLineItems = makeValidOrderLineItems(orderRequest, order);

        order.makeOrderIn(COOKING, LocalDateTime.now());
        Order savedOrder = orderDao.save(order);

        OrderLineItems saveOrderLineItems = saveOrderLineItems(orderLineItems);
        savedOrder.addAllOrderLineItems(saveOrderLineItems);

        return OrderResponse.of(savedOrder);
    }

    private OrderTable makeValidOrderTable(OrderRequest orderRequest) {
        long orderTableId = orderRequest.getOrderTableId();
        OrderTable orderTable = orderTableDao.findById(orderTableId)
                .orElseThrow(() -> new KitchenposException(ILLEGAL_ORDER_TABLE_ID));
        if (orderTable.isEmpty()) {
            throw new KitchenposException(EMPTY_ORDER_TABLE);
        }
        return orderTable;
    }

    private OrderLineItems makeValidOrderLineItems(OrderRequest orderRequest, Order order) {
        OrderLineItems orderLineItems = makeOrderLineItems(orderRequest, order);
        if (orderLineItems.isEmpty()) {
            throw new KitchenposException(EMPTY_ORDER_LINE_ITEMS);
        }

        List<Long> menuIds = orderLineItems.menuIdsFromOrderLineItem();
        if (orderLineItems.isNotSameSize(menuDao.countByIdIn(menuIds))) {
            throw new KitchenposException(ILLEGAL_ITEM_SIZE);
        }
        return orderLineItems;
    }

    private OrderLineItems makeOrderLineItems(OrderRequest orderRequest, Order order) {
        List<OrderLineItemRequest> orderLineItemRequests = orderRequest.getOrderLineItemRequests();
        List<OrderLineItem> orderLineItemsByRequest = orderLineItemRequests.stream()
                .map(orderLineItemRequest -> makeOrderLineItem(order, orderLineItemRequest))
                .collect(Collectors.toList());

        return new OrderLineItems(orderLineItemsByRequest);
    }

    private OrderLineItem makeOrderLineItem(Order order, OrderLineItemRequest orderLineItemRequest) {
        long menuId = orderLineItemRequest.getMenuId();
        Menu menu = menuDao.findById(menuId)
                .orElseThrow(() -> new KitchenposException(ILLEGAL_MENU_ID));
        return new OrderLineItem(order, menu, orderLineItemRequest.getQuantity());
    }

    private OrderLineItems saveOrderLineItems(OrderLineItems orderLineItems) {
        List<OrderLineItem> savedOrderLineItems = new ArrayList<>();
        for (OrderLineItem orderLineItem : orderLineItems.getLineItems()) {
            OrderLineItem savedOrderLineItem = orderLineItemDao.save(orderLineItem);
            savedOrderLineItems.add(savedOrderLineItem);
        }
        return new OrderLineItems(savedOrderLineItems);
    }

    public List<Order> list() {
        final List<Order> orders = orderDao.findAll();
        for (final Order order : orders) {
            findOrderLIneItems(order.getId(), order);
        }
        return orders;
    }

    @Transactional
    public OrderResponse changeOrderStatus(final Long orderId, final OrderRequest orderRequest) {
        Order savedOrder = orderDao.findById(orderId)
                .orElseThrow(() -> new KitchenposException(ILLEGAL_ORDER_ID));

        if (savedOrder.sameOrderStatus(OrderStatus.COMPLETION)) {
            throw new KitchenposException(SAME_ORDER_STATUS);
        }

        final OrderStatus orderStatus = OrderStatus.valueOf(orderRequest.getOrderStatus());
        savedOrder.changeOrderStatus(orderStatus.name());

        orderDao.save(savedOrder);
        findOrderLIneItems(orderId, savedOrder);

        return OrderResponse.of(savedOrder);
    }

    private void findOrderLIneItems(Long orderId, Order savedOrder) {
        List<OrderLineItem> orderLineItemsByOrderId = orderLineItemDao.findAllByOrder_Id(orderId);
        OrderLineItems orderLineItems = new OrderLineItems(orderLineItemsByOrderId);
        savedOrder.addAllOrderLineItems(orderLineItems);
    }
}
