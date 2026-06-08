package com.saga.payment.domain.model;

import com.saga.payment.domain.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OrderProduct / Product (domínio)")
class OrderProductTest {

    @Test
    @DisplayName("OrderProduct válido não lança")
    void validate_shouldPass() {
        Product product = Product.builder().code("A").unitValue(10.0).build();
        OrderProduct orderProduct = OrderProduct.builder().product(product).quantity(1L).build();

        assertThatCode(orderProduct::validate).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("OrderProduct sem quantidade lança exceção")
    void validate_shouldFailWhenQuantityMissing() {
        Product product = Product.builder().code("A").unitValue(10.0).build();
        OrderProduct orderProduct = OrderProduct.builder().product(product).build();

        assertThatThrownBy(orderProduct::validate)
                .isInstanceOf(ValidationException.class)
                .hasMessage("Product or quantity not informed");
    }

    @Test
    @DisplayName("Product sem code lança exceção")
    void product_shouldFailWhenCodeMissing() {
        Product product = Product.builder().unitValue(10.0).build();

        assertThatThrownBy(product::validate)
                .isInstanceOf(ValidationException.class)
                .hasMessage("Product code or unitValue not informed");
    }
}
