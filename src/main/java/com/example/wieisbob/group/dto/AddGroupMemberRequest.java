package com.example.wieisbob.group.dto;

import jakarta.validation.constraints.NotNull;

public record AddGroupMemberRequest(@NotNull Long userId) {}
