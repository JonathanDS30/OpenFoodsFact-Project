package com.epsi.openfoodfacts;

import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.storage.StorageLevel;

import java.util.Map;

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

            // Step 3: Load additional datasets for allergies and users
            System.out.println("Loading additional datasets (allergies and users)...");
            Dataset<Row> allergiesData = Extractor.extractFromDatabase(
                    sparkSession,
                    Config.DB_HOST,
                    Config.DB_USER,
                    Config.DB_PASSWORD,
                    "allergies"
            );

            Dataset<Row> usersData = Extractor.extractFromDatabase(
                    sparkSession,
                    Config.DB_HOST,
                    Config.DB_USER,
                    Config.DB_PASSWORD,
                    "users"
            );

            // Step 4: Generate weekly menus for all users
            System.out.println("Generating weekly menus for all users...");
            Map<String, Dataset<Row>> generatedData = Generator.generateWeeklyMenusForAllUsers(
                    transformedData,
                    sparkSession
            );

            // Step 5: Load generated data into the database
            System.out.println("Loading data into the database...");
            Loader.loadToDatabase(generatedData.get("menus"), Config.DB_HOST, Config.DB_USER, Config.DB_PASSWORD, "menus");
            Loader.loadToDatabase(generatedData.get("products"), Config.DB_HOST, Config.DB_USER, Config.DB_PASSWORD, "products");
            Loader.loadToDatabase(generatedData.get("menu_days"), Config.DB_HOST, Config.DB_USER, Config.DB_PASSWORD, "menu_days");

        } catch (Exception e) {
            System.err.println("An error occurred during execution: " + e.getMessage());
            e.printStackTrace();
        } finally {
            sparkSession.stop();
            System.out.println("SparkSession stopped.");
        }
    }
}
