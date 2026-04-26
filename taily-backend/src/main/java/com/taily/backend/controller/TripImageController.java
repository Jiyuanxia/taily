package com.taily.backend.controller;

import com.taily.backend.dto.trip.TripImageRequestDto;
import com.taily.backend.dto.trip.TripImageResponseDto;
import com.taily.backend.service.TripImageService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/trip")
public class TripImageController {
  private final TripImageService tripImageService;

  public TripImageController(TripImageService tripImageService) {
    this.tripImageService = tripImageService;
  }

  @PostMapping("/image")
  public TripImageResponseDto image(@Valid @RequestBody TripImageRequestDto request) {
    return tripImageService.generateTripImage(request);
  }
}

