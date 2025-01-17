package app;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class b_combineCSV {
    public static void main(String[] args) {
        String fakeNewsFilePath = "Fake.csv";
        String trueNewsFilePath = "True.csv";
        String outputFilePath = "CombinedNews.csv"; // New file to save combined data

        List<String[]> combinedRecords = new ArrayList<>();

        try {
            // Read fake news data
            CSVReader fakeReader = new CSVReader(new FileReader(fakeNewsFilePath));
            List<String[]> fakeRecords = fakeReader.readAll();
            fakeReader.close();

            // Add a label to each fake news record and add to combined list
            for (String[] record : fakeRecords) {
                String[] labeledRecord = new String[record.length + 1];
                System.arraycopy(record, 0, labeledRecord, 0, record.length);
                labeledRecord[record.length] = "fake";  // Adding the label "fake"
                combinedRecords.add(labeledRecord);
            }

            // Read true news data
            CSVReader trueReader = new CSVReader(new FileReader(trueNewsFilePath));
            List<String[]> trueRecords = trueReader.readAll();
            trueReader.close();

            // Add a label to each true news record and add to combined list
            for (String[] record : trueRecords) {
                String[] labeledRecord = new String[record.length + 1];
                System.arraycopy(record, 0, labeledRecord, 0, record.length);
                labeledRecord[record.length] = "true";  // Adding the label "true"
                combinedRecords.add(labeledRecord);
            }

            CSVWriter writer = new CSVWriter(new FileWriter(outputFilePath));
            writer.writeAll(combinedRecords);
            writer.close();

            System.out.println("Combined data has been written to " + outputFilePath);

        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }
}
