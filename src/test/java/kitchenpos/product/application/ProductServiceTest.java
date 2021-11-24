package kitchenpos.product.application;

import kitchenpos.product.repository.JpaProductDao;
import kitchenpos.product.domain.Product;
import kitchenpos.product.dto.ProductRequest;
import kitchenpos.product.dto.ProductResponse;
import kitchenpos.exception.KitchenposException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static kitchenpos.exception.KitchenposException.ILLEGAL_PRICE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ProductServiceTest extends ServiceTest {
    @InjectMocks
    private ProductService productService;

    @Mock
    private JpaProductDao productDao;

    @Test
    @DisplayName("상품을 생성한다.")
    void create() {
        ProductRequest productRequest = new ProductRequest(product.getName(), product.getPrice());

        when(productDao.save(any(Product.class)))
                .thenReturn(product);

        ProductResponse actual = productService.create(productRequest);
        assertThat(actual.getId()).isNotNull();
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -1000})
    @DisplayName("상품의 가격이 0보다 작으면 예외가 발생한다.")
    void createExceptionMinusPrice(Integer price) {
        product = new Product(product.getId(), product.getName(), BigDecimal.valueOf(price));
        ProductRequest productRequest = new ProductRequest(product.getName(), product.getPrice());

        assertThatThrownBy(() -> productService.create(productRequest))
                .isInstanceOf(KitchenposException.class)
                .hasMessage(ILLEGAL_PRICE);
    }

    @ParameterizedTest
    @NullSource
    @DisplayName("상품의 가격이 없으면 예외가 발생한다.")
    void createExceptionEmptyPrice(BigDecimal price) {
        product = new Product(product.getId(), product.getName(), price);
        ProductRequest productRequest = new ProductRequest(product.getName(), product.getPrice());

        assertThatThrownBy(() -> productService.create(productRequest))
                .isInstanceOf(KitchenposException.class)
                .hasMessage(ILLEGAL_PRICE);
    }

    @Test
    @DisplayName("모든 상품을 조회한다.")
    void list() {
        Product product2 = new Product(2L, "단무지", BigDecimal.valueOf(100));

        List<Product> products = new ArrayList<>();
        products.add(product);
        products.add(product2);

        when(productDao.findAll())
                .thenReturn(products);

        assertThat(productService.list()).usingRecursiveComparison()
                .isEqualTo(products);
    }
}
