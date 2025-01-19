package com.epsi.openfoodfacts;

import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        // Initialize SparkSession
        SparkSession sparkSession = SparkSession.builder()
                .appName("OpenFoodFacts ETL Test")
                .master("local[*]") // Use all available CPU cores
                .getOrCreate();

        try {
            // Extract data from the database
            System.out.println("Extracting data from the database...");

            // Extract users table
            Dataset<Row> usersDataset = Extractor.extractFromDatabase(
                    sparkSession,
                    Config.DB_HOST,
                    Config.DB_USER,
                    Config.DB_PASSWORD,
                    "users"
            );

            // Extract regimes table
            Dataset<Row> regimesDataset = Extractor.extractFromDatabase(
                    sparkSession,
                    Config.DB_HOST,
                    Config.DB_USER,
                    Config.DB_PASSWORD,
                    "regimes"
            );

            // Extract allergies table
            Dataset<Row> allergiesDataset = Extractor.extractFromDatabase(
                    sparkSession,
                    Config.DB_HOST,
                    Config.DB_USER,
                    Config.DB_PASSWORD,
                    "allergies"
            );

            // Extract products table
            Dataset<Row> productsDataset = Extractor.extractFromDatabase(
                    sparkSession,
                    Config.DB_HOST,
                    Config.DB_USER,
                    Config.DB_PASSWORD,
                    "products"
            );

            // Log counts for validation
            System.out.println("Users count: " + usersDataset.count());
            System.out.println("Regimes count: " + regimesDataset.count());
            System.out.println("Allergies count: " + allergiesDataset.count());
            System.out.println("Products count: " + productsDataset.count());

            // Example: Generate menu for a user
            System.out.println("Connecting to the database for menu generation...");
            Connection connection = DriverManager.getConnection(
                    Config.DB_HOST,
                    Config.DB_USER,
                    Config.DB_PASSWORD
            );
            System.out.println("Database connected successfully.");

            // Test user ID
            int userIdToTest = 1; // Replace with an actual user ID from your database

            // Generate menu for the test user
            System.out.println("Generating menu for user ID: " + userIdToTest);
            List<Map<String, Object>> generatedMenu = Generator.generateMenu(connection, userIdToTest);

            // Display the generated menu
            System.out.println("Menu generated for user ID: " + userIdToTest);
            for (Map<String, Object> product : generatedMenu) {
                System.out.println("Product Name: " + product.get("name"));
                System.out.println("Calories: " + product.get("calories"));
                System.out.println("Fat: " + product.get("fat"));
                System.out.println("Carbohydrates: " + product.get("carbs"));
                System.out.println("Fiber: " + product.get("fiber"));
                System.out.println("Proteins: " + product.get("proteins"));
                System.out.println("-------------------------------");
            }

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
