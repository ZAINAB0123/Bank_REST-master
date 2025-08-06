package com.example.bankcards.service;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.EntityNotFoundException;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j

public class CardServiceImpl {
    private final CardRepository cardRepository;
    private final CardMapper cardMapper;
    private final UserRepository userRepository;

    public CardResponseDto createCard(CardRequestDto cardRequestDto) {
        log.info("Creating new card");
        return cardMapper.cardToCardResponseDto(cardRepository.save(cardMapper.requestToCard(cardRequestDto)));
    }

    @Transactional(readOnly = true)
    public BigDecimal getCardBalance(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Карта с id %d не найдена", cardId)));

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new IllegalStateException("Нельзя получить баланс заблокированной карты");
        }

        if (card.getStatus() == CardStatus.EXPIRED) {
            throw new IllegalStateException("Нельзя получить баланс просроченной карты");
        }

        return card.getBalance();
    }

    public CardResponseDto getCardById(Long id) {
        log.info("Fetching card with id: {}", id);
        Card card = cardRepository.findById(id).orElseThrow(() -> new CardNotFoundException("Card not found with id: " + id));
        return cardMapper.cardToCardResponseDto(card);
    }

    @Transactional(readOnly = true)
    public Page<CardSearchResponse> getUserCards(Long userId, CardSearchRequest request, Pageable pageable) {
        Specification<Card> spec = Specification.where((root, query, cb) ->
                cb.equal(root.get("owner").get("id"), userId));

        if (request.searchTerm() != null && !request.searchTerm().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(root.get("cardNumber"), "%" + request.searchTerm() + "%"));
        }

        if (request.status() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), request.status()));
        }

        Page<Card> cards = cardRepository.findAll(spec, pageable);

        log.info("Запрошены карты пользователя с id {}. Найдено {} карт",
                userId, cards.getTotalElements());

        return cards.map(cardMapper::cardToCardSearchResponse);
    }

    @Transactional
    public CardResponseDto changeCardStatus(Long cardId, StatusChangeRequest request) {
        CardStatus newStatus;
        try {
            newStatus = CardStatus.valueOf(request.status().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Недопустимый статус карты. Допустимые значения: ACTIVE, BLOCKED, EXPIRED");
        }

        if (newStatus != CardStatus.ACTIVE && newStatus != CardStatus.BLOCKED) {
            throw new IllegalArgumentException("Можно изменить статус только на ACTIVE или BLOCKED");
        }

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Карта с id %d не найдена", cardId)));

        if (card.getStatus() == CardStatus.EXPIRED) {
            throw new IllegalStateException("Нельзя изменить статус просроченной карты");
        }

        if (card.getStatus() == newStatus) {
            throw new IllegalStateException(
                    String.format("Карта уже имеет статус: %s", newStatus));
        }

        card.setStatus(newStatus);
        Card updatedCard = cardRepository.save(card);

        log.info("Изменен статус карты. ID карты: {}, Новый статус: {}", cardId, newStatus);

        return cardMapper.cardToCardResponseDto(updatedCard);
    }

    public void deleteById(Long id) {
        log.info("Deleting card with id: {}", id);
        if (cardRepository.existsById(id)) {
            cardRepository.deleteById(id);
        } else throw new CardNotFoundException("Card not found with id: " + id);
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void updateExpiredCardStatuses() {
        LocalDate today = LocalDate.now();
        List<Card> activeCards = cardRepository.findByStatus(CardStatus.ACTIVE);

        if (activeCards.isEmpty()) {
            log.info("No active cards found to check.");
            return;
        }

        List<Card> expiredCards = activeCards.stream()
                .filter(card -> card.getExpirationDate().isBefore(today))
                .peek(card -> {
                    card.setStatus(CardStatus.EXPIRED);
                    card.setBalance(BigDecimal.ZERO);
                    log.warn("Card ID={} expired on {} — marked as EXPIRED and balance set to 0.",
                            card.getId(), card.getExpirationDate());
                })
                .toList();

        if (expiredCards.isEmpty()) {
            log.info("No cards expired today.");
            return;
        }

        cardRepository.saveAll(expiredCards);
        log.info("Updated {} expired card(s).", expiredCards.size());
    }
}
