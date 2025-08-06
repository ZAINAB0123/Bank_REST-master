package com.example.bankcards.controller;

import com.example.bankcards.entity.Role;
import com.example.bankcards.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
public class TestController {

    private final RoleRepository roleRepository;

    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        log.info("Getting all roles");
        List<Role> roles = roleRepository.findAll();
        log.info("Found {} roles", roles.size());
        for (Role role : roles) {
            log.info("Role: {} - {}", role.getId(), role.getDescription());
        }
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/roles/user")
    public ResponseEntity<Role> getUserRole() {
        log.info("Looking for USER role");
        Role userRole = roleRepository.findById("USER")
                .orElse(null);
        if (userRole != null) {
            log.info("Found USER role: {} - {}", userRole.getId(), userRole.getDescription());
        } else {
            log.warn("USER role not found");
        }
        return ResponseEntity.ok(userRole);
    }
} 