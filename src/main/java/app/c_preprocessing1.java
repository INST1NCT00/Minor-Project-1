package app;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class c_preprocessing1 {

    // Set of stop words for removal, loaded dynamically from a file
    private static Set<String> stopWords = new HashSet<>();

    // Initialize Stanford CoreNLP pipeline
    private static final Properties props = new Properties();
    private static final StanfordCoreNLP pipeline;

    static {
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma");
        pipeline = new StanfordCoreNLP(props);
    }

    public static void main(String[] args) {
        // File paths
        String inputFilePath = "CombinedNews.csv";
        String outputFilePath = "PreprocessedNews.csv";
        String stopWordsFilePath = "stopwords.txt"; // Path to stop words file

        // Step 1: Load stop words from file
        loadStopWordsFromFile(stopWordsFilePath);

        // Step 2: Load data
        List<String[]> records = readCSV(inputFilePath);

        // Step 3: Preprocess data
        c_preprocessing1 preprocessor = new c_preprocessing1();
        List<String[]> preprocessedRecords = preprocessor.preprocessData(records);

        // Step 4: Save preprocessed data to a new CSV file
        saveCSV(preprocessedRecords, outputFilePath);

        System.out.println("Preprocessing completed. Data saved to " + outputFilePath);
    }

    /**
     * Loads stop words from the specified file into a Set.
     *
     * @param filePath The path to the stop words file.
     */
    private static void loadStopWordsFromFile(String filePath) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            for (String line : lines) {
                String trimmedLine = line.trim().toLowerCase(); // Ensure the stop word is in lowercase and trimmed
                if (!trimmedLine.isEmpty()) { // Skip empty lines
                    stopWords.add(trimmedLine);
                }
            }
            System.out.println("Stop words loaded successfully: " + stopWords);
        } catch (IOException e) {
            System.err.println("Error loading stop words from file: " + e.getMessage());
        }
    }

    /**
     * Reads the CSV file and returns the list of records.
     *
     * @param filePath The path to the CSV file.
     * @return List of records read from the CSV file.
     */
    public static List<String[]> readCSV(String filePath) {
        List<String[]> records = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            records = reader.readAll();
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
        return records;
    }

    /**
     * Saves the processed records to a new CSV file.
     *
     * @param records  The list of processed records.
     * @param filePath The path where the CSV file should be saved.
     */
    public static void saveCSV(List<String[]> records, String filePath) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            writer.writeAll(records);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Main method to handle all preprocessing tasks for the dataset.
     *
     * @param records The raw data records from the CSV file.
     * @return List of preprocessed data records.
     */
    public List<String[]> preprocessData(List<String[]> records) {
        // Step 1: Handle missing values
        records = removeMissingValues(records);

        // Step 2: Clean text data (title and text columns)
        records = cleanRecords(records);

        // Step 3: Add additional features (e.g., text length)
        records = addTextLengthFeature(records);

        return records;
    }

    /**
     * Removes records with missing critical fields (title, text, classification).
     *
     * @param records The raw data records.
     * @return Filtered list of records without missing values.
     */
    private List<String[]> removeMissingValues(List<String[]> records) {
        List<String[]> filteredRecords = new ArrayList<>();
        for (String[] record : records) {
            if (record[0] != null && !record[0].isEmpty() && // Title
                    record[1] != null && !record[1].isEmpty() && // Text
                    record[4] != null && !record[4].isEmpty()) { // Classification
                filteredRecords.add(record);
            }
        }
        return filteredRecords;
    }

    /**
     * Cleans the text data by removing URLs, converting to lowercase, removing special characters,
     * tokenizing, removing stop words, and applying lemmatization.
     *
     * @param records The raw data records.
     * @return List of records with cleaned text.
     */
    private List<String[]> cleanRecords(List<String[]> records) {
        List<String[]> cleanedRecords = new ArrayList<>();
        for (String[] record : records) {
            // Remove URLs from title and text
            String cleanedTitle = removeUrls(record[0]);
            String cleanedText = removeUrls(record[1]);

            // Tokenize title and text
            List<String> titleTokens = tokenize(cleanedTitle);
            List<String> textTokens = tokenize(cleanedText);

            // Clean title and text using tokens (removing stop words and special characters)
            cleanedTitle = cleanText(titleTokens);
            cleanedText = cleanText(textTokens);

            // Apply lemmatization
            String lemmatizedTitle = lemmatizeText(cleanedTitle);
            String lemmatizedText = lemmatizeText(cleanedText);

            String subject = record[2]; // Leave subject as is for now
            String date = record[3]; // Leave date as is for now
            String classification = record[4];
            cleanedRecords.add(new String[]{lemmatizedTitle, lemmatizedText, subject, date, classification});
        }
        return cleanedRecords;
    }

    /**
     * Removes URLs from the given text.
     *
     * @param text The input text string.
     * @return The text without URLs.
     */
    private String removeUrls(String text) {
        // Regular expression pattern to detect URLs in the text
        String urlPattern = "(https?://\\S+|www\\.[\\w-]+\\.[\\w-]+|[\\w-]+\\.(com|org|net|io|gov|edu|info|co|uk))";
        return text.replaceAll(urlPattern, "").trim(); // Remove all URLs from the text
    }

    /**
     * Tokenizes a given text string into words.
     *
     * @param text The input text string.
     * @return A list of words (tokens) from the text.
     */
    private List<String> tokenize(String text) {
        return Arrays.asList(text.split("\\s+")); // Split text into words based on whitespace
    }

    /**
     * Cleans a list of text tokens by lowercasing, removing non-alphabetic characters,
     * and eliminating stop words.
     *
     * @param tokens The list of raw text tokens.
     * @return Cleaned text string.
     */
    private String cleanText(List<String> tokens) {
        StringBuilder cleanedText = new StringBuilder();
        for (String token : tokens) {
            // Convert to lowercase
            token = token.toLowerCase();
            // Remove special characters and numbers
            token = token.replaceAll("[^a-zA-Z]", ""); // Keep only alphabetic characters
            // Check if the token is a stop word or empty after cleaning
            if (!stopWords.contains(token) && !token.isEmpty()) {
                cleanedText.append(token).append(" ");
            }
        }
        return cleanedText.toString().trim();
    }

    /**
     * Applies lemmatization to the given text.
     *
     * @param text The cleaned text string.
     * @return The lemmatized version of the text.
     */
    private String lemmatizeText(String text) {
        StringBuilder lemmatizedText = new StringBuilder();
        CoreDocument document = new CoreDocument(text);
        pipeline.annotate(document);
        for (CoreSentence sentence : document.sentences()) {
            for (CoreLabel token : sentence.tokens()) {
                lemmatizedText.append(token.lemma()).append(" ");
            }
        }
        return lemmatizedText.toString().trim();
    }

    /**
     * Adds a feature indicating the length of the text.
     *
     * @param records The list of records with cleaned text.
     * @return List of records with added text length feature.
     */
    private List<String[]> addTextLengthFeature(List<String[]> records) {
        List<String[]> recordsWithLength = new ArrayList<>();
        for (String[] record : records) {
            int textLength = record[1].split("\\s+").length; // Count words in text
            String[] newRecord = Arrays.copyOf(record, record.length + 1);
            newRecord[newRecord.length - 1] = String.valueOf(textLength);
            recordsWithLength.add(newRecord);
        }
        return recordsWithLength;
    }
}
