package com.antock.web.coseller.presentation;

import com.antock.api.coseller.application.dto.RegionRequestDto;
import com.antock.api.coseller.application.service.CoSellerService;
import com.antock.api.coseller.value.City;
import com.antock.api.coseller.value.District;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/coseller/manual")
public class CoSellerWebController {
    private final CoSellerService coSellerService;

    @GetMapping
    public String showManualForm(Model model) {
        model.addAttribute("cities", Arrays.asList(City.values()));
        model.addAttribute("districts", Arrays.asList(District.values()));
        return "coseller/manual";
    }
}