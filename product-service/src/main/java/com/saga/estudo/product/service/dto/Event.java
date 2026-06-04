package com.saga.estudo.product.service.dto;




import com.saga.estudo.product.service.config.exception.ValidationException;
import com.saga.estudo.product.service.enums.ESagaStatus;
import lombok.*;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Event {
    private String id;
    private String transactionId;
    private String orderId;
    private Order payload;
    private String source;
    private ESagaStatus status;
    private List<History> eventHistory;
    private LocalDateTime createdAt;


    public void validate(){
        if (ObjectUtils.isEmpty(transactionId)
                || ObjectUtils.isEmpty(orderId)
                || ObjectUtils.isEmpty(payload)){
            throw new ValidationException("TransactionId and OrderId and Order must be informed.");
        }
        payload.validate();
    }

    public void addToHistory(History history) {
        if (ObjectUtils.isEmpty(eventHistory)){
            eventHistory = new ArrayList<History>();
        }
        eventHistory.add(history);
    }
}

