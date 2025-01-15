package com.epsi.openfoodfacts;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class Main {

    public static void main(String[] args) {
        // Initialize SparkSession
        SparkSession sparkSession = SparkSession.builder()
                .appName("OpenFoodFacts ETL Test")
                .master("local[*]") // Use all available CPU cores
                .getOrCreate();

        try {
            // Test CSV extraction
            System.out.println("Testing CSV extraction...");
            Dataset<Row> csvData = Extractor.extractFromCSV(
                    sparkSession,
                    Config.DATA_FILE_PRODUCTS,
                    "\\t" // Delimiter
            );
            csvData.show(5); // Display the first 5 rows

            // Test Database extraction
            System.out.println("Testing Database extraction...");
            Dataset<Row> dbData = Extractor.extractFromDatabase(
                    sparkSession,
                    Config.DB_HOST,
                    Config.DB_USER,
                    Config.DB_PASSWORD,
                    "products" // Replace with your table name
            );
            dbData.show(5); // Display the first 5 rows
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("An error occurred while testing the ETL pipeline: " + e.getMessage());
        } finally {
            // Stop SparkSession
            sparkSession.stop();
            System.out.println("SparkSession stopped.");
        }
    }
}
