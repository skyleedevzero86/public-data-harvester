package com.antock.global.utils;

import com.antock.api.corpsearch.application.dto.response.CorpMastSearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
public class ExcelUtils {

    private static final String SHEET_NAME = "법인 정보 검색 결과";
    private static final String[] HEADERS = {
            "번호", "법인명", "사업자번호", "판매자ID", "법인등록번호",
            "시/도", "구/군", "등록자"
    };

    public byte[] createCorpSearchExcel(List<CorpMastSearchResponse> corpList, long totalElements) throws IOException {

        log.debug("Excel 파일 생성 시작: {} 건의 데이터", corpList.size());

        Workbook workbook = null;
        ByteArrayOutputStream outputStream = null;

        try {
            workbook = new HSSFWorkbook();
            outputStream = new ByteArrayOutputStream();

            Sheet sheet = workbook.createSheet(SHEET_NAME);

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);

            int rowNum = 0;

            rowNum = createTitleRow(sheet, workbook, rowNum, totalElements);
            rowNum++;

            rowNum = createHeaderRow(sheet, headerStyle, rowNum);

            createDataRows(sheet, corpList, dataStyle, numberStyle, rowNum, totalElements);

            autoSizeColumns(sheet);

            workbook.write(outputStream);

            byte[] result = outputStream.toByteArray();
            log.debug("Excel 파일 생성 완료: {} bytes", result.length);
            return result;

        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    log.warn("OutputStream 닫기 실패", e);
                }
            }
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    log.warn("Workbook 닫기 실패", e);
                }
            }
        }
    }

    private int createTitleRow(Sheet sheet, Workbook workbook, int rowNum, long totalElements) {
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);

        String title = String.format("법인 정보 검색 결과 (총 %,d건)", totalElements);
        titleCell.setCellValue(title);

        CellStyle titleStyle = createTitleStyle(workbook);
        titleCell.setCellStyle(titleStyle);

        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, HEADERS.length - 1));

        Row dateRow = sheet.createRow(rowNum++);
        Cell dateCell = dateRow.createCell(0);
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        dateCell.setCellValue("생성일시: " + currentTime);

        CellStyle dateStyle = createDateStyle(workbook);
        dateCell.setCellStyle(dateStyle);

        return rowNum;
    }

    private int createHeaderRow(Sheet sheet, CellStyle headerStyle, int rowNum) {
        Row headerRow = sheet.createRow(rowNum++);

        for (int i = 0; i < HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(HEADERS[i]);
            cell.setCellStyle(headerStyle);
        }

        return rowNum;
    }

    private void createDataRows(Sheet sheet, List<CorpMastSearchResponse> corpList,
                                CellStyle dataStyle, CellStyle numberStyle, int startRowNum, long totalElements) {

        int rowNum = startRowNum;

        for (int i = 0; i < corpList.size(); i++) {
            CorpMastSearchResponse corp = corpList.get(i);
            Row row = sheet.createRow(rowNum++);

            int cellNum = 0;

            Cell numberCell = row.createCell(cellNum++);
            numberCell.setCellValue(totalElements - i);
            numberCell.setCellStyle(numberStyle);

            Cell bizNmCell = row.createCell(cellNum++);
            bizNmCell.setCellValue(corp.getBizNm());
            bizNmCell.setCellStyle(dataStyle);

            Cell bizNoCell = row.createCell(cellNum++);
            bizNoCell.setCellValue(corp.getFormattedBizNo());
            bizNoCell.setCellStyle(dataStyle);

            Cell sellerIdCell = row.createCell(cellNum++);
            sellerIdCell.setCellValue(corp.getSellerId());
            sellerIdCell.setCellStyle(dataStyle);

            Cell corpRegNoCell = row.createCell(cellNum++);
            corpRegNoCell.setCellValue(corp.getCorpRegNo());
            corpRegNoCell.setCellStyle(dataStyle);

            Cell siNmCell = row.createCell(cellNum++);
            siNmCell.setCellValue(corp.getSiNm());
            siNmCell.setCellStyle(dataStyle);

            Cell sggNmCell = row.createCell(cellNum++);
            sggNmCell.setCellValue(corp.getSggNm());
            sggNmCell.setCellStyle(dataStyle);

            Cell usernameCell = row.createCell(cellNum++);
            usernameCell.setCellValue(corp.getUsername());
            usernameCell.setCellStyle(dataStyle);
        }
    }

    private void autoSizeColumns(Sheet sheet) {
        for (int i = 0; i < HEADERS.length; i++) {
            sheet.autoSizeColumn(i);
            int currentWidth = sheet.getColumnWidth(i);
            if (currentWidth < 3000) {
                sheet.setColumnWidth(i, 3000);
            }
            if (currentWidth > 8000) {
                sheet.setColumnWidth(i, 8000);
            }
        }
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);

        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    private CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));

        return style;
    }

    public String generateFileName() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("법인정보검색결과_%s.xls", timestamp);
    }
}