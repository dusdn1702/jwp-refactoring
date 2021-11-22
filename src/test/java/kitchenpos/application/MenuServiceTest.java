package kitchenpos.application;

import kitchenpos.dao.JpaMenuDao;
import kitchenpos.dao.JpaMenuGroupDao;
import kitchenpos.dao.JpaMenuProductDao;
import kitchenpos.dao.JpaProductDao;
import kitchenpos.domain.Menu;
import kitchenpos.domain.MenuProduct;
import kitchenpos.domain.Product;
import kitchenpos.dto.MenuRequest;
import kitchenpos.dto.MenuResponse;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Mock
    private JpaProductDao productDao;

    @Test
    @DisplayName("올바르게 메뉴 생성 요청이 들어오면 수행된다.")
    void create() {
        when(menuGroupDao.findById(anyLong()))
                .thenReturn(Optional.of(menuGroup));
        when(menuDao.save(any(Menu.class)))
                .thenReturn(menu);
        when(menuProductDao.save(any(MenuProduct.class)))
                .thenReturn(menuProduct);
        when(productDao.findById(anyLong()))
                .thenReturn(Optional.of(product));

        MenuRequest menuRequest = menu.getMenuDto();
        MenuResponse actual = menuService.create(menuRequest);

        assertThat(actual.getId()).isNotNull();
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -1000})
    @DisplayName("메뉴의 가격이 0보다 작으면 예외가 발생한다.")
    void createExceptionMinusPrice(Integer price) {
        when(menuGroupDao.findById(anyLong()))
                .thenReturn(Optional.of(menuGroup));

        MenuRequest menuRequest = new MenuRequest(
                menu.getName(),
                BigDecimal.valueOf(price),
                menu.getMenuGroup().getId(),
                menu.getMenuProducts().stream().map(MenuProduct::toDto).collect(Collectors.toList()));

        assertThatThrownBy(() -> menuService.create(menuRequest))
                .isInstanceOf(KitchenposException.class)
                .hasMessage(ILLEGAL_PRICE);
    }

    @ParameterizedTest
    @NullSource
    @DisplayName("메뉴의 가격이 없으면 예외가 발생한다.")
    void createExceptionEmptyPrice(BigDecimal price) {
        when(menuGroupDao.findById(anyLong()))
                .thenReturn(Optional.of(menuGroup));

        MenuRequest menuRequest = new MenuRequest(
                menu.getName(),
                price,
                menu.getMenuGroup().getId(),
                menu.getMenuProducts().stream().map(MenuProduct::toDto).collect(Collectors.toList()));

        assertThatThrownBy(() -> menuService.create(menuRequest))
                .isInstanceOf(KitchenposException.class)
                .hasMessage(ILLEGAL_PRICE);
    }

    @Test
    @DisplayName("메뉴의 메뉴그룹이 존재하지 않으면 예외가 발생한다.")
    void createExceptionIllegalGroupId() {
        when(menuGroupDao.findById(anyLong()))
                .thenReturn(Optional.empty());

        MenuRequest menuRequest = menu.getMenuDto();

        assertThatThrownBy(() -> menuService.create(menuRequest))
                .isInstanceOf(KitchenposException.class)
                .hasMessage(ILLEGAL_MENU_GROUP_ID);
    }

    @ParameterizedTest
    @CsvSource(value = {"10:1:9", "1000:100:5", "1000:1:10"}, delimiter = ':')
    @DisplayName("메뉴의 가격이 메뉴상품들의 총합보다 비싸면 예외가 발생한다.")
    void createExceptionImpossibleSum(long menuPrice, long productPrice, long quantity) {
        product = new Product(product.getId(), product.getName(), BigDecimal.valueOf(productPrice));
        MenuProduct anotherMenuProduct = new MenuProduct(1L, menu, product, quantity);
        menu.addAllMenuProducts(Collections.singletonList(anotherMenuProduct));

        MenuRequest menuRequest = new MenuRequest(
                menu.getName(),
                BigDecimal.valueOf(menuPrice),
                menu.getMenuGroup().getId(),
                menu.getMenuProducts().stream().map(MenuProduct::toDto).collect(Collectors.toList()));

        when(menuGroupDao.findById(anyLong()))
                .thenReturn(Optional.of(menuGroup));
        when(productDao.findById(anyLong()))
                .thenReturn(Optional.of(product));

        assertThatThrownBy(() -> menuService.create(menuRequest))
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
