package com.antock.api.coseller.application.service;

import com.antock.api.coseller.application.dto.CorpMastCreateDTO;
import com.antock.api.coseller.application.dto.api.BizCsvInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CoSellerChunkWriter {

    private final CoSellerApiService apiService;
    private final CoSellerDataMapper dataMapper;
    private final CoSellerStorageService storageService;

    public List<CorpMastCreateDTO> prepare(List<BizCsvInfoDto> chunk, String username, String city, String district) {
        List<CorpMastCreateDTO> prepared = new ArrayList<>();
        for (BizCsvInfoDto row : chunk) {
            Optional<CorpMastCreateDTO> fromApi = apiService.processAsync(row, username).join();
            if (fromApi.isPresent()) {
                prepared.add(applyRequestedRegion(fromApi.get(), city, district));
                continue;
            }
            if (city != null && district != null) {
                prepared.addAll(dataMapper.mapToCorpMastCreateDTO(List.of(row), username, city, district));
            } else {
                prepared.addAll(dataMapper.mapToCorpMastCreateDTO(List.of(row), username));
            }
        }
        return prepared;
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
