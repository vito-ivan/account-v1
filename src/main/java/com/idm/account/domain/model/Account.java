package com.idm.account.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    private UUID accountId;
    private String customerId;
    private String accountNumber;
    private AccountType accountType;
    private CurrencyType currency;
    private BigDecimal balance;
    private Boolean active;
    private LocalDateTime createdAt;
}



