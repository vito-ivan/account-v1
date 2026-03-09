package com.idm.account.infrastructure.in.web;

import com.idm.account.infrastructure.in.web.api.AccountsApiDelegate;
import com.idm.account.infrastructure.in.web.mapper.AccountMapper;
import com.idm.account.infrastructure.in.web.model.AccountResponse;
import com.idm.account.infrastructure.in.web.model.AccountStatusResponse;
import com.idm.account.infrastructure.in.web.model.BalanceResponse;
import com.idm.account.infrastructure.in.web.model.CreateAccountRequest;
import com.idm.account.infrastructure.in.web.model.UpdateBalanceRequest;
import com.idm.account.application.UpdateAccountBalanceService;
import com.idm.account.domain.port.in.GetAccountBalanceUseCase;
import com.idm.account.domain.port.in.GetAccountByIdUseCase;
import com.idm.account.domain.port.in.GetAccountByNumberUseCase;
import com.idm.account.domain.port.in.RegisterAccountUseCase;
import com.idm.account.domain.port.in.ValidateAccountStatusUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountsApiDelegateImpl implements AccountsApiDelegate {

    private final RegisterAccountUseCase registerCustomerUseCase;
    private final GetAccountByNumberUseCase getAccountByNumberUseCase;
    private final GetAccountByIdUseCase getAccountByIdUseCase;
    private final GetAccountBalanceUseCase getAccountBalanceUseCase;
    private final ValidateAccountStatusUseCase validateAccountStatusUseCase;
    private final UpdateAccountBalanceService updateAccountBalanceService;

    @Override
    public Mono<ResponseEntity<AccountResponse>> createAccount(Mono<CreateAccountRequest> customerCreate,
                                                               ServerWebExchange exchange) {
        return customerCreate
                .map(AccountMapper::toDomain)
                .flatMap(registerCustomerUseCase::create)
                .map(AccountMapper::toApi)
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @Override
    public Mono<ResponseEntity<BalanceResponse>> getAccountBalance(UUID accountId,
                                                                   ServerWebExchange exchange) {
        return getAccountBalanceUseCase.getAccountBalance(accountId)
                .map(AccountMapper::balanceToApi)
                .map(ResponseEntity::ok);

    }

    @Override
    public Mono<ResponseEntity<Void>> updateAccountBalance(UUID accountId,
                                                           Mono<UpdateBalanceRequest> updateBalanceRequest,
                                                           ServerWebExchange exchange) {

        return updateBalanceRequest
                .flatMap(request -> updateAccountBalanceService
                        .updateAccountBalance(accountId, request.getBalance()))
                .then(Mono.just(ResponseEntity.noContent().build()));

    }

    @Override
    public Mono<ResponseEntity<AccountResponse>> getAccountById(UUID accountId,
                                                                ServerWebExchange exchange) {
        return getAccountByIdUseCase.getAccountById(accountId)
                .map(AccountMapper::toApi)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<AccountResponse>> getAccountByNumber(String accountNumber,
                                                                    ServerWebExchange exchange) {
        return getAccountByNumberUseCase.getAccountByNumber(accountNumber)
                .map(AccountMapper::toApi)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<AccountStatusResponse>> validateAccountStatus(UUID accountId,
                                                                             ServerWebExchange exchange) {
        return validateAccountStatusUseCase.validateAccountStatus(accountId)
                .map(AccountMapper::statusToApi)
                .map(ResponseEntity::ok);
    }

}
