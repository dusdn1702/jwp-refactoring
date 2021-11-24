package kitchenpos.order.repository;

import kitchenpos.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaOrderDao extends JpaRepository<Order, Long> {
    boolean existsByOrderTable_IdAndOrderStatusIn(Long orderTableId, List<String> asList);

    boolean existsByOrderTable_IdInAndOrderStatusIn(List<Long> orderTableIds, List<String> asList);
}
