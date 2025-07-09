package com.antock.global.config;

import com.antock.api.corpmanual.application.dto.request.CorpMastManualRequest;
import com.antock.api.corpmanual.application.service.CorpMastManualService;
import com.antock.global.common.exception.BusinessException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class TestExceptionHandler {
    private final CorpMastManualService corpMastSearchService;

    public TestExceptionHandler(CorpMastManualService corpMastSearchService) {
        this.corpMastSearchService = corpMastSearchService;
    }

    @ExceptionHandler(BusinessException.class)
    public String handleBusinessException(BusinessException e, Model model) {
        model.addAttribute("errorMessage", e.getMessage());
        model.addAttribute("cities", corpMastSearchService.getAllCities());
        model.addAttribute("searchRequest", new CorpMastManualRequest());
        return "corp/search";
    }
}