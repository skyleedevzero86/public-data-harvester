package com.antock.api.coseller.application.service;

import com.antock.api.coseller.application.dto.CorpMastCreateDTO;
import com.antock.api.coseller.application.dto.RegionRequestDto;
import com.antock.api.coseller.application.dto.api.BizCsvInfoDto;
import com.antock.api.coseller.domain.CorpMast;
import com.antock.api.coseller.infrastructure.CorpMastStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class CoSellerService {

    private final CoSellerCsvProcessor csvProcessor;
    private final CoSellerDataMapper dataMapper;
    private final CoSellerStorageService storageService;
    private final CoSellerApiService apiService;
    private final CsvService csvService;

    public CsvService getCsvService() {
        return csvService;
    }

    @Transactional
    public int saveCoSeller(RegionRequestDto requestDto, String username) {
        List<BizCsvInfoDto> csvList = csvProcessor.readCsvByRegion(requestDto);
        if (csvList.isEmpty()) {
            return 0;
        }

        return processBatch(csvList, username, null, null);
    }

    @Transactional
    public int saveCoSeller(String city, String district, String username) {
        List<BizCsvInfoDto> csvList = csvProcessor.readCsvByCityAndDistrict(city, district);
        if (csvList.isEmpty()) {
            return 0;
        }

        return processBatch(csvList, username, city, district);
    }

    @Transactional
    public void processBatch(List<BizCsvInfoDto> csvList, String username) {
        processBatch(csvList, username, null, null);
    }

    @Transactional
    public int clearAllData() {
        return storageService.clearAllData();
    }

    @Transactional
    public CompletableFuture<Optional<CorpMastCreateDTO>> processAsync(BizCsvInfoDto csvInfo, String username) {
        return apiService.processAsync(csvInfo, username);
    }

    private int processBatch(List<BizCsvInfoDto> csvList, String username, String city, String district) {
        int batchSize = 100;
        int totalSaved = 0;

        for (int i = 0; i < csvList.size(); i += batchSize) {
            int end = Math.min(i + batchSize, csvList.size());
            List<BizCsvInfoDto> batch = csvList.subList(i, end);

            List<CorpMastCreateDTO> corpCreateDtoList;
            if (city != null && district != null) {
                corpCreateDtoList = dataMapper.mapToCorpMastCreateDTO(batch, username, city, district);
            } else {
                corpCreateDtoList = dataMapper.mapToCorpMastCreateDTO(batch, username);
            }

            if (!corpCreateDtoList.isEmpty()) {
                int savedCnt = storageService.saveCorpMastList(corpCreateDtoList, username);
                totalSaved += savedCnt;
            }
        }

        return totalSaved;
    }
}
