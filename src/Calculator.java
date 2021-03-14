import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Calculator {

    public static final int SKIP_THRESHOLD = 10000;
    public static final int CYCLES = 20;
    public static final double TIME_STEP = 0.00001;

    private static final double GM = 40000;
    private static final double Gm = 0;

    // set x1, vY1, z3 and vZ3 only:
    private static double x1 = -500, y1 = 0, x2 = -1 * x1, y2 = 0, z3 = 350;
    private static double vX1 = 0, vY1 = 7.07, vX2 = 0, vY2 = -1 * vY1, vZ3 = 0;

    public static final String DIRECTORY = String.format("C:\\HP Data\\%s, %s cycles, z3 = %s\\", TIME_STEP, CYCLES, z3);
    private static final String CSV_FILE_NAME = DIRECTORY + String.format("Coordinates, %s, %s, z3 = %s.csv", TIME_STEP, CYCLES, z3);
    private static final String IC_SHEET = "Initial Conditions";
    private static final String DATA_SHEET = "Data on a & b points";
    private static ExcelWriter excelInstance;
    private static CSVWriter csvWriter;

    private static double minX1, maxX1, minX2, maxX2;
    private static double minY1, maxY1, minY2, maxY2;

    private static ArrayList<Double> minX1List, maxX1List, minX2List, maxX2List;
    private static ArrayList<Double> minY1List, maxY1List, minY2List, maxY2List;

    private static int cycleCount = 0;
    private static int xCount = 0, yCount = 0;
    private static boolean touchedXAxis = false, touchedYAxis = false;
    private static int rows = 0;
    private static double a, b, e;
    private static int skipSteps = 0;

    public static void main(String[] args) throws IOException {

        initList();
        initCSV();
        initExcel();
        writePosDataToCSV();

        while (cycleCount < CYCLES) {
            calculate();
        }

        writeAxesPointsToExcel();
        writeEccentricityToExcel();
        excelInstance.saveFile();
    }


    private static void initCSV() throws IOException {
        File theDir = new File(DIRECTORY);
        if (!theDir.exists()) {
            theDir.mkdirs();
        }
        csvWriter = new CSVWriter(CSV_FILE_NAME);

        csvWriter.write("counts, ");
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
        vX1 += (((2 * Gm) / powXYZ) - GM / (2 * powXY)) * x1 * TIME_STEP;
        x1 += lastVXOfSphere1 * TIME_STEP;

        vY1 += (((2 * Gm) / powXYZ) - GM / (2 * powXY)) * y1 * TIME_STEP;
        y1 += lastVYOfSphere1 * TIME_STEP;


        // Just separating the calculation steps for M2
        double powX2 = Math.pow(x2, 2);
        double powY2 = Math.pow(y2, 2);
        powXY = Math.pow(powX2 + powY2, 1.5);
        powXYZ = Math.pow(powX2 + powY2 + powZ3, 1.5);


        // Calculate and update velocities and locations (M2):
        vX2 += (((2 * Gm) / powXYZ) - GM / (2 * powXY)) * x2 * TIME_STEP;
        x2 += lastVXOfSphere2 * TIME_STEP;

        vY2 += (((2 * Gm) / powXYZ) - GM / (2 * powXY)) * y2 * TIME_STEP;
        y2 += lastVYOfSphere2 * TIME_STEP;


        // Just separating the calculation steps for M3
        double powXDiff = Math.pow(x2 - x1, 2);
        double powYDiff = Math.pow(y2 - y1, 2);
        powXY = Math.pow(powXDiff + powYDiff + powZ3, 1.5);


        // Calculate and update velocities and locations (M3):
        vZ3 += (-1) * ((4 * GM) / powXY) * z3 * TIME_STEP;
        z3 += lastVZOfSphere3 * TIME_STEP;

        if (skipSteps < SKIP_THRESHOLD) {
            skipSteps++;
        } else {
            writePosDataToCSV();
            skipSteps = 0;
        }
        countCycle();
    }

    private static void writePosDataToCSV() throws IOException {
        csvWriter.write(rows + ",");
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
        rows++;
    }

    private static void initList() {
        minX1List = new ArrayList<>();
        maxX1List = new ArrayList<>();
        minX2List = new ArrayList<>();
        maxX2List = new ArrayList<>();
        minY1List = new ArrayList<>();
        maxY1List = new ArrayList<>();
        minY2List = new ArrayList<>();
        maxY2List = new ArrayList<>();
    }

    private static void countCycle() {

        switch (yCount) {
            case 0 -> {
                // find min X1
                if (minX1 > x1)
                    minX1 = x1;

                // find max X2
                if (maxX2 < x2)
                    maxX2 = x2;

                if (Math.round(x1) == 0 && !touchedYAxis) {
                    // save minX1 & maxX2
                    minX1List.add(minX1);
                    maxX2List.add(maxX2);

                    yCount++;
                    touchedYAxis = true;
                }
            }
            case 1 -> {
                if (maxX1 < x1)
                    maxX1 = x1;

                if (minX2 > x2)
                    minX2 = x2;

                if (Math.round(x1) == 0 && !touchedYAxis) {
                    maxX1List.add(maxX1);
                    minX2List.add(minX2);
                    yCount = 0;
                    touchedYAxis = true;
                    cycleCount++;

                }
            }
        }
        if (Math.round(x1) != 0 && touchedYAxis) {
            touchedYAxis = false;
        }

        switch (xCount) {
            case 0 -> {
                if (maxY1 < y1)
                    maxY1 = y1;
                if (minY2 > y2)
                    minY2 = y2;

                if (yCount == 1 && Math.round(y1) == 0 && !touchedXAxis) {
                    maxY1List.add(maxY1);
                    minY2List.add(minY2);

                    xCount++;
                    touchedXAxis = true;
                }
            }
            case 1 -> {
                if (minY1 > y1)
                    minY1 = y1;
                if (maxY2 > y2)
                    maxY2 = y2;
                if (Math.round(y1) == 0 && !touchedXAxis) {

                    minY1List.add(minY1);
                    maxY2List.add(maxY2);

                    xCount = 0;
                    touchedXAxis = true;
                }
            }
        }
        if (Math.round(y1) != 0 && touchedXAxis) {
            touchedXAxis = false;
        }


    }

    private static void initExcel() throws IOException {

        HashMap<String, String> dataMap = initMap();

        excelInstance = ExcelWriter.getInstance();

        Sheet sheet = excelInstance.getSheet(IC_SHEET);
        excelInstance.writeInitialConditions(sheet, dataMap);

        sheet = excelInstance.createSheet(DATA_SHEET);
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("Cycles");
        row.createCell(2).setCellValue("x1 (0T)");
        row.createCell(3).setCellValue("y1 (1/4T)");
        row.createCell(4).setCellValue("x1 (2/4T)");
        row.createCell(5).setCellValue("y1 (3/4T)");

        row.createCell(7).setCellValue("x2 (0T)");
        row.createCell(8).setCellValue("y2 (1/4T)");
        row.createCell(9).setCellValue("x2 (2/4T)");
        row.createCell(10).setCellValue("y2 (3/4T)");

        row.createCell(12).setCellValue("Average a");
        row.createCell(13).setCellValue("Average b");
        row.createCell(15).setCellValue("Eccentricity");
    }

    private static HashMap<String, String> initMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("time_step", String.valueOf(TIME_STEP));
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
        List<Double> cycleList = new ArrayList<>();
        for (double i = 1; i <= CYCLES; i++) {
            cycleList.add(i);
        }

        excelInstance.writeList(DATA_SHEET, 0, cycleList);
        excelInstance.writeList(DATA_SHEET, 2, minX1List);
        excelInstance.writeList(DATA_SHEET, 3, maxY1List);
        excelInstance.writeList(DATA_SHEET, 4, maxX1List);
        excelInstance.writeList(DATA_SHEET, 5, minY1List);

        excelInstance.writeList(DATA_SHEET, 7, maxX2List);
        excelInstance.writeList(DATA_SHEET, 8, minY2List);
        excelInstance.writeList(DATA_SHEET, 9, minX2List);
        excelInstance.writeList(DATA_SHEET, 10, maxY2List);
    }

    private static void writeEccentricityToExcel() {
        Sheet sheet = excelInstance.getSheet(DATA_SHEET);
        Row row;
        for (int i = 0; i < maxY1List.size() - 1; i++) {
            row = sheet.getRow(i + 1);

            a = Math.abs(((maxX1List.get(i) - minX1List.get(i))) / 2);
            b = Math.abs(((maxY1List.get(i) - minY1List.get(i))) / 2);
            e = Math.sqrt(1 - Math.pow(b / a, 2));
            row.createCell(12).setCellValue(a);
            row.createCell(13).setCellValue(b);
            row.createCell(15).setCellValue(e);
        }
    }
}
