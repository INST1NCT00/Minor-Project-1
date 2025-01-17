package app;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class d_boWords {

    public static void main(String[] args) {
        // Step 1: Read preprocessed CSV file
        String inputFilePath = "PreprocessedNewsTemp.csv"; // Input file path
        List<String[]> records = readCSV(inputFilePath);

        // Step 2: Create vocabulary with a frequency count
        Set<String> vocabularySet = new HashSet<>(); // Use a Set to ensure uniqueness
        for (String[] record : records) {
            String content = record[0] + " " + record[1]; // Combine title and text columns
            String[] tokens = content.split("\\s+"); // Tokenize content
            for (String token : tokens) {
                token = token.toLowerCase(); // Convert to lowercase for uniformity
                if (!token.isEmpty()) {
                    vocabularySet.add(token); // Add unique tokens to the set
                }
            }
        }
        List<String> vocabulary = new ArrayList<>(vocabularySet); // Convert set to list for ordered access

        // Step 3: Construct the BoW matrix
        int[][] bowMatrix = new int[records.size()][vocabulary.size()];

        for (int i = 0; i < records.size(); i++) {
            String content = records.get(i)[0] + " " + records.get(i)[1]; // Combine title and text columns
            String[] tokens = content.split("\\s+"); // Tokenize content
            for (String token : tokens) {
                token = token.toLowerCase(); // Ensure case uniformity
                if (vocabularySet.contains(token)) {
                    int columnIndex = vocabulary.indexOf(token);
                    bowMatrix[i][columnIndex]++;
                }
            }
        }

        // Step 4: Save the BoW matrix with vocabulary as headers in a CSV file, including the label column
        saveMatrixWithVocabularyToCSV(bowMatrix, vocabulary, records, "BagOfWords_Feature_Matrix_with_Labels1.csv");

        // Step 5: Save the vocabulary to a separate CSV file
        saveVocabularyToCSV(vocabulary, "Vocabulary.csv");

        System.out.println("Bag-of-Words feature matrix with labels and vocabulary file saved successfully.");
    }

    public static List<String[]> readCSV(String filePath) {
        List<String[]> records = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                records.add(line.split(",", -1)); // Split by comma for each row, including empty fields
            }
        } catch (IOException e) {
            System.err.println("Error reading the CSV file: " + e.getMessage());
        }
        return records;
    }

    public static void saveMatrixWithVocabularyToCSV(int[][] matrix, List<String> vocabulary, List<String[]> records, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            // Write the vocabulary as the header row
            writer.write("DocumentID,Label,");
            for (String word : vocabulary) {
                writer.write(word + ",");
            }
            writer.write("\n");

            // Write the BoW matrix with DocumentID and label as the first two columns
            for (int i = 0; i < matrix.length; i++) {
                writer.write("Doc" + (i + 1) + ","); // Add DocumentID for each row
                writer.write(records.get(i)[records.get(i).length - 2] + ","); // Add classification label (last column)
                StringBuilder rowBuilder = new StringBuilder();
                for (int value : matrix[i]) {
                    rowBuilder.append(value).append(",");
                }
                writer.write(rowBuilder.deleteCharAt(rowBuilder.length() - 1).toString() + "\n"); // Remove trailing comma
            }
        } catch (IOException e) {
            System.err.println("Error saving the matrix with vocabulary to CSV: " + e.getMessage());
        }
    }

    public static void saveVocabularyToCSV(List<String> vocabulary, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            // Write the vocabulary words line by line
            for (String word : vocabulary) {
                writer.write(word + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error saving vocabulary to CSV: " + e.getMessage());
        }
    }
}