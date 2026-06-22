package com.be.dto.request.customer;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutRequest {
    @NotNull(message = "Customer ID is required")
    private Long customerId;

    private Long shippingAddressId;

    @NotNull(message = "Payment method is required")
    private String paymentMethod; // "COD" or "WALLET"

    @NotEmpty(message = "Items list cannot be empty")
    private List<CheckoutItem> items;
}
