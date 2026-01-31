package com.ewallet.wallet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ewallet.wallet.entity.WalletUser;

@Repository
public interface UserRepository extends JpaRepository<WalletUser, String> {

}
