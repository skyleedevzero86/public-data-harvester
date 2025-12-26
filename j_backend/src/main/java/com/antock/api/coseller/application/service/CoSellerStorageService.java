package com.antock.api.coseller.application.service;

import com.antock.api.coseller.application.dto.CorpMastCreateDTO;
import com.antock.api.coseller.domain.CorpMast;
import com.antock.api.coseller.domain.CorpMastHistory;
import com.antock.api.coseller.infrastructure.CorpMastStore;
import com.antock.api.coseller.infrastructure.CorpMastHistoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CoSellerStorageService {

    private final CorpMastStore corpMastStore;
    private final CorpMastHistoryStore corpMastHistoryStore;
    private final Executor asyncExecutor;

    @Transactional
    public int saveCorpMastList(List<CorpMastCreateDTO> corpCreateDtoList, String username) {
        int savedCount = 0;
        List<String> bizNos = corpCreateDtoList.stream()
                .map(CorpMastCreateDTO::getBizNo)
                .collect(Collectors.toList());
        List<String> existingBizNos = corpMastStore.findExistingBizNos(bizNos);

        for (CorpMastCreateDTO dto : corpCreateDtoList) {
            CorpMast entity = dto.toEntity();
            String user = dto.getUsername() != null ? dto.getUsername() : username;

            try {
                if (existingBizNos.contains(entity.getBizNo())) {
                    recordHistory(user, entity.getBizNo(), "INSERT", "DUPLICATE", "중복 bizNo, 저장 스킵");
                } else {
                    corpMastStore.save(entity);
                    savedCount++;
                    recordHistory(user, entity.getBizNo(), "INSERT", "SUCCESS", "정상 저장");
                }
            } catch (DataIntegrityViolationException ex) {
                recordHistory(user, entity.getBizNo(), "INSERT", "FAIL", "DataIntegrityViolationException: " + ex.getMessage());
            } catch (Exception ex) {
                recordHistory(user, entity.getBizNo(), "INSERT", "FAIL", "Exception: " + ex.getMessage());
            }
        }

        return savedCount;
    }

    @Transactional
    public int clearAllData() {
        List<CorpMast> allCorps = corpMastStore.findAll();
        int totalCount = allCorps.size();

        for (CorpMast corp : allCorps) {
            corpMastStore.delete(corp);
        }
        return totalCount;
    }

    private void recordHistory(String username, String bizNo, String action, String result, String message) {
        CompletableFuture.runAsync(() -> {
            try {
                CorpMastHistory history = CorpMastHistory.builder()
                        .username(username)
                        .action(action)
                        .bizNo(bizNo)
                        .result(result)
                        .message(message)
                        .timestamp(java.time.LocalDateTime.now())
                        .build();
                corpMastHistoryStore.save(history);
            } catch (Exception e) {
                log.warn("히스토리 저장 실패: bizNo={}", bizNo, e);
            }
        }, asyncExecutor);
    }
}

