package com.ewallet.wallet.dto;

import com.ewallet.wallet.entity.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {
    private UUID reservationId;
    private ReservationStatus status;
    private String message;
}
