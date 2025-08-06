package com.example.bankcards.dto;

public record UserRequestDto(String username,
                             String password,
                             String roleId) {
}

