package com.rizwan.money_tracker.service;

import com.rizwan.money_tracker.dto.ExcelExportDto;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelService {

    public byte[] exportToExcel(List<? extends ExcelExportDto> rows) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Transaction Details");
            String[] headers = {"ID", "Name", "Category", "Amount", "Date", "Created At", "Modified At"};

            Row headerRow = sheet.createRow(0);
            for (int index = 0; index < headers.length; index++) {
                headerRow.createCell(index).setCellValue(headers[index]);
            }

            int rowIndex = 1;
            for (ExcelExportDto rowData : rows) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(rowData.getId() != null ? rowData.getId() : 0L);
                row.createCell(1).setCellValue(defaultValue(rowData.getName()));
                row.createCell(2).setCellValue(defaultValue(rowData.getCategoryName()));
                row.createCell(3).setCellValue(rowData.getAmount() != null ? rowData.getAmount().toPlainString() : "");
                row.createCell(4).setCellValue(rowData.getDate() != null ? rowData.getDate().toString() : "");
                row.createCell(5).setCellValue(rowData.getCreatedAt() != null ? rowData.getCreatedAt().toString() : "");
                row.createCell(6).setCellValue(rowData.getModifiedAt() != null ? rowData.getModifiedAt().toString() : "");
            }

            for (int columnIndex = 0; columnIndex < headers.length; columnIndex++) {
                sheet.autoSizeColumn(columnIndex);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new RuntimeException("Failed to generate excel file", exception);
        }
    }

    private String defaultValue(String value) {
        return value != null ? value : "";
    }
}
