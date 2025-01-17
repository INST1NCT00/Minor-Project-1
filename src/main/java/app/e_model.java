package app;

import java.io.*;
import java.util.*;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.Evaluation;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.core.Attribute;
import weka.core.DenseInstance;

public class e_model {

    public static void main(String[] args) throws Exception {
        // Load the dataset
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File("BagOfWords_Feature_Matrix_with_Labels.csv"));
        Instances data = loader.getDataSet();

        // Remove the Document column (first column)
        data.deleteAttributeAt(0);

        // Set the Label column as the class attribute (now the first column after removing Document)
        data.setClassIndex(0);

        // Perform stratified 10-fold cross-validation
        NaiveBayes model = new NaiveBayes();
        Evaluation evaluation = new Evaluation(data);

        int numFolds = 10;
        double totalAccuracy = 0;
        int[][] cumulativeConfusionMatrix = new int[data.numClasses()][data.numClasses()];

        for (int fold = 0; fold < numFolds; fold++) {
            // Split data into training and testing for this fold
            Instances train = data.trainCV(numFolds, fold);
            Instances test = data.testCV(numFolds, fold);

            // Build classifier and evaluate on test set
            model.buildClassifier(train);
            evaluation.evaluateModel(model, test);

            // Display accuracy for this fold
            double accuracy = (1 - evaluation.errorRate()) * 100;
            System.out.println("=== Confusion Matrix for Fold " + (fold + 1) + " ===");
            System.out.println(evaluation.toMatrixString());
            System.out.printf("Accuracy for Fold %d: %.2f%%\n\n", (fold + 1), accuracy);

            totalAccuracy += accuracy;

            // Update cumulative confusion matrix
            double[][] foldMatrix = evaluation.confusionMatrix();
            for (int i = 0; i < foldMatrix.length; i++) {
                for (int j = 0; j < foldMatrix[i].length; j++) {
                    cumulativeConfusionMatrix[i][j] += (int) foldMatrix[i][j];
                }
            }
        }

        // Display final cumulative confusion matrix
        System.out.println("=== Final Cumulative Confusion Matrix ===");
        for (int i = 0; i < cumulativeConfusionMatrix.length; i++) {
            for (int j = 0; j < cumulativeConfusionMatrix[i].length; j++) {
                System.out.print(cumulativeConfusionMatrix[i][j] + "\t");
            }
            System.out.println();
        }

        // Calculate final metrics
        int totalInstances = 0;
        int truePositive = 0;
        int trueNegative = 0;
        int falsePositive = 0;
        int falseNegative = 0;

        for (int i = 0; i < cumulativeConfusionMatrix.length; i++) {
            for (int j = 0; j < cumulativeConfusionMatrix[i].length; j++) {
                totalInstances += cumulativeConfusionMatrix[i][j];
                if (i == j) {
                    if (i == 0) truePositive += cumulativeConfusionMatrix[i][j];
                    else trueNegative += cumulativeConfusionMatrix[i][j];
                } else {
                    if (i == 0 && j == 1) falseNegative += cumulativeConfusionMatrix[i][j];
                    if (i == 1 && j == 0) falsePositive += cumulativeConfusionMatrix[i][j];
                }
            }
        }

        double finalAccuracy = (truePositive + trueNegative) / (double) totalInstances * 100;
        double precision = truePositive / (double) (truePositive + falsePositive);
        double recall = truePositive / (double) (truePositive + falseNegative);
        double f1Score = 2 * (precision * recall) / (precision + recall);

        System.out.println("\n=== Final Metrics ===");
        System.out.printf("Final Accuracy: %.2f%%\n", finalAccuracy);
        System.out.printf("Precision: %.2f\n", precision);
        System.out.printf("Recall: %.2f\n", recall);
        System.out.printf("F1-Score: %.2f\n", f1Score);

        // Train final model on entire dataset
        model.buildClassifier(data);

        // User input loop for classification
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\nEnter the text to classify (or type 'exit' to quit):");
            String userInput = scanner.nextLine();

            if (userInput.equalsIgnoreCase("exit")) {
                break;
            }

            // Preprocess user input (same as training data)
            String[] userTokens = userInput.split("\\s+");
            Map<String, Integer> wordCounts = new HashMap<>();
            for (String token : userTokens) {
                token = token.toLowerCase(); // Convert to lowercase for uniformity
                if (!token.isEmpty()) {
                    wordCounts.put(token, wordCounts.getOrDefault(token, 0) + 1);
                }
            }

            // Create a new instance for the user input with the same attributes as the training data
            ArrayList<Attribute> attributes = new ArrayList<>();

            // Add vocabulary attributes to the list (same as training data)
            for (int i = 0; i < data.numAttributes() - 1; i++) {
                attributes.add(new Attribute(data.attribute(i).name()));
            }

            // Add the class attribute (same as training data)
            ArrayList<String> classValues = new ArrayList<>();
            for (int i = 0; i < data.numClasses(); i++) {
                classValues.add(data.classAttribute().value(i));
            }
            attributes.add(new Attribute("class", classValues));

            // Create a new instance (feature vector) for user input
            Instance instance = new DenseInstance(attributes.size());
            instance.setDataset(data);

            // Populate the feature vector (BoW)
            for (int i = 0; i < attributes.size() - 1; i++) {
                String word = attributes.get(i).name();
                instance.setValue(i, wordCounts.getOrDefault(word, 0));
            }

            // Set the class value to an unknown initially
            instance.setClassMissing();

            // Classify the user input
            double predictedClass = model.classifyInstance(instance);
            String predictedLabel = data.classAttribute().value((int) predictedClass);

            // Print the predicted class label
            System.out.println("Predicted class: " + predictedLabel);
        }

        scanner.close();
    }
}