package kitchenpos.application;

import kitchenpos.domain.*;
import kitchenpos.dto.OrderRequest;
import kitchenpos.dto.OrderTableRequest;
import kitchenpos.dto.TableGroupRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ServiceTest {
    protected Product product;
    protected Menu menu;
    protected MenuProduct menuProduct;
    protected List<MenuProduct> menuProducts;
    protected MenuGroup menuGroup;

    protected Order order;
    protected OrderRequest orderRequest;
    protected OrderLineItem orderLineItem;
    protected List<OrderLineItem> orderLineItems;

    protected OrderTable orderTable;
    protected OrderTable orderTable2;
    protected OrderTableRequest orderTableRequest1;
    protected OrderTableRequest orderTableRequest2;

    protected TableGroup tableGroup;
    protected TableGroupRequest tableGroupRequest;

    @BeforeEach
    void setUp() {
        menuGroup = new MenuGroup(1L, "분식");

        menu = new Menu(1L, "떡볶이", BigDecimal.valueOf(2000L), menuGroup, null);

        product = new Product(1L, "어묵", BigDecimal.valueOf(1000L));

        menuProduct = new MenuProduct(1L, menu, product, 100);
        menuProducts = new ArrayList<>();
        menuProducts.add(menuProduct);

        menu.addAllMenuProducts(menuProducts);

        tableGroup = new TableGroup(1L, LocalDateTime.now());

        orderTable = new OrderTable(1L, 3, false);
        orderTable2 = new OrderTable(2L, 6, false);
        orderTableRequest1 = new OrderTableRequest(orderTable.getNumberOfGuests(), orderTable.isEmpty());
        orderTableRequest2 = new OrderTableRequest(orderTable2.getNumberOfGuests(), orderTable2.isEmpty());

        order = new Order(1L, orderTable, OrderStatus.COOKING.name(), LocalDateTime.now());
        orderLineItem = new OrderLineItem(1L, order, menu, 10);

        orderLineItems = new ArrayList<>();
        orderLineItems.add(orderLineItem);

        order.addAllOrderLineItems(new OrderLineItems(orderLineItems));
        orderRequest = OrderRequest.of(order);

        List<OrderTable> orderTables = new ArrayList<>(Arrays.asList(orderTable, orderTable2));
        tableGroup.addAllOrderTables(orderTables);
        tableGroupRequest = new TableGroupRequest(tableGroup.getCreatedDate(), List.of(orderTableRequest1, orderTableRequest2));
    }
}
