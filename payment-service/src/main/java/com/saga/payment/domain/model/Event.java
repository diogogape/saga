package com.saga.payment.domain.model;




import com.saga.payment.domain.exception.ValidationException;
import com.saga.payment.domain.enums.ESagaStatus;
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

    public void addToHistory(String message) {
        if (ObjectUtils.isEmpty(eventHistory)){
            eventHistory = new ArrayList<History>();
        }
        History history = History.builder()
                .source(this.getSource())
                .status(this.getStatus())
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();

        eventHistory.add(history);
    }

    public Double calculateAmount() {
        return payload.calculateAmount();
    }

    public Long calculateTotalItems() {
        return payload.calculateTotalItems();
    }

    public void updateEvent(String source, ESagaStatus status, String message){
        this.source = source;
        this.status = status;
        addToHistory(message);
    }
}
