package com.taily.backend.controller;

import com.taily.backend.dto.identity.IdentityPortraitRequestDto;
import com.taily.backend.dto.identity.IdentityPortraitResponseDto;
import com.taily.backend.service.IdentityPortraitService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/identity")
public class IdentityPortraitController {

  private final IdentityPortraitService identityPortraitService;

  public IdentityPortraitController(IdentityPortraitService identityPortraitService) {
    this.identityPortraitService = identityPortraitService;
  }

  @PostMapping("/portrait")
  public IdentityPortraitResponseDto portrait(@Valid @RequestBody IdentityPortraitRequestDto request) {
    return identityPortraitService.generatePortrait(request);
  }
}
