package kitchenpos.dto;

import java.util.List;

public class MenuProductsResponse {
    private List<MenuProductResponse> menuProductResponses;

    public MenuProductsResponse() {
    }

    public MenuProductsResponse(List<MenuProductResponse> menuProductResponses) {
        this.menuProductResponses = menuProductResponses;
    }

    public List<MenuProductResponse> getMenuProductResponses() {
        return menuProductResponses;
    }
}
