package kitchenpos.dao;

import kitchenpos.domain.MenuProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaMenuProductDao extends JpaRepository<MenuProduct, Long> {
    List<MenuProduct> findAllByMenu_Id(Long id);
}
