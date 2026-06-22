package com.sorokaandriy.price_fetcher.repository;

import com.sorokaandriy.price_fetcher.entity.Coin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoinRepository extends JpaRepository<Coin,Long> {

    List<Coin> findBySymbolOrderByTimestampDesc(String symbol);

}
