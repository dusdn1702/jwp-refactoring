package kitchenpos.application;

import kitchenpos.dao.JpaMenuDao;
import kitchenpos.dao.JpaOrderDao;
import kitchenpos.dao.JpaOrderLineItemDao;
import kitchenpos.dao.JpaOrderTableDao;
import kitchenpos.domain.Order;
import kitchenpos.domain.OrderLineItem;
import kitchenpos.domain.OrderLineItems;
import kitchenpos.domain.OrderStatus;
import kitchenpos.dto.OrderRequest;
import kitchenpos.dto.OrderResponse;
import kitchenpos.exception.KitchenposException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static kitchenpos.exception.KitchenposException.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class OrderServiceTest extends ServiceTest {
    @InjectMocks
    private OrderService orderService;

    @Mock
    private JpaMenuDao menuDao;

    @Mock
    private JpaOrderDao orderDao;

    @Mock
    private JpaOrderTableDao orderTableDao;

    @Mock
    private JpaOrderLineItemDao orderLineItemDao;

    @Test
    @DisplayName("주문을 생성한다.")
    void create() {
        Order savedOrder = new Order(1L, orderTable, OrderStatus.COOKING.name(), LocalDateTime.now());
        OrderLineItems orderLineItems1 = new OrderLineItems(orderLineItems);
        savedOrder.addAllOrderLineItems(orderLineItems1);

        when(orderTableDao.findById(anyLong()))
                .thenReturn(Optional.of(orderTable));
        when(menuDao.countByIdIn(anyList()))
                .thenReturn(1L);
        when(menuDao.findById(anyLong()))
                .thenReturn(Optional.of(menu));
        when(orderLineItemDao.save(any(OrderLineItem.class)))
                .thenReturn(orderLineItem);
        when(orderDao.save(any(Order.class)))
                .thenReturn(savedOrder);

        orderRequest = OrderRequest.of(savedOrder);
        OrderResponse actual = orderService.create(orderRequest);
        assertThat(actual.getId()).isNotNull();
    }

    @Test
    @DisplayName("주문에 주문항목이 존재하지 않으면 에러가 발생한다.")
    void createExceptionEmptyOrderLineItems() {
        order.addAllOrderLineItems(new OrderLineItems(Collections.emptyList()));
        OrderRequest anotherOrderRequest = OrderRequest.of(order);

        when(orderTableDao.findById(anyLong()))
                .thenReturn(Optional.of(orderTable));

        assertThatThrownBy(() -> orderService.create(anotherOrderRequest))
                .isInstanceOf(KitchenposException.class)
                .hasMessage(EMPTY_ORDER_LINE_ITEMS);
    }

    @Test
    @DisplayName("주문항목과 메뉴의 갯수가 일치하지 않으면 에러가 발생한다.")
    void createExceptionCountOrderItems() {
        when(orderTableDao.findById(anyLong()))
                .thenReturn(Optional.of(orderTable));
        when(menuDao.findById(anyLong()))
                .thenReturn(Optional.of(menu));
        when(menuDao.countByIdIn(anyList()))
                .thenReturn(2L);

        assertThatThrownBy(() -> orderService.create(orderRequest))
                .isInstanceOf(KitchenposException.class)
                .hasMessage(ILLEGAL_ITEM_SIZE);
    }

    @Test
    @DisplayName("주문테이블이 비어있으면 에러가 발생한다.")
    void createExceptionEmptyOrderTable() {
        orderTable.makeEmpty(true);

        when(orderTableDao.findById(anyLong()))
                .thenReturn(Optional.of(orderTable));

        assertThatThrownBy(() -> orderService.create(orderRequest))
                .isInstanceOf(KitchenposException.class)
                .hasMessage(EMPTY_ORDER_TABLE);
    }

    @Test
    @DisplayName("모든 주문을 조회한다.")
    void list() {
        Order order1 = new Order(2L, orderTable, OrderStatus.COOKING.name(), LocalDateTime.now());
        order1.addAllOrderLineItems(new OrderLineItems(orderLineItems));

        List<Order> orders = new ArrayList<>();
        orders.add(order);
        orders.add(order1);

        when(orderDao.findAll())
                .thenReturn(orders);
        when(orderLineItemDao.findAllByOrder_Id(anyLong()))
                .thenReturn(orderLineItems);

        List<Order> actual = orderService.list();
        assertThat(actual).usingRecursiveComparison()
                .isEqualTo(orders);
    }

    @Test
    @DisplayName("주문의 상태를 변경한다.")
    void changeOrderStatus() {
        Order anotherOrder = new Order(order.getId(), order.getOrderTable(), OrderStatus.COMPLETION.name(), order.getOrderedTime());
        anotherOrder.addAllOrderLineItems(new OrderLineItems(order.getOrderLineItems()));
        OrderRequest anotherOrderRequest = OrderRequest.of(anotherOrder);

        when(orderDao.findById(anyLong()))
                .thenReturn(Optional.of(order));
        when(orderDao.save(any(Order.class)))
                .thenReturn(anotherOrder);
        when(orderLineItemDao.findAllByOrder_Id(anyLong()))
                .thenReturn(orderLineItems);

        OrderResponse actual = orderService.changeOrderStatus(order.getId(), anotherOrderRequest);
        assertThat(actual.getOrderStatus()).isEqualTo(OrderStatus.COMPLETION.name());
    }

    @Test
    @DisplayName("주문이 올바르지 않으면 에러가 발생한다.")
    void changeOrderStatusExceptionIllegalOrder() {
        order.changeOrderStatus(OrderStatus.COMPLETION.name());
        OrderRequest orderRequest = OrderRequest.of(order);

        when(orderDao.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.changeOrderStatus(1L, orderRequest))
                .isInstanceOf(KitchenposException.class)
                .hasMessage(ILLEGAL_ORDER_ID);
    }

    @Test
    @DisplayName("변경하고자 하는 상태가 완료되면 에러가 발생한다.")
    void changeOrderStatusExceptionSameStatus() {
        order.changeOrderStatus(OrderStatus.COMPLETION.name());
        OrderRequest orderRequest = OrderRequest.of(order);

        when(orderDao.findById(anyLong()))
                .thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.changeOrderStatus(1L, orderRequest))
                .isInstanceOf(KitchenposException.class)
                .hasMessage(SAME_ORDER_STATUS);
    }
}
