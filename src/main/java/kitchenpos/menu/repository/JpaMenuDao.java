package kitchenpos.menu.repository;

import kitchenpos.menu.domain.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaMenuDao extends JpaRepository<Menu, Long> {
    long countByIdIn(List<Long> menuIds);
}
