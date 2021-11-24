package kitchenpos.menu.application;

import kitchenpos.menu.repository.JpaMenuGroupDao;
import kitchenpos.menu.domain.MenuGroup;
import kitchenpos.menu.dto.MenuGroupRequest;
import kitchenpos.menu.dto.MenuGroupResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MenuGroupService {
    private final JpaMenuGroupDao menuGroupDao;

    public MenuGroupService(final JpaMenuGroupDao menuGroupDao) {
        this.menuGroupDao = menuGroupDao;
    }

    @Transactional
    public MenuGroupResponse create(final MenuGroupRequest menuGroupRequest) {
        MenuGroup savedMenuGroup = menuGroupDao.save(MenuGroup.of(menuGroupRequest));
        return MenuGroupResponse.of(savedMenuGroup);
    }

    public List<MenuGroup> list() {
        return menuGroupDao.findAll();
    }
}
