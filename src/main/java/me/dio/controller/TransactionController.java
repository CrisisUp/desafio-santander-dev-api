package me.dio.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.dio.controller.dto.TransactionDto;
import me.dio.controller.dto.TransactionRequestDto;
import me.dio.controller.dto.TransactionTypeSummaryDto;
import me.dio.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/accounts")
@Tag(name = "Transactions Controller", description = "RESTful API for account statements.")
public record TransactionController(TransactionService transactionService) {

    @GetMapping("/transactions/summary")
    @Operation(summary = "Transaction totals by type",
            description = "Aggregate SUM(amount) and COUNT per transaction type across all accounts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation successful")
    })
    public ResponseEntity<List<TransactionTypeSummaryDto>> summarizeByType() {
        return ResponseEntity.ok(transactionService.summarizeByType().stream()
                .map(TransactionTypeSummaryDto::new)
                .toList());
    }

    @GetMapping("/{id}/transactions")
    @Operation(summary = "List transactions", description = "Retrieve a paged statement for an account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation successful")
    })
    public ResponseEntity<Page<TransactionDto>> findByAccountId(@PathVariable Long id, Pageable pageable) {
        var page = transactionService.findByAccountId(id, pageable);
        return ResponseEntity.ok(page.map(TransactionDto::new));
    }

    @PostMapping("/{id}/transactions")
    @Operation(summary = "Create a transaction", description = "DEPOSIT adds, WITHDRAWAL/PAYMENT/TRANSFER subtract from the balance")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transaction created"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "422", description = "Invalid transaction data or insufficient funds")
    })
    public ResponseEntity<TransactionDto> create(@PathVariable Long id, @RequestBody TransactionRequestDto request) {
        var transaction = transactionService.create(id, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{tid}")
                .buildAndExpand(transaction.getId())
                .toUri();
        return ResponseEntity.created(location).body(new TransactionDto(transaction));
    }
}
