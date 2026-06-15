package com.saga.payment.infrastructure.utils;

import com.saga.payment.domain.enums.ESagaStatus;
import com.saga.payment.domain.model.Event;
import com.saga.payment.domain.model.Order;
import com.saga.payment.domain.model.OrderProduct;
import com.saga.payment.domain.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JsonUtil (serialização)")
class JsonUtilTest {

    private JsonUtil jsonUtil;

    @BeforeEach
    void setUp() {
        jsonUtil = new JsonUtil(new ObjectMapper());
    }

    private Event sampleEvent() {
        Product product = Product.builder().code("A").unitValue(10.0).build();
        OrderProduct orderProduct = OrderProduct.builder().product(product).quantity(2L).build();
        Order order = Order.builder().id("order-1").products(List.of(orderProduct)).build();
        return Event.builder()
                .id("evt-1")
                .transactionId("tx-1")
                .orderId("order-1")
                .source("PAYMENT_SERVICE")
                .status(ESagaStatus.SUCCESS)
                .payload(order)
                .build();
    }

    @Test
    @DisplayName("toJson e toEvent fazem round-trip preservando os dados")
    void roundTrip_shouldPreserveData() {
        Event original = sampleEvent();

        String json = jsonUtil.toJson(original);
        Event parsed = jsonUtil.toEvent(json);

        assertThat(json).contains("tx-1").contains("order-1").contains("PAYMENT_SERVICE");
        assertThat(parsed.getId()).isEqualTo("evt-1");
        assertThat(parsed.getTransactionId()).isEqualTo("tx-1");
        assertThat(parsed.getOrderId()).isEqualTo("order-1");
        assertThat(parsed.getSource()).isEqualTo("PAYMENT_SERVICE");
        assertThat(parsed.getStatus()).isEqualTo(ESagaStatus.SUCCESS);
        assertThat(parsed.getPayload().getProducts()).hasSize(1);
        assertThat(parsed.getPayload().getProducts().get(0).getProduct().getCode()).isEqualTo("A");
    }

    @Test
    @DisplayName("toEvent lança exceção para JSON inválido (não engole o erro)")
    void toEvent_shouldThrowOnInvalidJson() {
        assertThatThrownBy(() -> jsonUtil.toEvent("isto não é json"))
                .isInstanceOf(Exception.class);
    }
}
