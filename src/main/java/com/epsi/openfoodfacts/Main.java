package com.epsi.openfoodfacts;

import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.storage.StorageLevel;

public class Main {

    public static void main(String[] args) {
        // Initialize SparkSession
        SparkSession sparkSession = SparkSession.builder()
                .appName("OpenFoodFacts ETL - Generate Menus for All Users")
                .master("local[*]")
                .getOrCreate();

        try {
            // Step 1: Extract raw data from CSV
            System.out.println("Extracting data from CSV...");
            Dataset<Row> rawCsvData = Extractor.extractFromCSV(
                    sparkSession,
                    Config.DATA_FILE_PRODUCTS,
                    "\\t" // Tab-delimited
            ).repartition(10);

            // Step 2: Transform and validate data
            System.out.println("Transforming data...");
            Dataset<Row> transformedData = Transformer.transformData(rawCsvData, sparkSession)
                    .persist(StorageLevel.MEMORY_AND_DISK());

            // Step 3: Generate weekly menus for all users
            System.out.println("Generating weekly menus for all users...");
            Generator.generateWeeklyMenusForAllUsers(transformedData, sparkSession);

        } catch (Exception e) {
            System.err.println("An error occurred during execution: " + e.getMessage());
            e.printStackTrace();
        } finally {
            sparkSession.stop();
            System.out.println("SparkSession stopped.");
        }
    }
}
