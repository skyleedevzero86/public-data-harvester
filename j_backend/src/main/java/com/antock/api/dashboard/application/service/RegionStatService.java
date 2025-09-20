package com.antock.api.dashboard.application.service;

import com.antock.api.coseller.domain.CorpMast;
import com.antock.api.coseller.infrastructure.CorpMastRepository;
import com.antock.api.dashboard.application.dto.RegionStatDto;
import com.antock.global.utils.ExcelExportUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegionStatService {
    private final CorpMastRepository corpMastRepository;

    public RegionStatDto getTopRegionStat() {
        List<RegionStatDto> allStats = getAllRegionStats();
        return allStats.isEmpty() ? null : allStats.get(0);
    }

    public List<RegionStatDto> getAllRegionStats() {
        List<Object[]> rawStats = corpMastRepository.getRegionStats();
        log.info("Raw stats count: {}", rawStats.size());

        return rawStats.stream()
                .map(this::convertToRegionStatDto)
                .collect(Collectors.toList());
    }

    public Page<RegionStatDto> getRegionStatsWithPaging(Pageable pageable, String city, String district) {
        try {
            log.info("Getting region stats with paging - page: {}, size: {}, city: {}, district: {}",
                    pageable.getPageNumber(), pageable.getPageSize(), city, district);

            Page<Object[]> rawStatsPage = corpMastRepository.getRegionStatsWithPaging(pageable, city, district);

            log.info("Raw stats page - total: {}, current page: {}, content size: {}",
                    rawStatsPage.getTotalElements(), rawStatsPage.getNumber(), rawStatsPage.getContent().size());

            return rawStatsPage.map(this::convertToRegionStatDto);
        } catch (Exception e) {
            log.error("Error getting region stats with paging", e);
            throw new RuntimeException("Failed to get region stats", e);
        }
    }

    public List<String> getCities() {
        return corpMastRepository.findDistinctCities();
    }

    public List<String> getDistrictsByCity(String city) {
        return corpMastRepository.findDistinctDistrictsByCity(city);
    }

    public byte[] exportToExcel(String city, String district) throws Exception {
        log.info("Exporting region stats to Excel - city: {}, district: {}", city, district);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = createCombinedSheet(workbook, city, district);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Error creating Excel file", e);
            throw new Exception("Excel 파일 생성 중 오류가 발생했습니다.", e);
        }
    }

    private Sheet createCombinedSheet(XSSFWorkbook workbook, String city, String district) {
        Sheet sheet = workbook.createSheet("지역별 통계");

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle summaryStyle = createSummaryStyle(workbook);

        int rowIdx = 0;

        Row titleRow = sheet.createRow(rowIdx++);
        Cell titleCell = titleRow.createCell(0);
        String title = "지역별 통계";
        if (city != null && !city.isEmpty()) {
            title += " - " + city;
            if (district != null && !district.isEmpty()) {
                title += " " + district;
            }
        }
        titleCell.setCellValue(title);
        titleCell.setCellStyle(titleStyle);

        Row dateRow = sheet.createRow(rowIdx++);
        Cell dateCell = dateRow.createCell(0);
        dateCell.setCellValue(
                "생성일시: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        dateCell.setCellStyle(dataStyle);

        rowIdx += 2;

        Row summaryTitleRow = sheet.createRow(rowIdx++);
        Cell summaryTitleCell = summaryTitleRow.createCell(0);
        summaryTitleCell.setCellValue("지역별 통계 요약");
        summaryTitleCell.setCellStyle(summaryStyle);

        Row summaryHeaderRow = sheet.createRow(rowIdx++);
        String[] summaryHeaders = { "시/도", "구/군", "총 법인 수", "유효한 법인등록번호", "유효한 지역코드", "완성도(%)" };

        for (int i = 0; i < summaryHeaders.length; i++) {
            Cell cell = summaryHeaderRow.createCell(i);
            cell.setCellValue(summaryHeaders[i]);
            cell.setCellStyle(headerStyle);
        }

        List<Object[]> rawStats = corpMastRepository.getRegionStatsWithPaging(
                Pageable.unpaged(), city, district).getContent();

        for (Object[] rawData : rawStats) {
            Row dataRow = sheet.createRow(rowIdx++);

            dataRow.createCell(0).setCellValue((String) rawData[0]);
            dataRow.createCell(1).setCellValue((String) rawData[1]);
            dataRow.createCell(2).setCellValue(((Number) rawData[2]).longValue());
            dataRow.createCell(3).setCellValue(((Number) rawData[3]).longValue());
            dataRow.createCell(4).setCellValue(((Number) rawData[4]).longValue());

            long totalCount = ((Number) rawData[2]).longValue();
            long validCorpRegNoCount = ((Number) rawData[3]).longValue();
            long validRegionCdCount = ((Number) rawData[4]).longValue();
            double completionRate = totalCount > 0
                    ? ((double) (validCorpRegNoCount + validRegionCdCount) / (totalCount * 2)) * 100
                    : 0;
            dataRow.createCell(5).setCellValue(Math.round(completionRate * 100.0) / 100.0);

            for (int i = 0; i < 6; i++) {
                dataRow.getCell(i).setCellStyle(dataStyle);
            }
        }

        rowIdx += 2;

        Row detailTitleRow = sheet.createRow(rowIdx++);
        Cell detailTitleCell = detailTitleRow.createCell(0);
        detailTitleCell.setCellValue("상세 법인 목록");
        detailTitleCell.setCellStyle(summaryStyle);

        Row detailHeaderRow = sheet.createRow(rowIdx++);
        String[] detailHeaders = { "ID", "법인명", "사업자번호", "법인등록번호", "지역코드", "시/도", "구/군", "판매자ID", "등록자", "설명", "등록일시",
                "수정일시" };

        for (int i = 0; i < detailHeaders.length; i++) {
            Cell cell = detailHeaderRow.createCell(i);
            cell.setCellValue(detailHeaders[i]);
            cell.setCellStyle(headerStyle);
        }

        List<CorpMast> corpList;
        if (city != null && !city.isEmpty() && district != null && !district.isEmpty()) {
            corpList = corpMastRepository.findBySiNmAndSggNm(city, district);
        } else if (city != null && !city.isEmpty()) {
            corpList = corpMastRepository.findBySiNm(city);
        } else {
            corpList = corpMastRepository.findAll();
        }

        for (CorpMast corp : corpList) {
            Row dataRow = sheet.createRow(rowIdx++);

            dataRow.createCell(0).setCellValue(corp.getId());
            dataRow.createCell(1).setCellValue(corp.getBizNm());
            dataRow.createCell(2).setCellValue(corp.getBizNo());
            dataRow.createCell(3).setCellValue(corp.getCorpRegNo());
            dataRow.createCell(4).setCellValue(corp.getRegionCd());
            dataRow.createCell(5).setCellValue(corp.getSiNm());
            dataRow.createCell(6).setCellValue(corp.getSggNm());
            dataRow.createCell(7).setCellValue(corp.getSellerId());
            dataRow.createCell(8).setCellValue(corp.getUsername());
            dataRow.createCell(9).setCellValue(corp.getDescription() != null ? corp.getDescription() : "");
            dataRow.createCell(10)
                    .setCellValue(corp.getCreateDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            dataRow.createCell(11)
                    .setCellValue(corp.getModifyDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            for (int i = 0; i < 12; i++) {
                dataRow.getCell(i).setCellStyle(dataStyle);
            }
        }

        for (int i = 0; i < 12; i++) {
            sheet.autoSizeColumn(i);
        }

        return sheet;
    }

    private CellStyle createHeaderStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);

        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }

    private CellStyle createDataStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();

        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }

    private CellStyle createTitleStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);

        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }

    private CellStyle createSummaryStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);

        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }

    private RegionStatDto convertToRegionStatDto(Object[] rawData) {
        try {
            String city = (String) rawData[0];
            String district = (String) rawData[1];
            Long totalCount = ((Number) rawData[2]).longValue();
            Long validCorpRegNoCount = ((Number) rawData[3]).longValue();
            Long validRegionCdCount = ((Number) rawData[4]).longValue();

            RegionStatDto dto = new RegionStatDto(city, district, totalCount, validCorpRegNoCount, validRegionCdCount);
            log.debug("Converted RegionStatDto: {}", dto);
            return dto;
        } catch (Exception e) {
            log.error("Error converting raw data to RegionStatDto: {}", java.util.Arrays.toString(rawData), e);
            throw new RuntimeException("Failed to convert region stat data", e);
        }
    }
}