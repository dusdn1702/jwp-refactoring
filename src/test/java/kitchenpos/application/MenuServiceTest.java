package kitchenpos.application;

import kitchenpos.dao.*;
import kitchenpos.domain.Menu;
import kitchenpos.domain.MenuProduct;
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
        when(menuGroupDao.existsById(anyLong()))
                .thenReturn(true);
        when(productDao.findById(anyLong()))
                .thenReturn(Optional.of(product));
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
        menu.setPrice(BigDecimal.valueOf(price));

        assertThatThrownBy(() -> menuService.create(menu))
                .isInstanceOf(KitchenposException.class)
                .hasMessage(ILLEGAL_PRICE);
    }

    @ParameterizedTest
    @NullSource
    @DisplayName("메뉴의 가격이 없으면 예외가 발생한다.")
    void createExceptionEmptyPrice(BigDecimal price) {
        menu.setPrice(price);

        assertThatThrownBy(() -> menuService.create(menu))
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

    @Test
    @DisplayName("메뉴의 상품이 존재하지 않으면 예외가 발생한다.")
    void createExceptionIllegalProductId() {
        when(menuGroupDao.existsById(anyLong()))
                .thenReturn(true);
        when(productDao.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> menuService.create(menu))
                .isInstanceOf(KitchenposException.class)
                .hasMessage(ILLEGAL_PRODUCT_ID);
    }


    @ParameterizedTest
    @CsvSource(value = {"10:1:9", "1000:100:5", "1000:1:10"}, delimiter = ':')
    @DisplayName("메뉴의 가격이 메뉴상품들의 총합보다 비싸면 예외가 발생한다.")
    void createExceptionImpossibleSum(int menuPrice, int productPrice, int quantity) {
        menu.setPrice(BigDecimal.valueOf(menuPrice));
        product.setPrice(BigDecimal.valueOf(productPrice));
        menuProduct.setQuantity(quantity);

        when(menuGroupDao.existsById(anyLong()))
                .thenReturn(true);
        when(productDao.findById(anyLong()))
                .thenReturn(Optional.of(product));

        assertThatThrownBy(() -> menuService.create(menu))
                .isInstanceOf(KitchenposException.class)
                .hasMessage(IMPOSSIBLE_MENU_PRICE);
    }

    @Test
    @DisplayName("모든 메뉴를 조회한다.")
    void list() {
        Menu menu2 = new Menu();
        menu2.setName("순대");
        menu2.setId(2L);

        MenuProduct menuProduct2 = new MenuProduct();
        menuProduct2.setMenuId(menu2.getId());
        menuProduct2.setProductId(2L);
        menuProduct2.setSeq(2L);
        menuProduct2.setQuantity(100);
        menuProducts.add(menuProduct2);

        menu2.setMenuProducts(menuProducts);
        menu2.setPrice(BigDecimal.valueOf(3000.0));
        menu2.setMenuGroupId(menuGroup.getId());

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
