package com.example.bankcards.controller;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.service.CardServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardsControllerImpl {
    private final CardServiceImpl cardService;

    @PostMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponseDto> addCard(@Valid @RequestBody CardRequestDto card) {
        return ok(cardService.createCard(card));
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<CardResponseDto> getCard(@PathVariable Long id) {
        return ok(cardService.getCardById(id));
    }


    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Page<CardSearchResponse>> getUserCards(
            @PathVariable Long userId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CardStatus status,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        CardSearchRequest searchRequest = new CardSearchRequest(search, status);
        Page<CardSearchResponse> cards = cardService.getUserCards(userId, searchRequest, pageable);
        return ResponseEntity.ok(cards);
    }


    @GetMapping("/{cardId}/balance")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<BigDecimal> getCardBalance(@PathVariable Long cardId) {
        BigDecimal balance = cardService.getCardBalance(cardId);
        return ResponseEntity.ok(balance);
    }


    @PatchMapping("/{cardId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponseDto> changeCardStatus(
            @PathVariable Long cardId,
            @RequestBody StatusChangeRequest request) {
        CardResponseDto updatedCard = cardService.changeCardStatus(cardId, request);
        return ResponseEntity.ok(updatedCard);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteById(id);
        return ok().build();
    }
}
