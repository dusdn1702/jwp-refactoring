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
import java.util.Objects;

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

        List<MenuProductRequest> menuProductsDto = menuRequest.getMenuProducts();
        List<MenuProduct> menuProducts = new ArrayList<>();

        Menu menu = new Menu(menuRequest.getName(), menuRequest.getPrice(), menuGroup, menuProducts);
        final BigDecimal price = menu.getPrice();

        if (Objects.isNull(price) || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new KitchenposException(ILLEGAL_PRICE);
        }

        BigDecimal sum = BigDecimal.ZERO;
        for (MenuProductRequest menuProductRequest : menuProductsDto) {
            long productId = menuProductRequest.getProductId();
            Product product = productDao.findById(productId).orElseThrow(() -> new KitchenposException(ILLEGAL_MENU_PRODUCT_ID));
            MenuProduct menuProduct = new MenuProduct(menu, product, menuProductRequest.getQuantity());
            menuProducts.add(menuProduct);
            sum = sum.add(product.getPrice().multiply(BigDecimal.valueOf(menuProduct.getQuantity())));
        }

        if (price.compareTo(sum) > 0) {
            throw new KitchenposException(IMPOSSIBLE_MENU_PRICE);
        }

        final Menu savedMenu = menuDao.save(menu);

        final List<MenuProduct> savedMenuProducts = new ArrayList<>();
        for (final MenuProduct menuProduct : menuProducts) {
            menuProduct.makeMenu(savedMenu);
            savedMenuProducts.add(menuProductDao.save(menuProduct));
        }
        savedMenu.addAllMenuProducts(savedMenuProducts);

        return MenuResponse.of(savedMenu);
    }

    public List<Menu> list() {
        final List<Menu> menus = menuDao.findAll();

        for (final Menu menu : menus) {
            menu.addAllMenuProducts(menuProductDao.findAllByMenu_Id(menu.getId()));
        }

        return menus;
    }
}
