package com.antock.global.utils;

import com.antock.api.corpmanual.application.dto.response.CorpMastManualResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.OutputStream;
import java.util.List;

public class ExcelExportUtil {

    public static void writeCorpListToExcel(List<CorpMastManualResponse> corpList, OutputStream os) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("법인목록");
            int rowIdx = 0;

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.BLACK.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            Row header = sheet.createRow(rowIdx++);
            String[] headers = {"ID", "법인명", "사업자번호", "법인등록번호", "시/도", "구/군", "판매자ID", "등록자", "설명"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            header.setHeightInPoints(25);

            for (CorpMastManualResponse corp : corpList) {
                Row row = sheet.createRow(rowIdx++);

                Cell cell0 = row.createCell(0);
                cell0.setCellValue(corp.getId());
                cell0.setCellStyle(cellStyle);

                Cell cell1 = row.createCell(1);
                cell1.setCellValue(corp.getBizNm());
                cell1.setCellStyle(cellStyle);

                Cell cell2 = row.createCell(2);
                cell2.setCellValue(corp.getBizNo());
                cell2.setCellStyle(cellStyle);

                Cell cell3 = row.createCell(3);
                cell3.setCellValue(corp.getCorpRegNo());
                cell3.setCellStyle(cellStyle);

                Cell cell4 = row.createCell(4);
                cell4.setCellValue(corp.getSiNm());
                cell4.setCellStyle(cellStyle);

                Cell cell5 = row.createCell(5);
                cell5.setCellValue(corp.getSggNm());
                cell5.setCellStyle(cellStyle);

                Cell cell6 = row.createCell(6);
                cell6.setCellValue(corp.getSellerId());
                cell6.setCellStyle(cellStyle);

                Cell cell7 = row.createCell(7);
                cell7.setCellValue(corp.getUsername());
                cell7.setCellStyle(cellStyle);

                Cell cell8 = row.createCell(8);
                String desc = corp.getDescription();
                if (desc == null || desc.trim().isEmpty()) {
                    desc = "자동수집";
                }
                cell8.setCellValue(desc);
                cell8.setCellStyle(cellStyle);

                row.setHeightInPoints(20);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                int currentWidth = sheet.getColumnWidth(i);
                if (currentWidth < 3000) {
                    sheet.setColumnWidth(i, 3000);
                }
            }

            workbook.write(os);
        }
    }
}