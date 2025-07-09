package com.antock.api.corpmanual.application.service;

import com.antock.api.corpmanual.application.dto.request.CorpMastManualRequest;
import com.antock.api.corpmanual.application.dto.response.CorpMastManualResponse;
import com.antock.api.corpmanual.infrastructure.CorpMastManualRepository;
import com.antock.api.coseller.domain.CorpMast;
import com.antock.global.utils.ExcelExportUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CorpMastManualExcelService {

    private final CorpMastManualRepository corpMastManualRepository;

    public void exportToExcel(CorpMastManualRequest request, OutputStream os) throws Exception {

        List<CorpMast> corpList = corpMastManualRepository.findBySearchConditions(
                request.getBizNmForSearch(),
                request.getBizNoForSearch(),
                request.getSellerIdForSearch(),
                request.getCorpRegNoForSearch(),
                request.getCityForSearch(),
                request.getDistrictForSearch(),
                PageRequest.of(0, 5000)
        ).getContent();

        List<CorpMastManualResponse> responseList = corpList.stream()
                .map(CorpMastManualResponse::from)
                .collect(Collectors.toList());

        ExcelExportUtil.writeCorpListToExcel(responseList, os);
    }
}