package com.example.bankcards.controller;

import com.example.bankcards.dto.TransactionRequestDto;
import com.example.bankcards.dto.TransactionResponseDto;
import com.example.bankcards.service.TransactionServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionsControllerImpl {
    private final TransactionServiceImpl transactionService;

    @PostMapping()
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<TransactionResponseDto> addTransfer(@RequestBody TransactionRequestDto transactionRequestDto) {
        return ok(transactionService.createTransfer(transactionRequestDto));
    }

   @GetMapping("/{cardNumber}")
   @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<TransactionResponseDto>> getCardTransactions(
            @PathVariable String cardNumber
    ) {
        return ok(transactionService.getTransactionsByCardId(cardNumber));
    }
}
