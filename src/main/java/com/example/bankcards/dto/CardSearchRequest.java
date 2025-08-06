package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.CardStatus;

public record CardSearchRequest(
        String searchTerm,  // для поиска по номеру карты
        CardStatus status   // для фильтрации по статусу (опционально)
) {}
