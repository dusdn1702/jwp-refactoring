package kitchenpos.application;

import kitchenpos.dao.JpaProductDao;
import kitchenpos.domain.Product;
import kitchenpos.exception.KitchenposException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
public class ProductService {
    private final JpaProductDao productDao;

    public ProductService(final JpaProductDao productDao) {
        this.productDao = productDao;
    }

    @Transactional
    public Product create(final Product product) {
        final BigDecimal price = product.getPrice();

        if (Objects.isNull(price) || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new KitchenposException(KitchenposException.ILLEGAL_PRICE);
        }

        return productDao.save(product);
    }

    public List<Product> list() {
        return productDao.findAll();
    }
}
