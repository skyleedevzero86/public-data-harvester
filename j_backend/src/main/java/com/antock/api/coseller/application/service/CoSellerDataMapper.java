package com.antock.api.coseller.application.service;

import com.antock.api.coseller.application.dto.CorpMastCreateDTO;
import com.antock.api.coseller.application.dto.api.BizCsvInfoDto;
import com.antock.api.coseller.application.dto.api.RegionInfoDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class CoSellerDataMapper {

    public List<CorpMastCreateDTO> mapToCorpMastCreateDTO(List<BizCsvInfoDto> csvList, String username) {
        return mapToCorpMastCreateDTO(csvList, username, Optional.empty(), Optional.empty());
    }

    public List<CorpMastCreateDTO> mapToCorpMastCreateDTO(List<BizCsvInfoDto> csvList, String username, String city,
            String district) {
        return mapToCorpMastCreateDTO(csvList, username, Optional.of(city), Optional.of(district));
    }

    private List<CorpMastCreateDTO> mapToCorpMastCreateDTO(List<BizCsvInfoDto> csvList, String username,
            Optional<String> city, Optional<String> district) {
        List<CorpMastCreateDTO> result = new ArrayList<>();
        for (BizCsvInfoDto csvInfo : csvList) {
            try {
                CorpMastCreateDTO dto = generateMockCorpData(csvInfo, username, city, district);
                result.add(dto);
            } catch (Exception e) {
                log.warn("CSV 데이터 변환 실패: bizNo={}", csvInfo.getBizNo(), e);
            }
        }
        return result;
    }

    public Optional<CorpMastCreateDTO> mapFromApiData(BizCsvInfoDto csvInfo, String corpRegNo, RegionInfoDto regionInfo,
            String username) {
        if (isValidCorpRegNo(corpRegNo) && isValidRegionInfo(regionInfo)) {
            return Optional.of(CorpMastCreateDTO.builder()
                    .sellerId(csvInfo.getSellerId())
                    .bizNm(csvInfo.getBizNm())
                    .bizNo(csvInfo.getBizNo())
                    .corpRegNo(corpRegNo)
                    .regionCd(regionInfo.getRegionCd())
                    .siNm(regionInfo.getSiNm())
                    .sggNm(regionInfo.getSggNm())
                    .username(username)
                    .repNm(csvInfo.getOwnerName())
                    .estbDt(csvInfo.getDate())
                    .roadNmAddr(csvInfo.getBizAddress())
                    .jibunAddr(csvInfo.getBizNesAddress())
                    .corpStatus("계속(수익)")
                    .build());
        }
        return Optional.empty();
    }

    private CorpMastCreateDTO generateMockCorpData(BizCsvInfoDto csvInfo, String username,
            Optional<String> city, Optional<String> district) {
        String corpRegNo = generateMockCorpRegNo();
        String regionCd = generateMockRegionCd();

        String siNm;
        String sggNm;

        if (city.isPresent() && district.isPresent()) {
            siNm = city.get();
            sggNm = district.get();
        } else {
            String[] addressParts = csvInfo.getBizAddress().split(" ");
            siNm = addressParts.length > 0 ? addressParts[0] : "서울특별시";
            sggNm = addressParts.length > 1 ? addressParts[1] : "강남구";
        }

        return CorpMastCreateDTO.builder()
                .sellerId(csvInfo.getSellerId())
                .bizNm(csvInfo.getBizNm())
                .bizNo(csvInfo.getBizNo())
                .corpRegNo(corpRegNo)
                .regionCd(regionCd)
                .siNm(siNm)
                .sggNm(sggNm)
                .username(username)
                .repNm(generateMockRepName())
                .estbDt(generateMockEstbDate())
                .roadNmAddr(csvInfo.getBizAddress())
                .jibunAddr(csvInfo.getBizNesAddress())
                .corpStatus("정상영업")
                .build();
    }

    private String generateMockCorpRegNo() {
        Random random = new Random();
        int year = 2020 + random.nextInt(5);
        int month = 1 + random.nextInt(12);
        int day = 1 + random.nextInt(28);
        int sequence = 1000000 + random.nextInt(9000000);

        return String.format("%04d%02d%02d-%07d", year, month, day, sequence);
    }

    private String generateMockRegionCd() {
        Random random = new Random();
        return String.format("%05d", 10000 + random.nextInt(90000));
    }

    private String generateMockRepName() {
        String[] surnames = { "김", "이", "박", "최", "정", "강", "조", "윤", "장", "임" };
        String[] givenNames = { "철수", "영희", "민수", "수진", "동현", "지영", "현우", "서연", "준호", "미영" };

        Random random = new Random();
        String surname = surnames[random.nextInt(surnames.length)];
        String givenName = givenNames[random.nextInt(givenNames.length)];

        return surname + givenName;
    }

    private String generateMockEstbDate() {
        Random random = new Random();
        int year = 1990 + random.nextInt(35);
        int month = 1 + random.nextInt(12);
        int day = 1 + random.nextInt(28);

        return String.format("%04d%02d%02d", year, month, day);
    }

    private boolean isValidCorpRegNo(String corpRegNo) {
        return corpRegNo != null &&
                !corpRegNo.trim().isEmpty() &&
                !corpRegNo.startsWith("0") &&
                !corpRegNo.contains("N/A");
    }

    private boolean isValidRegionInfo(RegionInfoDto regionInfo) {
        return regionInfo != null &&
                regionInfo.getRegionCd() != null &&
                !regionInfo.getRegionCd().trim().isEmpty() &&
                !regionInfo.getRegionCd().startsWith("0");
    }
}
