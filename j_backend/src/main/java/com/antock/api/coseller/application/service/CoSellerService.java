package com.antock.api.coseller.application.service;

import com.antock.api.coseller.application.dto.CorpMastCreateDTO;
import com.antock.api.coseller.application.dto.RegionRequestDto;
import com.antock.api.coseller.application.dto.api.BizCsvInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class CoSellerService {

    static final int CHUNK_SIZE = 100;

    private final CoSellerStorageService storageService;
    private final CoSellerApiService apiService;
    private final CsvService csvService;
    private final CoSellerChunkWriter chunkWriter;
    private final FailedChunkRecorder failedChunkRecorder;
    private final ManualRegistrationHistory manualRegistrationHistory;

    public CsvService getCsvService() {
        return csvService;
    }

    public int saveCoSeller(RegionRequestDto requestDto, String username) {
        return saveRegion(requestDto.getCity().getValue(), requestDto.getDistrict().getValue(), username, false);
    }

    public int saveCoSeller(String city, String district, String username) {
        return saveRegion(city, district, username, true);
    }

    public List<ManualRegistrationHistory.Row> recentManualHistories() {
        return manualRegistrationHistory.recent();
    }

    private int saveRegion(String city, String district, String username, boolean keepRegionOnRows) {
        String fileName = city + "_" + district + ".csv";
        String rowCity = keepRegionOnRows ? city : null;
        String rowDistrict = keepRegionOnRows ? district : null;
        try {
            int saved = processFile(fileName, username, rowCity, rowDistrict);
            remember(username, city, district, fileName, saved, null);
            return saved;
        } catch (RuntimeException exception) {
            remember(username, city, district, fileName, 0, exception);
            throw exception;
        }
    }

    private void remember(String username, String city, String district, String fileName, int savedCount, Exception error) {
        try {
            manualRegistrationHistory.record(username, city, district, fileName, savedCount, error);
        } catch (RuntimeException exception) {
            log.error("수동 등록 이력 저장 실패: {} {}", city, district, exception);
        }
    }

    public void processBatch(List<BizCsvInfoDto> csvList, String username) {
        processList(csvList, "inline-batch", username, null, null);
    }

    @Transactional
    public int clearAllData() {
        return storageService.clearAllData();
    }

    @Transactional
    public CompletableFuture<Optional<CorpMastCreateDTO>> processAsync(BizCsvInfoDto csvInfo, String username) {
        return apiService.processAsync(csvInfo, username);
    }

    private int processFile(String fileName, String username, String city, String district) {
        AtomicInteger totalSaved = new AtomicInteger();
        AtomicInteger offset = new AtomicInteger();
        csvService.readInChunks(fileName, CHUNK_SIZE, chunk -> {
            int startIndex = offset.getAndAdd(chunk.size());
            totalSaved.addAndGet(handleChunk(fileName, startIndex, chunk, username, city, district));
        });
        return totalSaved.get();
    }

    private int processList(List<BizCsvInfoDto> csvList, String fileName, String username, String city, String district) {
        if (csvList == null || csvList.isEmpty()) {
            return 0;
        }
        int totalSaved = 0;
        for (int i = 0; i < csvList.size(); i += CHUNK_SIZE) {
            int end = Math.min(i + CHUNK_SIZE, csvList.size());
            List<BizCsvInfoDto> chunk = csvList.subList(i, end);
            totalSaved += handleChunk(fileName, i, chunk, username, city, district);
        }
        return totalSaved;
    }

    private int handleChunk(String fileName, int startIndex, List<BizCsvInfoDto> chunk, String username, String city,
            String district) {
        try {
            List<CorpMastCreateDTO> prepared = chunkWriter.prepare(chunk, username, city, district);
            return chunkWriter.saveChunk(prepared, username);
        } catch (Exception e) {
            recordFailure(fileName, startIndex, chunk, e);
            return 0;
        }
    }

    private void recordFailure(String fileName, int startIndex, List<BizCsvInfoDto> chunk, Exception error) {
        try {
            failedChunkRecorder.record(fileName, startIndex, chunk, error);
        } catch (Exception recordError) {
            log.error("실패 청크 기록 실패: file={}, startIndex={}", fileName, startIndex, recordError);
        }
    }
}
