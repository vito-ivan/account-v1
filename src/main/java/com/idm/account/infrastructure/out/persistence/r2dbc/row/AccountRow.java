package com.idm.account.infrastructure.out.persistence.r2dbc.row;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountRow implements Persistable<UUID> {

    @Id
    @Column("account_id")
    private UUID accountId;

    @Column("customer_id")
    private String customerId;

    @Column("account_number")
    private String accountNumber;

    //@Enumerated(EnumType.STRING)
    @Column("account_type")
    private AccountType accountType;

    //@Enumerated(EnumType.STRING)
    @Column("currency")
    private CurrencyType currency;

    @Column("balance")
    private BigDecimal balance;

    @Column("active")
    private Boolean active;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Transient
    private boolean newRecord;

    @Override
    public UUID getId() {
        return accountId;
    }

    @Override
    public boolean isNew() {
        return newRecord;
    }

}



