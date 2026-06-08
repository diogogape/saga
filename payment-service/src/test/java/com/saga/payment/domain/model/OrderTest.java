package com.saga.payment.domain.model;

import com.saga.payment.domain.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Order (domínio)")
class OrderTest {

    private OrderProduct orderProduct(String code, double unitValue, long quantity) {
        Product product = Product.builder().code(code).unitValue(unitValue).build();
        return OrderProduct.builder().product(product).quantity(quantity).build();
    }

    @Test
    @DisplayName("calculateAmount soma quantidade * valor unitário de cada item")
    void calculateAmount_shouldSumItems() {
        Order order = Order.builder()
                .products(List.of(
                        orderProduct("A", 10.0, 2),   // 20.0
                        orderProduct("B", 5.5, 4)))    // 22.0
                .build();

        assertThat(order.calculateAmount()).isEqualTo(42.0);
        assertThat(order.getTotalAmount()).isEqualTo(42.0);
    }

    @Test
    @DisplayName("calculateTotalItems soma as quantidades")
    void calculateTotalItems_shouldSumQuantities() {
        Order order = Order.builder()
                .products(List.of(
                        orderProduct("A", 10.0, 2),
                        orderProduct("B", 5.5, 4)))
                .build();

        assertThat(order.calculateTotalItems()).isEqualTo(6L);
        assertThat(order.getTotalItems()).isEqualTo(6L);
    }

    @Test
    @DisplayName("validate aceita pedido com produtos válidos")
    void validate_shouldPassForValidOrder() {
        Order order = Order.builder()
                .products(List.of(orderProduct("A", 10.0, 1)))
                .build();

        order.validate(); // não deve lançar
    }

    @Test
    @DisplayName("validate lança exceção quando não há produtos")
    void validate_shouldFailWhenNoProducts() {
        Order order = Order.builder().products(List.of()).build();

        assertThatThrownBy(order::validate)
                .isInstanceOf(ValidationException.class)
                .hasMessage("Products not informed.");
    }

    @Test
    @DisplayName("validate lança exceção quando a lista de produtos é nula")
    void validate_shouldFailWhenProductsNull() {
        Order order = Order.builder().build();

        assertThatThrownBy(order::validate)
                .isInstanceOf(ValidationException.class);
    }
}
