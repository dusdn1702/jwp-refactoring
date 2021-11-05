package kitchenpos.application;

import kitchenpos.domain.*;
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
    protected OrderLineItem orderLineItem;
    protected List<OrderLineItem> orderLineItems;

    protected OrderTable orderTable;
    protected OrderTable orderTable2;

    protected TableGroup tableGroup;

    @BeforeEach
    void setUp() {
        menuGroup = new MenuGroup(1L, "분식");

        menu = new Menu(1L, "떡볶이", BigDecimal.valueOf(2000L), menuGroup);

        product = new Product(1L, "어묵", BigDecimal.valueOf(1000L));

        menuProduct = new MenuProduct(1L, menu, product, 100);

        menuProducts = new ArrayList<>();
        menuProducts.add(menuProduct);

        menu.addAllMenuProducts(menuProducts);

        order = new Order(1L, orderTable, OrderStatus.COOKING.name(), LocalDateTime.now());
        orderLineItem = new OrderLineItem(1L, order, menu, 10);

        orderLineItems = new ArrayList<>();
        orderLineItems.add(orderLineItem);

        order.addAllOrderLineItems(orderLineItems);

        tableGroup = new TableGroup(1L, LocalDateTime.now());

        orderTable = new OrderTable(1L, tableGroup, 3, false);
        orderTable2 = new OrderTable(2L, tableGroup, 6, false);

        List<OrderTable> orderTables = new ArrayList<>(Arrays.asList(orderTable, orderTable2));
        tableGroup.addAllOrderTables(orderTables);
    }
}
