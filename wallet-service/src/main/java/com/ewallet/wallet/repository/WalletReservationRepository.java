package com.ewallet.wallet.repository;

import com.ewallet.wallet.entity.WalletReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WalletReservationRepository extends JpaRepository<WalletReservation, UUID> {
}
