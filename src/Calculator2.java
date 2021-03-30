import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Calculator2 {


    private static final String POS_FILE_NAME = "positions & velocities.xlsx";
    private static final String AXES_FILE_NAME = "eccentricity.xlsx";
    public static final int SKIP_THRESHOLD = 1000;    //100000
    public static final int CYCLES = 5;
    public static final double TIME_STEP = 0.1; //0.00001

    private static final double GM = 40000;
    private static final double Gm = 0;

    // set x1, vY1, z3 and vZ3 only:
    private static double x1 = -500, y1 = 0, x2 = -1 * x1, y2 = 0, z3 = 0;
    private static double vX1 = 0, vY1 = 7.07, vX2 = 0, vY2 = -1 * vY1, vZ3 = 1;

    private static double minX1, maxX1, minX2, maxX2;
    private static double minY1, maxY1, minY2, maxY2;

    private static ArrayList<Double> minX1List, maxX1List, minX2List, maxX2List;
    private static ArrayList<Double> minY1List, maxY1List, minY2List, maxY2List;

    private static int cycleCount = 0;
    private static int xCount = 0, yCount = 0;
    private static boolean touchedXAxis = false, touchedYAxis = false;
    private static int posRowNum = 1;
    private static double a, b, e;
    private static int skipSteps = 0;
    private static XSSFWorkbook posWorkBook, axesWorkbook;
    private static String sheetName;
    private static XSSFRow posRow;
    private static double variableStep = 0.5;

    public static void main(String[] args) throws IOException {

        initList();

        File posFile = new File(POS_FILE_NAME);
        if (posFile.exists()) {
            posWorkBook = new XSSFWorkbook(new FileInputStream(POS_FILE_NAME));
        } else {
            posWorkBook = new XSSFWorkbook();
            posWorkBook.write(new FileOutputStream(POS_FILE_NAME));
        }

        File axesFile = new File(AXES_FILE_NAME);
        if (axesFile.exists()) {
            axesWorkbook = new XSSFWorkbook(new FileInputStream(AXES_FILE_NAME));
        } else {
            axesWorkbook = new XSSFWorkbook();
            axesWorkbook.write(new FileOutputStream(AXES_FILE_NAME));
        }

        String targetName = "vz3";
        double targetValue = 2;

        // if testing diff variable, remember to update that in updateVariables()
        while (vZ3 < targetValue) {

            sheetName = String.format("%s = %s", targetName, vZ3);
            initSheet();

            while (cycleCount < CYCLES) {
                calculate();
                countCycle();

                if (skipSteps < SKIP_THRESHOLD) {
                    skipSteps++;
                } else {
                    writePosDataToExcel();
                    skipSteps = 0;
                }
            }
            
            writeAxesPointsToExcel();
            writeEccentricityToExcel();
            
            posWorkBook.write(new FileOutputStream(POS_FILE_NAME));    // Save the file once it completes one round.
            axesWorkbook.write(new FileOutputStream(AXES_FILE_NAME));
            System.out.println(String.format("Finished: %s = %s", targetName, vZ3));

            // Reset the variables and lists
            updateVariables();
        }


    }

    private static void updateVariables() {

        cycleCount = 0;
        posRowNum = 1;

        x1 = -500;
        y1 = 0;
        x2 = -1 * x1;
        y2 = 0;
        z3 = 0;

        vX1 = 0;
        vY1 = 7.07;
        vX2 = 0;
        vY2 = -1 * vY1;
        vZ3 = 1 + variableStep;
        variableStep += 0.5;

        minX1List.clear();
        maxX1List.clear();
        minX2List.clear();
        maxX2List.clear();
        minY1List.clear();
        maxY1List.clear();
        minY2List.clear();
        maxY2List.clear();
    }

    private static void initSheet() {
        Sheet posSheet = posWorkBook.createSheet(sheetName);
        Row row = posSheet.createRow(0);

        row.createCell(2).setCellValue("x1");
        row.createCell(3).setCellValue("y1");
        row.createCell(4).setCellValue("vx1");
        row.createCell(5).setCellValue("vy1");

        row.createCell(7).setCellValue("x2");
        row.createCell(8).setCellValue("y2");
        row.createCell(9).setCellValue("vx2");
        row.createCell(10).setCellValue("vy2");

        row.createCell(12).setCellValue("z3");
        row.createCell(13).setCellValue("vz3");

        Sheet axesSheet = axesWorkbook.createSheet(sheetName);
        row = axesSheet.createRow(0);
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

        // Write initial conditions:
        writePosDataToExcel();
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

    private static void writePosDataToExcel() {

        posRow = posWorkBook.getSheet(sheetName).createRow(posRowNum);
        posRow.createCell(2).setCellValue(x1);
        posRow.createCell(3).setCellValue(y1);
        posRow.createCell(4).setCellValue(vX1);
        posRow.createCell(5).setCellValue(vY1);

        posRow.createCell(7).setCellValue(x2);
        posRow.createCell(8).setCellValue(y2);
        posRow.createCell(9).setCellValue(vX2);
        posRow.createCell(10).setCellValue(vY2);

        posRow.createCell(12).setCellValue(z3);
        posRow.createCell(13).setCellValue(vZ3);
        posRowNum++;
    }

    private static void writeAxesPointsToExcel() {

        ArrayList<Double> cycleList = new ArrayList<>();
        for (double i = 1; i <= CYCLES; i++) {
            cycleList.add(i);
        }

        writeListToExcel(0, cycleList);
        writeListToExcel(2, minX1List);
        writeListToExcel(3, maxY1List);
        writeListToExcel(4, maxX1List);
        writeListToExcel(5, minY1List);

        writeListToExcel(7, maxX2List);
        writeListToExcel(8, minY2List);
        writeListToExcel(9, minX2List);
        writeListToExcel(10, maxY2List);
    }

    private static void writeListToExcel(int colNum, ArrayList<Double> list) {
        Sheet sheet = axesWorkbook.getSheet(sheetName);
        int rowNum = 1;
        for (Double val : list) {
            Row row;

            if (sheet.getRow(rowNum) == null)
                row = sheet.createRow(rowNum);
            else
                row = sheet.getRow(rowNum);

            if (row.getCell(colNum) == null)
                row.createCell(colNum).setCellValue(val);
            else
                row.getCell(colNum).setCellValue(val);

            rowNum++;
        }
    }

    private static void writeEccentricityToExcel() {
        Sheet sheet = axesWorkbook.getSheet(sheetName);
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
