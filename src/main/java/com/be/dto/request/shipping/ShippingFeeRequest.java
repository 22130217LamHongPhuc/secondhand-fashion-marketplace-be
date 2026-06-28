package com.be.dto.request.shipping;

import jakarta.validation.Valid;
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
public class ShippingFeeRequest {
    private Long customerId;

    @NotNull(message = "Shipping address ID is required")
    private Long shippingAddressId;

    @NotEmpty(message = "Items list cannot be empty")
    private List<@Valid ShippingFeeItemRequest> items;
}
