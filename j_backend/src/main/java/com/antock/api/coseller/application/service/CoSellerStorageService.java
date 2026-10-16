package com.antock.api.coseller.application.service;

import com.antock.api.coseller.application.dto.CorpMastCreateDTO;
import com.antock.api.coseller.domain.CorpMast;
import com.antock.api.coseller.domain.CorpMastHistory;
import com.antock.api.coseller.infrastructure.CorpMastStore;
import com.antock.api.coseller.infrastructure.CorpMastHistoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@Slf4j
@RequiredArgsConstructor
public class CoSellerStorageService {

    private final CorpMastStore corpMastStore;
    private final CorpMastHistoryStore corpMastHistoryStore;
    private final Executor asyncExecutor;

    @Transactional
    public int saveCorpMastList(List<CorpMastCreateDTO> corpCreateDtoList, String username) {
        if (corpCreateDtoList == null || corpCreateDtoList.isEmpty()) {
            return 0;
        }

        Map<String, CorpMast> byBizNo = new LinkedHashMap<>();
        for (CorpMastCreateDTO dto : corpCreateDtoList) {
            CorpMast entity = dto.toEntity();
            if (entity.getUsername() == null || entity.getUsername().isBlank()) {
                entity.setUsername(username == null ? "" : username);
            }
            byBizNo.put(entity.getBizNo(), entity);
        }
        List<CorpMast> rows = new ArrayList<>(byBizNo.values());
        Set<String> existingBizNos = new HashSet<>(corpMastStore.findExistingBizNos(
                rows.stream().map(CorpMast::getBizNo).toList()));
        int written = corpMastStore.upsertAll(rows);

        for (CorpMast entity : rows) {
            String user = entity.getUsername() == null || entity.getUsername().isBlank()
                    ? username
                    : entity.getUsername();
            if (existingBizNos.contains(entity.getBizNo())) {
                recordHistory(user, entity.getBizNo(), "UPDATE", "SUCCESS", "기존 사업자번호 갱신");
            } else {
                recordHistory(user, entity.getBizNo(), "INSERT", "SUCCESS", "정상 저장");
            }
        }
        return written;
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

