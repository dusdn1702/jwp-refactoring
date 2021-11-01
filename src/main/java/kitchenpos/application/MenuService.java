package kitchenpos.application;

import kitchenpos.dao.*;
import kitchenpos.domain.Menu;
import kitchenpos.domain.MenuProduct;
import kitchenpos.domain.Product;
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
            final JpaProductDao productDao
    ) {
        this.menuDao = menuDao;
        this.menuGroupDao = menuGroupDao;
        this.menuProductDao = menuProductDao;
        this.productDao = productDao;
    }

    @Transactional
    public Menu create(final Menu menu) {
        final BigDecimal price = menu.getPrice();

        if (Objects.isNull(price) || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new KitchenposException(ILLEGAL_PRICE);
        }

        if (!menuGroupDao.existsById(menu.getMenuGroupId())) {
            throw new KitchenposException(ILLEGAL_MENU_GROUP_ID);
        }

        final List<MenuProduct> menuProducts = menu.getMenuProducts();

        BigDecimal sum = BigDecimal.ZERO;
        for (final MenuProduct menuProduct : menuProducts) {
            final Product product = productDao.findById(menuProduct.getProductId())
                    .orElseThrow(() -> new KitchenposException(ILLEGAL_PRODUCT_ID));
            sum = sum.add(product.getPrice().multiply(BigDecimal.valueOf(menuProduct.getQuantity())));
        }

        if (price.compareTo(sum) > 0) {
            throw new KitchenposException(IMPOSSIBLE_MENU_PRICE);
        }

        final Menu savedMenu = menuDao.save(menu);

        final Long menuId = savedMenu.getId();
        final List<MenuProduct> savedMenuProducts = new ArrayList<>();
        for (final MenuProduct menuProduct : menuProducts) {
            menuProduct.setMenuId(menuId);
            savedMenuProducts.add(menuProductDao.save(menuProduct));
        }
        savedMenu.setMenuProducts(savedMenuProducts);

        return savedMenu;
    }

    public List<Menu> list() {
        final List<Menu> menus = menuDao.findAll();

        for (final Menu menu : menus) {
            menu.setMenuProducts(menuProductDao.findAllByMenu_Id(menu.getId()));
        }

        return menus;
    }
}
