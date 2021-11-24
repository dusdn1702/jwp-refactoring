package kitchenpos.menu.dto;

import kitchenpos.menu.domain.Menu;
import kitchenpos.menu.domain.MenuGroup;
import kitchenpos.menu.domain.MenuProduct;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MenuResponse {
    private long id;
    private String name;
    private BigDecimal price;
    private MenuGroupResponse menuGroupResponse;
    private MenuProductsResponse menuProductsResponse;

    public MenuResponse() {
    }

    public MenuResponse(long id, String name, BigDecimal price, MenuGroupResponse menuGroupResponse, MenuProductsResponse menuProductsResponse) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.menuGroupResponse = menuGroupResponse;
        this.menuProductsResponse = menuProductsResponse;
    }

    public static MenuResponse of(Menu menu) {
        MenuGroup menuGroup = menu.getMenuGroup();
        MenuGroupResponse menuGroupResponse = new MenuGroupResponse(menuGroup.getId(), menuGroup.getName());
        List<MenuProduct> menuProducts = menu.getMenuProducts();
        List<MenuProductResponse> menuProductsResponses = new ArrayList<>();
        for (MenuProduct menuProduct : menuProducts) {
            MenuProductResponse menuProductResponse = new MenuProductResponse(menuProduct.getSeq(), menuProduct.getQuantity());
            menuProductsResponses.add(menuProductResponse);
        }
        MenuProductsResponse menuProductsResponse = new MenuProductsResponse(menuProductsResponses);
        return new MenuResponse(menu.getId(), menu.getName(), menu.getPrice(), menuGroupResponse, menuProductsResponse);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public MenuGroupResponse getMenuGroupResponse() {
        return menuGroupResponse;
    }

    public MenuProductsResponse getMenuProductsResponse() {
        return menuProductsResponse;
    }
}
