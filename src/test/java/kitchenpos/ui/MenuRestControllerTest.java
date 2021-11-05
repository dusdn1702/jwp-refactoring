package kitchenpos.ui;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import kitchenpos.domain.Menu;
import kitchenpos.domain.MenuGroup;
import kitchenpos.domain.MenuProduct;
import kitchenpos.domain.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static kitchenpos.ui.MenuGroupRestControllerTest.postMenuGroup;
import static kitchenpos.ui.ProductRestControllerTest.postProduct;
import static org.assertj.core.api.Assertions.assertThat;

class MenuRestControllerTest extends ControllerTest {
    @Test
    @DisplayName("menu 생성")
    void create() {
        MenuGroup menuGroup = new MenuGroup("분식");
        MenuGroup savedMenuGroup = postMenuGroup(menuGroup).as(MenuGroup.class);

        Product product = new Product("떡볶이", BigDecimal.valueOf(10000));
        Product savedProduct = postProduct(product).as(Product.class);

        Menu menu = new Menu("떡볶이", BigDecimal.valueOf(3000), savedMenuGroup);

        MenuProduct menuProduct = new MenuProduct(menu, savedProduct, 10);

        List<MenuProduct> menuProducts = new ArrayList<>();
        menuProducts.add(menuProduct);
        menu.addAllMenuProducts(menuProducts);

        ExtractableResponse<Response> response = postMenu(menu);
        Menu savedMenu = response.as(Menu.class);
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(savedMenu.getId()).isNotNull();
    }

    @Test
    @DisplayName("모든 menu 조회")
    void list() {
        ExtractableResponse<Response> response = getMenus();
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.body().as(List.class)).isEmpty();
    }

    static ExtractableResponse<Response> postMenu(Menu menu) {
        return RestAssured
                .given().log().all()
                .accept("application/json")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(menu)
                .when().post("/api/menus")
                .then().log().all().extract();
    }

    private ExtractableResponse<Response> getMenus() {
        return RestAssured
                .given().log().all()
                .accept("application/json")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().get("/api/menus")
                .then().log().all().extract();
    }
}
