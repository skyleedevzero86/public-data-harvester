package com.antock.api.coseller.application.service;

import com.antock.api.coseller.application.dto.CorpMastCreateDTO;
import com.antock.api.coseller.application.dto.api.BizCsvInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoSellerChunkWriter {

    private final CoSellerApiService apiService;
    private final CoSellerDataMapper dataMapper;
    private final CoSellerStorageService storageService;

    @Value("${batch.api-timeout-seconds:30}")
    private long apiTimeoutSeconds = 30;

    public List<CorpMastCreateDTO> prepare(List<BizCsvInfoDto> chunk, String username, String city, String district) {
        List<CompletableFuture<Optional<CorpMastCreateDTO>>> futures = new ArrayList<>(chunk.size());
        for (BizCsvInfoDto row : chunk) {
            futures.add(apiService.processAsync(row, username));
        }
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .orTimeout(apiTimeoutSeconds, TimeUnit.SECONDS)
                .exceptionally(ex -> null)
                .join();

        List<CorpMastCreateDTO> prepared = new ArrayList<>();
        for (int i = 0; i < chunk.size(); i++) {
            appendRow(prepared, chunk.get(i), futures.get(i), username, city, district);
        }
        return prepared;
    }

    private void appendRow(List<CorpMastCreateDTO> prepared, BizCsvInfoDto row,
            CompletableFuture<Optional<CorpMastCreateDTO>> future, String username, String city, String district) {
        Optional<CorpMastCreateDTO> fromApi = readApiResult(row, future);
        if (fromApi.isPresent()) {
            prepared.add(applyRequestedRegion(fromApi.get(), city, district));
            return;
        }
        if (city != null && district != null) {
            prepared.addAll(dataMapper.mapToCorpMastCreateDTO(List.of(row), username, city, district));
        } else {
            prepared.addAll(dataMapper.mapToCorpMastCreateDTO(List.of(row), username));
        }
    }

    private Optional<CorpMastCreateDTO> readApiResult(BizCsvInfoDto row,
            CompletableFuture<Optional<CorpMastCreateDTO>> future) {
        if (!future.isDone()) {
            future.cancel(true);
            log.warn("API 보강 시간 초과, 기본 매핑으로 저장: bizNo={}", row.getBizNo());
            return Optional.empty();
        }
        try {
            Optional<CorpMastCreateDTO> fromApi = future.join();
            return fromApi == null ? Optional.empty() : fromApi;
        } catch (CompletionException | CancellationException e) {
            log.warn("API 보강 실패, 기본 매핑으로 저장: bizNo={}", row.getBizNo());
            return Optional.empty();
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int saveChunk(List<CorpMastCreateDTO> rows, String username) {
        if (rows == null || rows.isEmpty()) {
            return 0;
        }
        return storageService.saveCorpMastList(rows, username);
    }

    private CorpMastCreateDTO applyRequestedRegion(CorpMastCreateDTO source, String city, String district) {
        if (city == null || district == null) {
            return source;
        }
        return CorpMastCreateDTO.builder()
                .sellerId(source.getSellerId())
                .bizNm(source.getBizNm())
                .bizNo(source.getBizNo())
                .corpRegNo(source.getCorpRegNo())
                .regionCd(source.getRegionCd())
                .siNm(city)
                .sggNm(district)
                .username(source.getUsername())
                .repNm(source.getRepNm())
                .estbDt(source.getEstbDt())
                .roadNmAddr(source.getRoadNmAddr())
                .jibunAddr(source.getJibunAddr())
                .corpStatus(source.getCorpStatus())
                .build();
    }
}
