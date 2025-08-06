package com.example.bankcards.mapper;

import com.example.bankcards.dto.CardRequestDto;
import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.dto.CardSearchRequest;
import com.example.bankcards.dto.CardSearchResponse;
import com.example.bankcards.entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CardMapper {

    Card requestToCard(CardRequestDto cardRequestDto);

    CardResponseDto cardToCardResponseDto(Card card);
    @Mapping(target = "maskedCardNumber", expression = "java(maskCardNumber(card.getCardNumber()))")
    CardSearchResponse cardToCardSearchResponse(Card card);

    default String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 8) return "****";
        return "****" + cardNumber.substring(cardNumber.length() - 4);
    }
}
