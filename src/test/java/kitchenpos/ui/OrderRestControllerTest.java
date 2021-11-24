package kitchenpos.ui;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import kitchenpos.domain.*;
import kitchenpos.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static kitchenpos.ui.MenuGroupRestControllerTest.postMenuGroup;
import static kitchenpos.ui.MenuRestControllerTest.postMenu;
import static kitchenpos.ui.ProductRestControllerTest.postProduct;
import static kitchenpos.ui.TableRestControllerTest.postOrderTable;
import static org.assertj.core.api.Assertions.assertThat;

class OrderRestControllerTest extends ControllerTest {
    private Order order;
    private OrderRequest orderRequest;

    static ExtractableResponse<Response> postOrder(OrderRequest orderRequest) {
        return RestAssured
                .given().log().all()
                .accept("application/json")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(orderRequest)
                .when().post("/api/orders")
                .then().log().all().extract();
    }

    @BeforeEach
    void setUp() {
        super.setUp();
        MenuGroup menuGroup = new MenuGroup("분식");
        MenuGroup savedMenuGroup = postMenuGroup(menuGroup).as(MenuGroup.class);

        Product product = new Product("떡볶이", BigDecimal.valueOf(10000));
        Product savedProduct = postProduct(product).as(Product.class);

        Menu menu = new Menu(
                "떡볶이",
                BigDecimal.valueOf(3000),
                savedMenuGroup,
                null);

        MenuProduct menuProduct = new MenuProduct(menu, savedProduct, 10);

        List<MenuProduct> menuProducts = new ArrayList<>();
        menuProducts.add(menuProduct);
        menu.addAllMenuProducts(menuProducts);

        MenuRequest menuDto = menu.getMenuDto();
        MenuResponse menuResponse = postMenu(menuDto).as(MenuResponse.class);

        menu = new Menu(menuResponse.getId(), menu.getName(), menu.getPrice(), menu.getMenuGroup(), menu.getMenuProducts());

        OrderLineItem orderLineItem = new OrderLineItem(order, menu, 5);
        List<OrderLineItem> orderLineItems = new ArrayList<>();
        orderLineItems.add(orderLineItem);

        OrderTable orderTable = new OrderTable(0, false);
        OrderTableRequest orderTableRequest = new OrderTableRequest(orderTable.getNumberOfGuests(), orderTable.isEmpty());
        OrderTable savedOrderTable = postOrderTable(orderTableRequest).as(OrderTable.class);

        order = new Order(savedOrderTable, OrderStatus.COOKING.name(), LocalDateTime.now());
        order.addAllOrderLineItems(new OrderLineItems(orderLineItems));

    }

    @Test
    @DisplayName("Order 생성")
    void create() {
        orderRequest = OrderRequest.of(order);
        ExtractableResponse<Response> response = postOrder(orderRequest);
        OrderResponse savedOrder = response.as(OrderResponse.class);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(savedOrder.getId()).isNotNull();
    }

    @Test
    @DisplayName("모든 Order 조회")
    void list() {
        orderRequest = OrderRequest.of(order);
        postOrder(orderRequest);
        postOrder(orderRequest);
        postOrder(orderRequest);
        ExtractableResponse<Response> response = getOrders();

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.body().as(List.class)).hasSize(3);
    }

    @Test
    @DisplayName("Order 상태 변경")
    void changeOrderStatus() {
        orderRequest = OrderRequest.of(order);
        OrderResponse savedOrderResponse = postOrder(orderRequest).as(OrderResponse.class);

        Order savedOrder = Order.of(savedOrderResponse);
        savedOrder.changeOrderStatus(OrderStatus.MEAL.name());
        savedOrder.addAllOrderLineItems(new OrderLineItems(order.getOrderLineItems()));
        OrderRequest anotherOrderRequest = OrderRequest.of(savedOrder);

        ExtractableResponse<Response> response = putOrderStatus(savedOrder.getId(), anotherOrderRequest);
        OrderResponse changedOrder = response.as(OrderResponse.class);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(changedOrder.getOrderStatus()).isEqualTo(changedOrder.getOrderStatus());
    }

    private ExtractableResponse<Response> getOrders() {
        return RestAssured
                .given().log().all()
                .accept("application/json")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().get("/api/orders")
                .then().log().all().extract();
    }

    private ExtractableResponse<Response> putOrderStatus(long orderId, OrderRequest changeOrder) {
        return RestAssured
                .given().log().all()
                .accept("application/json")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(changeOrder)
                .when().put("/api/orders/" + orderId + "/order-status")
                .then().log().all().extract();
    }
}
