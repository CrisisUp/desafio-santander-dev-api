package me.dio.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.dio.config.AuthenticatedUser;
import me.dio.config.SecurityUtils;
import me.dio.controller.dto.TransactionDto;
import me.dio.controller.dto.TransactionRequestDto;
import me.dio.controller.dto.TransactionTypeSummaryDto;
import me.dio.service.TransactionService;
import me.dio.service.UserService;
import me.dio.service.exception.ForbiddenException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/accounts")
@Tag(name = "Transactions Controller", description = "RESTful API for account statements.")
public record TransactionController(TransactionService transactionService, UserService userService) {

    @GetMapping("/transactions/summary")
    @Operation(summary = "Transaction totals by type",
            description = "Aggregate SUM(amount) and COUNT per transaction type across all accounts (ADMIN only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation successful"),
            @ApiResponse(responseCode = "403", description = "Forbidden: requires ADMIN")
    })
    public ResponseEntity<List<TransactionTypeSummaryDto>> summarizeByType() {
        return ResponseEntity.ok(transactionService.summarizeByType().stream()
                .map(TransactionTypeSummaryDto::new)
                .toList());
    }

    @GetMapping("/{id}/transactions")
    @Operation(summary = "List transactions", description = "Retrieve a paged statement for an account. USER role may only read their own account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation successful"),
            @ApiResponse(responseCode = "403", description = "Forbidden: not the account owner and not ADMIN"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<Page<TransactionDto>> findByAccountId(@PathVariable Long id, Pageable pageable) {
        requireAccountOwnerOrAdmin(id);
        var page = transactionService.findByAccountId(id, pageable);
        return ResponseEntity.ok(page.map(TransactionDto::new));
    }

    @PostMapping("/{id}/transactions")
    @Operation(summary = "Create a transaction", description = "DEPOSIT adds, WITHDRAWAL/PAYMENT/TRANSFER subtract from the balance. USER role may only operate on their own account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transaction created"),
            @ApiResponse(responseCode = "403", description = "Forbidden: not the account owner and not ADMIN"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "422", description = "Invalid transaction data or insufficient funds")
    })
    public ResponseEntity<TransactionDto> create(@PathVariable Long id, @Valid @RequestBody TransactionRequestDto request,
                                                 @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        requireAccountOwnerOrAdmin(id);
        var transaction = transactionService.create(id, request, idempotencyKey);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{tid}")
                .buildAndExpand(transaction.getId())
                .toUri();
        return ResponseEntity.created(location).body(new TransactionDto(transaction));
    }

    /**
     * A USER may only operate on their own account (the banking user whose
     * account.id matches). ADMIN may operate on any account.
     */
    private void requireAccountOwnerOrAdmin(Long accountId) {
        AuthenticatedUser actor = SecurityUtils.currentUser();
        if (actor == null) {
            throw new ForbiddenException();
        }
        if (actor.isAdmin()) {
            return;
        }
        // The token carries the banking user id (tb_user.id); its account must
        // be the target account.
        if (actor.userId() != null) {
            var owner = userService.findById(actor.userId());
            if (owner.getAccount() != null && accountId.equals(owner.getAccount().getId())) {
                return;
            }
        }
        throw new ForbiddenException();
    }
}
