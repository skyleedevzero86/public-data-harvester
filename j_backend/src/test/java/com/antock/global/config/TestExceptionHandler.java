package com.antock.global.config;

import com.antock.api.corpsearch.application.dto.request.CorpMastSearchRequest;
import com.antock.api.corpsearch.application.service.CorpMastSearchService;
import com.antock.global.common.exception.BusinessException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class TestExceptionHandler {
    private final CorpMastSearchService corpMastSearchService;

    public TestExceptionHandler(CorpMastSearchService corpMastSearchService) {
        this.corpMastSearchService = corpMastSearchService;
    }

    @ExceptionHandler(BusinessException.class)
    public String handleBusinessException(BusinessException e, Model model) {
        model.addAttribute("errorMessage", e.getMessage());
        model.addAttribute("cities", corpMastSearchService.getAllCities());
        model.addAttribute("searchRequest", new CorpMastSearchRequest());
        return "corp/search";
    }
}