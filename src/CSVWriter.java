import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.FileWriter;
import java.io.IOException;

public class CSVWriter {

    private FileWriter fileWriter = null;

    public CSVWriter(String fileName) throws IOException {
        this.fileWriter = new FileWriter(fileName);
    }

    public void write(String data) throws IOException {
        this.fileWriter.append(data);
    }

    public void flushAndClose() throws IOException {
        this.fileWriter.flush();
        this.fileWriter.close();
    }
}