package com.epsi.openfoodfacts;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.sql.RowFactory;

import java.util.Random;
import java.util.Collections;

public class Main {

    public static void main(String[] args) {
        // Initialize SparkSession
        SparkSession sparkSession = SparkSession.builder()
                .appName("OpenFoodFacts ETL Test")
                .master("local[*]") // Use all available CPU cores
                .getOrCreate();

        try {
            // Load the CSV file into a Dataset
            System.out.println("Loading CSV data...");
            Dataset<Row> csvData = Extractor.extractFromCSV(
                    sparkSession,
                    "data/Sample.csv",
                    "\\t" // Tab-delimited file
            );

            // Select a random row from the Dataset
            System.out.println("Selecting a random product...");
            long totalRows = csvData.count(); // Count the number of rows in the dataset
            int randomIndex = new Random().nextInt((int) totalRows); // Generate a random index
            Row randomProduct = csvData.collectAsList().get(randomIndex); // Get the random row as a Spark Row

            // Extract relevant columns for insertion into the "products" table
            String productName = randomProduct.getAs("product_name");
            String categoriesEn = randomProduct.getAs("categories_en");
            String brands = randomProduct.getAs("brands");
            String originsEn = randomProduct.getAs("origins_en");
            Float energyKcal100g = getFloatValue(randomProduct, "energy-kcal_100g");
            Float fat100g = getFloatValue(randomProduct, "fat_100g");
            Float saturatedFat100g = getFloatValue(randomProduct, "saturated-fat_100g");
            Float carbohydrates100g = getFloatValue(randomProduct, "carbohydrates_100g");
            Float sugars100g = getFloatValue(randomProduct, "sugars_100g");
            Float fiber100g = getFloatValue(randomProduct, "fiber_100g");
            Float proteins100g = getFloatValue(randomProduct, "proteins_100g");
            Float salt100g = getFloatValue(randomProduct, "salt_100g");
            Float sodium100g = getFloatValue(randomProduct, "sodium_100g");
            String allergens = randomProduct.getAs("allergens");
            Integer nutriscoreScore = getIntegerValue(randomProduct, "nutriscore_score");
            Integer novaGroup = getIntegerValue(randomProduct, "nova_group");

            // Build a Spark DataFrame for insertion
            StructType schema = DataTypes.createStructType(new StructField[]{
                    DataTypes.createStructField("product_name", DataTypes.StringType, true),
                    DataTypes.createStructField("categories_en", DataTypes.StringType, true),
                    DataTypes.createStructField("brands", DataTypes.StringType, true),
                    DataTypes.createStructField("origins_en", DataTypes.StringType, true),
                    DataTypes.createStructField("energy-kcal_100g", DataTypes.FloatType, true),
                    DataTypes.createStructField("fat_100g", DataTypes.FloatType, true),
                    DataTypes.createStructField("saturated-fat_100g", DataTypes.FloatType, true),
                    DataTypes.createStructField("carbohydrates_100g", DataTypes.FloatType, true),
                    DataTypes.createStructField("sugars_100g", DataTypes.FloatType, true),
                    DataTypes.createStructField("fiber_100g", DataTypes.FloatType, true),
                    DataTypes.createStructField("proteins_100g", DataTypes.FloatType, true),
                    DataTypes.createStructField("salt_100g", DataTypes.FloatType, true),
                    DataTypes.createStructField("sodium_100g", DataTypes.FloatType, true),
                    DataTypes.createStructField("allergens", DataTypes.StringType, true),
                    DataTypes.createStructField("nutriscore_score", DataTypes.IntegerType, true),
                    DataTypes.createStructField("nova_group", DataTypes.IntegerType, true),
            });

            Row newRow = RowFactory.create(
                    productName,
                    categoriesEn,
                    brands,
                    originsEn,
                    energyKcal100g,
                    fat100g,
                    saturatedFat100g,
                    carbohydrates100g,
                    sugars100g,
                    fiber100g,
                    proteins100g,
                    salt100g,
                    sodium100g,
                    allergens,
                    nutriscoreScore,
                    novaGroup
            );

            Dataset<Row> productToInsert = sparkSession.createDataFrame(
                    Collections.singletonList(newRow), // Single row to insert
                    schema // Schema for the DataFrame
            );

            // Insert the random product into the database
            System.out.println("Inserting random product into the database...");
            Loader.loadToDatabase(
                    productToInsert,
                    Config.DB_HOST,
                    Config.DB_USER,
                    Config.DB_PASSWORD,
                    "products" // Target table name
            );
            System.out.println("Random product successfully inserted into the 'products' table!");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("An error occurred while testing the ETL pipeline: " + e.getMessage());
        } finally {
            // Stop SparkSession
            sparkSession.stop();
            System.out.println("SparkSession stopped.");
        }
    }

    /**
     * Helper method to safely extract Float values from a Row.
     */
    private static Float getFloatValue(Row row, String columnName) {
        try {
            return row.getAs(columnName) != null ? Float.valueOf(row.getAs(columnName).toString()) : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Helper method to safely extract Integer values from a Row.
     */
    private static Integer getIntegerValue(Row row, String columnName) {
        try {
            return row.getAs(columnName) != null ? Integer.valueOf(row.getAs(columnName).toString()) : null;
        } catch (Exception e) {
            return null;
        }
    }
}
