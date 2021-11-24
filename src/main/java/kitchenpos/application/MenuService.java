package kitchenpos.application;

import kitchenpos.dao.*;
import kitchenpos.domain.Menu;
import kitchenpos.domain.MenuGroup;
import kitchenpos.domain.MenuProduct;
import kitchenpos.domain.Product;
import kitchenpos.dto.MenuRequest;
import kitchenpos.dto.MenuProductRequest;
import kitchenpos.dto.MenuResponse;
import kitchenpos.exception.KitchenposException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static kitchenpos.exception.KitchenposException.*;

@Service
public class MenuService {
    private final JpaMenuDao menuDao;
    private final JpaMenuGroupDao menuGroupDao;
    private final JpaMenuProductDao menuProductDao;
    private final JpaProductDao productDao;

    public MenuService(
            final JpaMenuDao menuDao,
            final JpaMenuGroupDao menuGroupDao,
            final JpaMenuProductDao menuProductDao,
            final JpaProductDao productDao) {
        this.menuDao = menuDao;
        this.menuGroupDao = menuGroupDao;
        this.menuProductDao = menuProductDao;
        this.productDao = productDao;
    }

    @Transactional
    public MenuResponse create(final MenuRequest menuRequest) {
        MenuGroup menuGroup = menuGroupDao.findById(menuRequest.getMenuGroupId())
                .orElseThrow(() -> new KitchenposException(ILLEGAL_MENU_GROUP_ID));
        Menu menu = new Menu(menuRequest.getName(), menuRequest.getPrice(), menuGroup, new ArrayList<>());
        List<MenuProduct> menuProducts = menuRequest.getMenuProductRequests().stream()
                .map(menuProductRequest -> makeMenuProduct(menu, menuProductRequest))
                .collect(Collectors.toList());

        if (menu.isValidPrice()) {
            throw new KitchenposException(ILLEGAL_PRICE);
        }
        checkPossiblePrice(menuProducts, menu);

        Menu savedMenu = saveMenuAndProducts(menu, menuProducts);
        return MenuResponse.of(savedMenu);
    }

    private Menu saveMenuAndProducts(Menu menu, List<MenuProduct> menuProducts) {
        Menu savedMenu = menuDao.save(menu);
        List<MenuProduct> savedMenuProducts = makeMenuProducts(menuProducts, savedMenu);
        savedMenu.addAllMenuProducts(savedMenuProducts);
        return savedMenu;
    }

    private MenuProduct makeMenuProduct(Menu menu, MenuProductRequest menuProductRequest) {
        long productId = menuProductRequest.getProductId();
        Product product = productDao.findById(productId)
                .orElseThrow(() -> new KitchenposException(ILLEGAL_MENU_PRODUCT_ID));
        return new MenuProduct(menu, product, menuProductRequest.getQuantity());
    }

    private void checkPossiblePrice(List<MenuProduct> menuProducts, Menu menu) {
        BigDecimal sum = BigDecimal.ZERO;
        for (MenuProduct menuProduct : menuProducts) {
            Product product = menuProduct.getProduct();
            sum = sum.add(product.calculateTotal(menuProduct.getQuantity()));
        }
        if (menu.isSumSmallerThan(sum)) {
            throw new KitchenposException(IMPOSSIBLE_MENU_PRICE);
        }
    }

    private List<MenuProduct> makeMenuProducts(List<MenuProduct> menuProducts, Menu savedMenu) {
        List<MenuProduct> savedMenuProducts = new ArrayList<>();
        for (MenuProduct menuProduct : menuProducts) {
            menuProduct.makeMenu(savedMenu);
            savedMenuProducts.add(menuProductDao.save(menuProduct));
        }
        return savedMenuProducts;
    }

    public List<Menu> list() {
        final List<Menu> menus = menuDao.findAll();

        for (final Menu menu : menus) {
            menu.addAllMenuProducts(menuProductDao.findAllByMenu_Id(menu.getId()));
        }

        return menus;
    }
}
