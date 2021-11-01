package kitchenpos.dao;

import kitchenpos.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaOrderDao extends JpaRepository<Order, Long> {
    boolean existsByOrderTable_IdAndOrderStatusIn(Long orderTableId, List<String> asList);

    boolean existsByOrderTableIdInAndOrderStatusIn(List<Long> orderTableIds, List<String> asList);
}
