package com.saga.payment.domain.model;

import com.saga.payment.domain.enums.EPaymentStatus;
import com.saga.payment.domain.exception.ValidationException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Payment {

    public static final double MIN_AMOUNT = 0.1;

    private Integer id;

    private String orderId;

    private String transactionId;


    private Long totalItems;


    private Double totalAmount;


    private EPaymentStatus status;


    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


    public void createPending(Long totalItems, Double totalAmount, String orderId, String transactionId){
        this.totalAmount = totalAmount;
        this.totalItems = totalItems;
        this.orderId = orderId;
        this.transactionId = transactionId;
        this.status =  EPaymentStatus.PENDING;
    }

    public void validatePayment(){
        if (this.totalAmount< MIN_AMOUNT){
            throw  new ValidationException("Total Amount Invalid");
        }
        this.status = EPaymentStatus.SUCCESS;
    }

    public void refund(){
        this.status = EPaymentStatus.REFUND;
    }

}
