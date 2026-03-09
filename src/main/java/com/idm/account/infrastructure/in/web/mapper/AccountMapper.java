package com.idm.account.infrastructure.in.web.mapper;

import com.idm.account.infrastructure.in.web.model.AccountResponse;
import com.idm.account.infrastructure.in.web.model.AccountStatusResponse;
import com.idm.account.infrastructure.in.web.model.BalanceResponse;
import com.idm.account.infrastructure.in.web.model.CreateAccountRequest;
import com.idm.account.domain.model.Account;
import com.idm.account.domain.model.AccountType;
import com.idm.account.domain.model.CurrencyType;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AccountMapper {

    public static Account toDomain(CreateAccountRequest req) {

        return Account.builder()
                .customerId(req.getCustomerId())
                .accountNumber(req.getAccountNumber())
                .accountType(AccountType.valueOf(req.getAccountType().getValue()))
                .currency(CurrencyType.valueOf(req.getCurrency().getValue()))
                .balance(req.getInitialBalance())
                .build();
    }

    public static AccountResponse toApi(Account account) {

        return new AccountResponse()
                .accountId(account.getAccountId())
                .customerId(account.getCustomerId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType().name())
                .currency(account.getCurrency().name())
                .balance(account.getBalance())
                .active(account.getActive())
                .createdAt(account.getCreatedAt());
    }

    public static BalanceResponse balanceToApi(Account account) {
        return new BalanceResponse()
                .accountId(account.getAccountId())
                .accountNumber(account.getAccountNumber())
                .currency(account.getCurrency().name())
                .balance(account.getBalance());
    }

    public static AccountStatusResponse statusToApi(Account account) {
        return new AccountStatusResponse()
                .accountId(account.getAccountId())
                .active(account.getActive());
    }
}
