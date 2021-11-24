package kitchenpos.product.repository;

import kitchenpos.product.Product;
import kitchenpos.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaProductDao extends JpaRepository<Product, Long> {
}
