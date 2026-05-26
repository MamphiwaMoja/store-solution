package com.example.store.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateCustomerRequest(@NotBlank String name) {}
