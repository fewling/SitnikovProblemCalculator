import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class ExcelWriter {

    private static final String FILE_NAME = "Results.xlsx";
    private static ExcelWriter instance = null;

    private XSSFWorkbook workbook;

    public ExcelWriter() throws IOException {
        File file = new File(FILE_NAME);
        if (file.exists()) {
            this.workbook = new XSSFWorkbook(new FileInputStream(FILE_NAME));
        } else {
            this.workbook = new XSSFWorkbook();
            this.workbook.write(new FileOutputStream(FILE_NAME));
        }
    }

    public static ExcelWriter getInstance() throws IOException {
        if (instance == null) {
            instance = new ExcelWriter();
        }
        return instance;
    }

    public void writeInitialConditions(Sheet targetSheet, HashMap<String, String> dataMap) {

        Row row1 = targetSheet.createRow(0);
        row1.createCell(0).setCellValue("Timestep");
        row1.createCell(1).setCellValue(dataMap.get("time_step"));
        row1.createCell(3).setCellValue("x1");
        row1.createCell(4).setCellValue("y1");
        row1.createCell(5).setCellValue("vx1");
        row1.createCell(6).setCellValue("vy1");
        row1.createCell(8).setCellValue("x2");
        row1.createCell(9).setCellValue("y2");
        row1.createCell(10).setCellValue("vx2");
        row1.createCell(11).setCellValue("vy2");
        row1.createCell(13).setCellValue("z3");
        row1.createCell(14).setCellValue("vz3");


        Row row2 = targetSheet.createRow(1);
        row2.createCell(0).setCellValue("GM");
        row2.createCell(1).setCellValue(dataMap.get("GM"));
        row2.createCell(3).setCellValue(dataMap.get("x1"));
        row2.createCell(4).setCellValue(dataMap.get("y1"));
        row2.createCell(5).setCellValue(dataMap.get("vX1"));
        row2.createCell(6).setCellValue(dataMap.get("vY1"));
        row2.createCell(8).setCellValue(dataMap.get("x2"));
        row2.createCell(9).setCellValue(dataMap.get("y2"));
        row2.createCell(10).setCellValue(dataMap.get("vX2"));
        row2.createCell(11).setCellValue(dataMap.get("vY2"));
        row2.createCell(13).setCellValue(dataMap.get("z3"));
        row2.createCell(14).setCellValue(dataMap.get("vZ3"));

        Row row3 = targetSheet.createRow(2);
        row3.createCell(0).setCellValue("Gm");
        row3.createCell(1).setCellValue(dataMap.get("Gm"));

        Row row4 = targetSheet.createRow(3);
        row4.createCell(0).setCellValue("Cycle counts");
        row4.createCell(1).setCellValue(dataMap.get("cycle count"));
        saveFile();
    }

    public void clearSheet(String sheetName) {
        if (this.workbook.getSheet(sheetName) != null) {
            Sheet sheet = this.workbook.getSheet(sheetName);
            Iterator<Row> rowIte = sheet.iterator();
            while (rowIte.hasNext()) {
                rowIte.next();
                rowIte.remove();
            }
        }
    }

    public Sheet createSheet(String sheetName) {
        if (this.workbook.getSheet(sheetName) != null) {
            return this.workbook.getSheet(sheetName);
        } else {
            return this.workbook.createSheet(sheetName);
        }
    }

    public void saveFile() {
        try {
            this.workbook.write(new FileOutputStream(FILE_NAME));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Sheet getSheet(String sheetName) {
        if (this.workbook.getSheet(sheetName) == null) {
            return this.createSheet(sheetName);
        } else {
            return this.workbook.getSheet(sheetName);
        }
    }

    public void writeList(String sheetName, int colNum, List<Double> dataList) {
        Sheet sheet = this.getSheet(sheetName);
        int rowNum = 1;
        for (Double val : dataList) {
            Row row;
            if (sheet.getRow(rowNum) == null) {
                row = sheet.createRow(rowNum);
            } else {
                row = sheet.getRow(rowNum);
            }
            if (val == null) {
                row.createCell(colNum).setCellValue("null");
            } else {
                row.createCell(colNum).setCellValue(val);
            }
            rowNum++;
        }
    }

    public void write(String sheetName, int rowNum, int colNum, double data) {
        Sheet sheet = this.getSheet(sheetName);
        Row row;
        if (sheet.getRow(rowNum) == null) {
            row = sheet.createRow(rowNum);
        } else {
            row = sheet.getRow(rowNum);
        }
        row.createCell(colNum).setCellValue(data);
    }
}
