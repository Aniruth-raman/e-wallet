package com.ewallet.notification.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ewallet.notification.entity.NotificationEntity;

@Repository
public interface NotificationRepo extends JpaRepository<NotificationEntity, Long> {
	
	
}
