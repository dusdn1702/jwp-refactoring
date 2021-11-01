package kitchenpos.dao;

import kitchenpos.domain.OrderLineItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaOrderLineItemDao extends JpaRepository<OrderLineItem, Long> {
    List<OrderLineItem> findAllByOrder_Id(Long id);
}
