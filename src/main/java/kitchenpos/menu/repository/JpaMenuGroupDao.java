package kitchenpos.menu.repository;

import kitchenpos.menu.domain.MenuGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaMenuGroupDao extends JpaRepository<MenuGroup, Long> {
}
