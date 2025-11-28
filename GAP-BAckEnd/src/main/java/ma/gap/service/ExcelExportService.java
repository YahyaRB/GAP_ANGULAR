package ma.gap.service;

import ma.gap.entity.CalculPerProjet;
import ma.gap.entity.EmployeeCalcul;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelExportService {

    public ByteArrayInputStream exportCalculsToExcel(List<CalculPerProjet> calculs) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Calculs");

            // Header
            Row headerRow = sheet.createRow(0);
            String[] columns = { "Projet", "Atelier", "Nombre d'Heures", "Pourcentage" };
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            // Data
            int rowIdx = 1;
            for (CalculPerProjet calcul : calculs) {
                Row row = sheet.createRow(rowIdx++);

                String projetDesignation = "";
                if (calcul.getProjet() != null) {
                    projetDesignation = (calcul.getProjet().getCode() != null ? calcul.getProjet().getCode() + " - "
                            : "") + calcul.getProjet().getDesignation();
                }
                row.createCell(0).setCellValue(projetDesignation);

                row.createCell(1).setCellValue(calcul.getAtelier() != null ? calcul.getAtelier().getDesignation() : "");
                row.createCell(2).setCellValue(calcul.getHeureTrav() != null ? calcul.getHeureTrav() : 0);
                row.createCell(3).setCellValue(calcul.getPourcHeur());
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

    public ByteArrayInputStream exportEmployeeDetailsToExcel(CalculPerProjet calcul) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Détails Employés");

            // Project Info Row
            Row projectRow = sheet.createRow(0);
            String projetDesignation = "";
            if (calcul.getProjet() != null) {
                projetDesignation = (calcul.getProjet().getCode() != null ? calcul.getProjet().getCode() + " - " : "")
                        + calcul.getProjet().getDesignation();
            }
            projectRow.createCell(0).setCellValue("Projet: " + projetDesignation);

            Row hoursRow = sheet.createRow(1);
            hoursRow.createCell(0).setCellValue(
                    "Total Heures Projet: " + (calcul.getHeureTrav() != null ? calcul.getHeureTrav() : 0));

            // Header
            Row headerRow = sheet.createRow(3);
            String[] columns = { "Matricule", "Nom et Prénom", "Nombre d'H.T", "% H.T" };
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            // Data
            int rowIdx = 4;
            if (calcul.getEmployesCalculs() != null) {
                for (EmployeeCalcul emp : calcul.getEmployesCalculs()) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(emp.getMatricule());
                    row.createCell(1).setCellValue(emp.getNom() + " " + emp.getPrenom());
                    row.createCell(2).setCellValue(emp.getHeures() != null ? emp.getHeures() : 0);
                    row.createCell(3).setCellValue(emp.getPourcentage());
                }
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }
}
