package kitchenpos.application;

import kitchenpos.dao.JpaMenuDao;
import kitchenpos.dao.JpaMenuGroupDao;
import kitchenpos.dao.JpaMenuProductDao;
import kitchenpos.domain.Menu;
import kitchenpos.domain.MenuProduct;
import kitchenpos.domain.Product;
import kitchenpos.exception.KitchenposException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static kitchenpos.exception.KitchenposException.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

class MenuServiceTest extends ServiceTest {
    @InjectMocks
    private MenuService menuService;

    @Mock
    private JpaMenuGroupDao menuGroupDao;

    @Mock
    private JpaMenuDao menuDao;

    @Mock
    private JpaMenuProductDao menuProductDao;

    @Test
    @DisplayName("올바르게 메뉴 생성 요청이 들어오면 수행된다.")
    void create() {
        when(menuGroupDao.existsById(anyLong()))
                .thenReturn(true);
        when(menuProductDao.save(any(MenuProduct.class)))
                .thenReturn(menuProduct);
        when(menuDao.save(any(Menu.class)))
                .thenReturn(menu);

        Menu actual = menuService.create(menu);

        assertThat(actual.getId()).isNotNull();
        assertThat(actual).usingRecursiveComparison()
                .isEqualTo(menu);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -1000})
    @DisplayName("메뉴의 가격이 0보다 작으면 예외가 발생한다.")
    void createExceptionMinusPrice(Integer price) {
        Menu anotherMenu = new Menu(
                menu.getId(),
                menu.getName(),
                BigDecimal.valueOf(price),
                menu.getMenuGroup(),
                menu.getMenuProducts());

        assertThatThrownBy(() -> menuService.create(anotherMenu))
                .isInstanceOf(KitchenposException.class)
                .hasMessage(ILLEGAL_PRICE);
    }

    @ParameterizedTest
    @NullSource
    @DisplayName("메뉴의 가격이 없으면 예외가 발생한다.")
    void createExceptionEmptyPrice(BigDecimal price) {
        Menu anotherMenu = new Menu(
                menu.getId(),
                menu.getName(),
                price,
                menu.getMenuGroup(),
                menu.getMenuProducts());

        assertThatThrownBy(() -> menuService.create(anotherMenu))
                .isInstanceOf(KitchenposException.class)
                .hasMessage(ILLEGAL_PRICE);
    }

    @Test
    @DisplayName("메뉴의 메뉴그룹이 존재하지 않으면 예외가 발생한다.")
    void createExceptionIllegalGroupId() {
        when(menuGroupDao.existsById(anyLong()))
                .thenReturn(false);

        assertThatThrownBy(() -> menuService.create(menu))
                .isInstanceOf(KitchenposException.class)
                .hasMessage(ILLEGAL_MENU_GROUP_ID);
    }

    @ParameterizedTest
    @CsvSource(value = {"10:1:9", "1000:100:5", "1000:1:10"}, delimiter = ':')
    @DisplayName("메뉴의 가격이 메뉴상품들의 총합보다 비싸면 예외가 발생한다.")
    void createExceptionImpossibleSum(long menuPrice, long productPrice, long quantity) {
        product = new Product(product.getId(), product.getName(), BigDecimal.valueOf(productPrice));
        MenuProduct anotherMenuProduct = new MenuProduct(menu, product, quantity);
        menu.addAllMenuProducts(Collections.singletonList(anotherMenuProduct));

        Menu anotherMenu = new Menu(
                menu.getId(),
                menu.getName(),
                BigDecimal.valueOf(menuPrice),
                menu.getMenuGroup(),
                menu.getMenuProducts());

        when(menuGroupDao.existsById(anyLong()))
                .thenReturn(true);

        assertThatThrownBy(() -> menuService.create(anotherMenu))
                .isInstanceOf(KitchenposException.class)
                .hasMessage(IMPOSSIBLE_MENU_PRICE);
    }

    @Test
    @DisplayName("모든 메뉴를 조회한다.")
    void list() {
        Menu menu2 = new Menu(
                2L,
                "순대",
                BigDecimal.valueOf(3000.0),
                menuGroup,
                menuProducts);

        MenuProduct menuProduct2 = new MenuProduct(2L, menu2, product, 100);
        menuProducts.add(menuProduct2);

        menu2.addAllMenuProducts(menuProducts);

        List<Menu> menus = new ArrayList<>();
        menus.add(menu);
        menus.add(menu2);
        when(menuDao.findAll())
                .thenReturn(menus);
        when(menuProductDao.findAllByMenu_Id(1L))
                .thenReturn(Collections.singletonList(menuProduct));
        when(menuProductDao.findAllByMenu_Id(2L))
                .thenReturn(Collections.singletonList(menuProduct2));

        List<Menu> actual = menuService.list();
        assertThat(actual).hasSize(2);
        assertThat(actual).usingRecursiveComparison()
                .isEqualTo(menus);
    }
}
