package com.saga.estudo.order.service.dto;

import com.saga.estudo.order.service.config.exception.ValidationException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.ObjectUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventFilters {

    private String orderId;
    private String transactionId;

    public void  validateFilters(){
        if (ObjectUtils.isEmpty(this.orderId)
                && ObjectUtils.isEmpty(this.transactionId)){
            throw new ValidationException("OrderId or TransactionId must be informed");
        }
    }
}
