package kitchenpos.application;

import kitchenpos.dao.JpaMenuGroupDao;
import kitchenpos.domain.MenuGroup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


class MenuGroupServiceTest extends ServiceTest {
    @InjectMocks
    private MenuGroupService menuGroupService;

    @Mock
    private JpaMenuGroupDao menuGroupDao;

    @Test
    @DisplayName("메뉴그룹을 생성한다.")
    void create() {
        when(menuGroupDao.save(any(MenuGroup.class)))
                .thenReturn(menuGroup);

        MenuGroup actual = menuGroupService.create(menuGroup);
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getName()).isEqualTo(menuGroup.getName());
    }

    @Test
    @DisplayName("모든 메뉴그룹을 조회한다.")
    void list() {
        MenuGroup menuGroup2 = new MenuGroup(2L, "그룹2");

        List<MenuGroup> menuGroups = new ArrayList<>();
        menuGroups.add(menuGroup);
        menuGroups.add(menuGroup2);

        when(menuGroupDao.findAll())
                .thenReturn(menuGroups);

        assertThat(menuGroupService.list()).hasSize(2);
    }
}
