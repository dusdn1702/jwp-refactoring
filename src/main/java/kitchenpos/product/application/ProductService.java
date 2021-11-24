package kitchenpos.product.application;

import kitchenpos.product.repository.JpaProductDao;
import kitchenpos.product.domain.Product;
import kitchenpos.product.dto.ProductRequest;
import kitchenpos.product.dto.ProductResponse;
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
    public ProductResponse create(final ProductRequest productRequest) {
        Product product = Product.of(productRequest);
        final BigDecimal price = product.getPrice();

        if (Objects.isNull(price) || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new KitchenposException(KitchenposException.ILLEGAL_PRICE);
        }

        Product savedProduct = productDao.save(product);
        return ProductResponse.of(savedProduct);
    }

    public List<Product> list() {
        return productDao.findAll();
    }
}
