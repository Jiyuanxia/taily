package com.taily.backend.controller;

import com.taily.backend.dto.trip.TripGenerateWithFitRequestDto;
import com.taily.backend.dto.trip.TripGenerateWithFitResponseDto;
import com.taily.backend.dto.trip.TripRefineRequestDto;
import com.taily.backend.service.TripGenerateWithFitService;
import com.taily.backend.service.TripRefineService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TripController {
  private final TripGenerateWithFitService tripGenerateWithFitService;
  private final TripRefineService tripRefineService;

  public TripController(TripGenerateWithFitService tripGenerateWithFitService, TripRefineService tripRefineService) {
    this.tripGenerateWithFitService = tripGenerateWithFitService;
    this.tripRefineService = tripRefineService;
  }

  @PostMapping("/trip/generate-with-fit")
  public TripGenerateWithFitResponseDto generateWithFit(
      @Valid @RequestBody TripGenerateWithFitRequestDto request) {
    return tripGenerateWithFitService.generateWithFit(request);
  }

  @PostMapping("/trip/refine")
  public TripGenerateWithFitResponseDto refine(@Valid @RequestBody TripRefineRequestDto request) {
    return tripRefineService.refine(request);
  }
}

