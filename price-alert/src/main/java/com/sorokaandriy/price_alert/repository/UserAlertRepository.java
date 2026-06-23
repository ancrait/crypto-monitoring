package com.sorokaandriy.price_alert.repository;

import com.sorokaandriy.price_alert.entity.UserAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.ScopedValue;
import java.util.List;
import java.util.Optional;

public interface UserAlertRepository extends JpaRepository<UserAlert, Long> {

    List<UserAlert> findBySymbolAndEnabledTrue(String symbol);

    List<UserAlert> findByChatId(Long chatId);

    Optional<UserAlert> findByChatIdAndSymbol(Long chatId, String symbol);
}
