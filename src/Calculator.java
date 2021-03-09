import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;

public class Calculator {

    private static final int CYCLES = 10;   // target: 100+ cycles
    private static final String IC_SHEET = "Initial Conditions";
    private static final String DATA_SHEET = "Data on a & b points";
//        private static final String ALL_DATA_SHEET = "All Data";
    private static final String CSV_FILE_NAME = "All Data.csv";
    private static ExcelRecord excelInstance;
    private static CSVWriter csvWriter;

    private static final double timeStep = 0.01;
    private static final double GM = 40000;
    private static final double Gm = 0;

    // set x1, vY1, z3 and vZ3 only:
    private static double x1 = -500, y1 = 0, x2 = -1 * x1, y2 = 0, z3 = 0;
    private static double vX1 = 0, vY1 = 7.07, vX2 = 0, vY2 = -1 * vY1, vZ3 = 1.5;

    private static double minY1, maxY1, minY2, maxY2;
    private static double minvX1, maxvX1, minvX2, maxvX2;

    private static ArrayList<Double> x1List0, y1List3;
    private static ArrayList<Double> x1List2, y1List1;
    private static ArrayList<Double> x2List0, y2List3;
    private static ArrayList<Double> x2List2, y2List1;
    private static ArrayList<Double> z3List0;
    private static ArrayList<Double> z3List2;
    private static ArrayList<Double> z3List3;
    private static ArrayList<Double> z3List1;
    private static ArrayList<Double> vy1List0, vx1List1, vy1List2, vx1List3;
    private static ArrayList<Double> vy2List0, vx2List1, vy2List2, vx2List3;
    private static ArrayList<Double> vz3List0, vz3List1, vz3List2, vz3List3;

    private static int cycleCount = 0;
    private static int yCount = 0;
    private static boolean touchedYAxis = true;
//    private static int rowNum = 1;

    public static void main(String[] args) throws IOException {

        initList();
        initExcel();
        initCSV();
//        writePosDataToExcel();
        writePosDataToCSV();

        while (cycleCount <= CYCLES) {
            calculate();
        }

        writeAxesPointsToExcel();
        excelInstance.saveFile();
    }

    private static void initCSV() throws IOException {
        csvWriter = new CSVWriter(CSV_FILE_NAME);

        csvWriter.write("x1, ");
        csvWriter.write("y1, ");
        csvWriter.write("vx1, ");
        csvWriter.write("vy1, ");
        csvWriter.write(", ");
        csvWriter.write("x2, ");
        csvWriter.write("y2, ");
        csvWriter.write("vx2, ");
        csvWriter.write("vy2, ");
        csvWriter.write(", ");
        csvWriter.write("z3, ");
        csvWriter.write("vz3 \n");
    }

    private static void calculate() throws IOException {
        // record current velocities:
        double lastVXOfSphere1 = vX1;
        double lastVYOfSphere1 = vY1;
        double lastVXOfSphere2 = vX2;
        double lastVYOfSphere2 = vY2;
        double lastVZOfSphere3 = vZ3;


        // Just separating the calculation steps for M1
        double powX1 = Math.pow(x1, 2);
        double powY1 = Math.pow(y1, 2);
        double powZ3 = Math.pow(z3, 2);
        double powXY = Math.pow(powX1 + powY1, 1.5);
        double powXYZ = Math.pow(powX1 + powY1 + powZ3, 1.5);


        // Calculate and update velocities and locations (M1):
        vX1 += (((2 * Gm) / powXYZ) - GM / (2 * powXY)) * x1 * timeStep;
        x1 += lastVXOfSphere1 * timeStep;

        vY1 += (((2 * Gm) / powXYZ) - GM / (2 * powXY)) * y1 * timeStep;
        y1 += lastVYOfSphere1 * timeStep;


        // Just separating the calculation steps for M2
        double powX2 = Math.pow(x2, 2);
        double powY2 = Math.pow(y2, 2);
        powXY = Math.pow(powX2 + powY2, 1.5);
        powXYZ = Math.pow(powX2 + powY2 + powZ3, 1.5);


        // Calculate and update velocities and locations (M2):
        vX2 += (((2 * Gm) / powXYZ) - GM / (2 * powXY)) * x2 * timeStep;
        x2 += lastVXOfSphere2 * timeStep;

        vY2 += (((2 * Gm) / powXYZ) - GM / (2 * powXY)) * y2 * timeStep;
        y2 += lastVYOfSphere2 * timeStep;


        // Just separating the calculation steps for M3
        double powXDiff = Math.pow(x2 - x1, 2);
        double powYDiff = Math.pow(y2 - y1, 2);
        powXY = Math.pow(powXDiff + powYDiff + powZ3, 1.5);


        // Calculate and update velocities and locations (M3):
        vZ3 += (-1) * ((4 * GM) / powXY) * z3 * timeStep;
        z3 += lastVZOfSphere3 * timeStep;

//        writePosDataToExcel();
        writePosDataToCSV();
        countCycle();
    }

//    private static void writePosDataToExcel() {
//
//        excelInstance.write(ALL_DATA_SHEET, rowNum, 1, x1);
//        excelInstance.write(ALL_DATA_SHEET, rowNum, 2, y1);
//        excelInstance.write(ALL_DATA_SHEET, rowNum, 3, vX1);
//        excelInstance.write(ALL_DATA_SHEET, rowNum, 4, vY1);
//
//        excelInstance.write(ALL_DATA_SHEET, rowNum, 7, x2);
//        excelInstance.write(ALL_DATA_SHEET, rowNum, 8, y2);
//        excelInstance.write(ALL_DATA_SHEET, rowNum, 9, vX2);
//        excelInstance.write(ALL_DATA_SHEET, rowNum, 10, vY2);
//
//
//        excelInstance.write(ALL_DATA_SHEET, rowNum, 13, z3);
//        excelInstance.write(ALL_DATA_SHEET, rowNum, 14, vZ3);
//
//        rowNum++;
//    }

    private static void writePosDataToCSV() throws IOException {
        csvWriter.write(x1 + ",");
        csvWriter.write(y1 + ",");
        csvWriter.write(vX1 + ",");
        csvWriter.write(vY1 + ",");
        csvWriter.write(", ");
        csvWriter.write(x2 + ",");
        csvWriter.write(y2 + ",");
        csvWriter.write(vX2 + ",");
        csvWriter.write(vY2 + ",");
        csvWriter.write(", ");
        csvWriter.write(z3 + ",");
        csvWriter.write(vZ3 + "\n");
    }

    private static void initList() {
        x1List0 = new ArrayList<>();
        y1List3 = new ArrayList<>();
        x2List0 = new ArrayList<>();
        y2List3 = new ArrayList<>();
        x1List2 = new ArrayList<>();
        y1List1 = new ArrayList<>();
        x2List2 = new ArrayList<>();
        y2List1 = new ArrayList<>();
        z3List0 = new ArrayList<>();
        z3List3 = new ArrayList<>();
        z3List2 = new ArrayList<>();
        z3List1 = new ArrayList<>();

        vy1List0 = new ArrayList<>();
        vx1List1 = new ArrayList<>();
        vy1List2 = new ArrayList<>();
        vx1List3 = new ArrayList<>();
        vy2List0 = new ArrayList<>();
        vx2List1 = new ArrayList<>();
        vy2List2 = new ArrayList<>();
        vx2List3 = new ArrayList<>();
        vz3List0 = new ArrayList<>();
        vz3List1 = new ArrayList<>();
        vz3List2 = new ArrayList<>();
        vz3List3 = new ArrayList<>();

        x1List0.add(x1);
        x2List0.add(x2);
        z3List0.add(z3);
        vy1List0.add(vY1);
        vy2List0.add(vY2);
        vz3List0.add(vZ3);

    }

    private static void countCycle() {
        if (yCount == 0) {

            if (maxY1 < y1)
                maxY1 = y1;

            if (y2 < minY2)
                minY2 = y2;

            if (maxvX1 < vX1)
                maxvX1 = vX1;

            if (minvX2 > vX2)
                minvX2 = vX2;
        } else if (yCount == 1) {

            if (y1 < minY1)
                minY1 = y1;

            if (maxY2 < y2)
                maxY2 = y2;

            if (minvX1 > vX1)
                minvX1 = vX1;

            if (maxvX2 < vX2)
                maxvX2 = vX2;
        }

        if (Math.round(y1) == 0 && !touchedYAxis) {
            yCount++;
            touchedYAxis = true;

            if (yCount == 1) {
                // 2/4T

                y1List1.add(maxY1);
                y2List1.add(minY2);
                z3List1.add(null);
                vx1List1.add(maxvX1);
                vx2List1.add(minvX2);
                vz3List1.add(null);

                x1List2.add(x1);
                x2List2.add(x2);
                z3List2.add(z3);
                vy1List2.add(vY1);
                vy2List2.add(vY2);
                vz3List2.add(vZ3);

            } else if (yCount == 2) {
                // One full cycle completes
                yCount = 0;
                cycleCount++;
                x1List0.add(x1);
                x2List0.add(x2);
                z3List0.add(z3);
                vy1List0.add(vY1);
                vy2List0.add(vY2);
                vz3List0.add(vZ3);

                y1List3.add(minY1);
                y2List3.add(maxY2);
                z3List3.add(null);
                vx1List3.add(minvX1);
                vx2List3.add(maxvX2);
                vz3List3.add(null);
            }
        } else if (Math.round(y1) != 0 && touchedYAxis) {
            touchedYAxis = false;
        }
    }

    private static void initExcel() throws IOException {

        HashMap<String, String> dataMap = initMap();

        excelInstance = ExcelRecord.getInstance();

        Sheet sheet = excelInstance.getSheet(IC_SHEET);
        excelInstance.writeInitialConditions(sheet, dataMap);

        sheet = excelInstance.createSheet(DATA_SHEET);
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("x1 (0T)");
        row.createCell(1).setCellValue("y1 (1/4T)");
        row.createCell(2).setCellValue("x1 (2/4T)");
        row.createCell(3).setCellValue("y1 (3/4T)");

        row.createCell(5).setCellValue("x2 (0T)");
        row.createCell(6).setCellValue("y2 (1/4T)");
        row.createCell(7).setCellValue("x2 (2/4T)");
        row.createCell(8).setCellValue("y2 (3/4T)");

        row.createCell(10).setCellValue("z3 (0T)");
        row.createCell(11).setCellValue("z3 (1/4T)");
        row.createCell(12).setCellValue("z3 (2/4T)");
        row.createCell(13).setCellValue("z3 (3/4T)");

        row.createCell(15).setCellValue("vy1 (0T)");
        row.createCell(16).setCellValue("vx1 (1/4T)");
        row.createCell(17).setCellValue("vy1 (2/4T)");
        row.createCell(18).setCellValue("vx1 (3/4T)");

        row.createCell(20).setCellValue("vy2 (0T)");
        row.createCell(21).setCellValue("vx2 (1/4T)");
        row.createCell(22).setCellValue("vy2 (2/4T)");
        row.createCell(23).setCellValue("vx2 (3/4T)");

        row.createCell(25).setCellValue("vz3 (0T)");
        row.createCell(26).setCellValue("vz3 (1/4T)");
        row.createCell(27).setCellValue("vz3 (2/4T)");
        row.createCell(28).setCellValue("vz3 (3/4T)");

//        sheet = excelInstance.createSheet(ALL_DATA_SHEET);
//        row = sheet.createRow(0);
//
//        row.createCell(0).setCellValue("m1:");
//        row.createCell(1).setCellValue("x1");
//        row.createCell(2).setCellValue("y1");
//        row.createCell(3).setCellValue("vx1");
//        row.createCell(4).setCellValue("vy1");
//
//        row.createCell(6).setCellValue("m2:");
//        row.createCell(7).setCellValue("x2");
//        row.createCell(8).setCellValue("y2");
//        row.createCell(9).setCellValue("vx2");
//        row.createCell(10).setCellValue("vy2");
//
//        row.createCell(12).setCellValue("m3:");
//        row.createCell(13).setCellValue("z3");
//        row.createCell(14).setCellValue("vz3");
    }

    private static HashMap<String, String> initMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("time_step", String.valueOf(timeStep));
        map.put("GM", String.valueOf(GM));
        map.put("Gm", String.valueOf(Gm));
        map.put("x1", String.valueOf(x1));
        map.put("y1", "0");
        map.put("vX1", "0");
        map.put("vY1", String.valueOf(vY1));
        map.put("x2", String.valueOf(x2));
        map.put("y2", "0");
        map.put("vX2", "0");
        map.put("vY2", String.valueOf(vY2));
        map.put("z3", String.valueOf(z3));
        map.put("vZ3", String.valueOf(vZ3));
        return map;
    }

    private static void writeAxesPointsToExcel() {
        excelInstance.writeList(DATA_SHEET, 0, x1List0);
        excelInstance.writeList(DATA_SHEET, 1, y1List1);
        excelInstance.writeList(DATA_SHEET, 2, x1List2);
        excelInstance.writeList(DATA_SHEET, 3, y1List3);

        excelInstance.writeList(DATA_SHEET, 5, x2List0);
        excelInstance.writeList(DATA_SHEET, 6, y2List1);
        excelInstance.writeList(DATA_SHEET, 7, x2List2);
        excelInstance.writeList(DATA_SHEET, 8, y2List3);

        excelInstance.writeList(DATA_SHEET, 10, z3List0);
        excelInstance.writeList(DATA_SHEET, 11, z3List1);
        excelInstance.writeList(DATA_SHEET, 12, z3List2);
        excelInstance.writeList(DATA_SHEET, 13, z3List3);

        excelInstance.writeList(DATA_SHEET, 15, vy1List0);
        excelInstance.writeList(DATA_SHEET, 16, vx1List1);
        excelInstance.writeList(DATA_SHEET, 17, vy1List2);
        excelInstance.writeList(DATA_SHEET, 18, vx1List3);

        excelInstance.writeList(DATA_SHEET, 20, vy2List0);
        excelInstance.writeList(DATA_SHEET, 21, vx2List1);
        excelInstance.writeList(DATA_SHEET, 22, vy2List2);
        excelInstance.writeList(DATA_SHEET, 23, vx2List3);

        excelInstance.writeList(DATA_SHEET, 25, vz3List0);
        excelInstance.writeList(DATA_SHEET, 26, vz3List1);
        excelInstance.writeList(DATA_SHEET, 27, vz3List2);
        excelInstance.writeList(DATA_SHEET, 28, vz3List3);
    }

}
