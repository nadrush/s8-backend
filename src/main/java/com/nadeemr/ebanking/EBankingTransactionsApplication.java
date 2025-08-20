package com.nadeemr.ebanking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableKafka
@EnableCaching
@EnableAsync
public class EBankingTransactionsApplication {

    public static void main(String[] args) {
        SpringApplication.run(EBankingTransactionsApplication.class, args);
    }
}
