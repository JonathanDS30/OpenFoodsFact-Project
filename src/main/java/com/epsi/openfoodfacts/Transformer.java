package com.epsi.openfoodfacts;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import static org.apache.spark.sql.functions.*;

public class Transformer {

    /**
     * Transforms and cleans raw data by applying filters and validations.
     *
     * @param rawData      The raw Dataset to transform.
     * @param sparkSession The Spark session used for transformations.
     * @return A transformed and cleaned Dataset.
     */
    public static Dataset<Row> transformData(Dataset<Row> rawData, SparkSession sparkSession) {
        // Selecting and converting necessary columns
        Dataset<Row> selectedData = rawData.select(
                col("code").cast("int"),
                col("product_name").cast("string"),
                col("brands").cast("string"),
                col("categories").cast("string"),
                col("countries_tags").cast("string"),
                col("allergens").cast("string"),
                col("nutriscore_score").cast("float"),
                col("nutriscore_grade").cast("string"),
                col("nova_group").cast("int"),
                col("energy-kcal_100g").cast("float"),
                col("fat_100g").cast("float"),
                col("saturated-fat_100g").cast("float"),
                col("carbohydrates_100g").cast("float"),
                col("sugars_100g").cast("float"),
                col("fiber_100g").cast("float"),
                col("proteins_100g").cast("float"),
                col("salt_100g").cast("float"),
                col("sodium_100g").cast("float"),
                col("pnns_groups_1").cast("string"),
                col("pnns_groups_2").cast("string"),
                col("origins").cast("string")
        );

        // Removing rows with null values in essential columns
        String[] essentialColumns = {
        	    "code", "product_name", "categories", "nutriscore_score", "nutriscore_grade",
        	    "nova_group", "energy-kcal_100g", "fat_100g", "saturated-fat_100g", "carbohydrates_100g",
        	    "sugars_100g", "proteins_100g", "countries_tags"
        };
        Dataset<Row> cleanedData = selectedData.na().drop(essentialColumns);

        // Filtering to keep only products from France
        cleanedData = cleanedData.filter(col("countries_tags").contains("en:france"));

        // Filtering data based on valid value ranges
        cleanedData = cleanedData.filter(
                        col("nutriscore_grade").isin("a", "b", "c", "d", "e")
                        .and(col("nova_group").between(1, 4))
                        .and(col("energy-kcal_100g").between(0, 1000))
                        .and(col("fat_100g").between(0, 100))
                        .and(col("saturated-fat_100g").between(0, 100))
                        .and(col("carbohydrates_100g").between(0, 100))
                        .and(col("sugars_100g").between(0, 100))
                        .and(col("fiber_100g").between(0, 100))
                        .and(col("proteins_100g").between(0, 100))
                        .and(col("salt_100g").between(0, 100))
                        .and(col("sodium_100g").between(0, 100))
                        .and(col("nutriscore_score").between(-15, 40))
        );

        // Removing duplicates
        cleanedData = cleanedData.dropDuplicates();

        // Cleaning string columns
        cleanedData = cleanedData.filter(
                col("product_name").rlike("^[\\x00-\\x7F]+$") // ASCII characters only
                        .and(col("categories").rlike("^[\\x00-\\x7F]+$"))
                        .and(col("countries_tags").rlike("^[\\x00-\\x7F]+$"))
        );

        // Repartitioning to balance tasks (depending on data size)
        cleanedData = cleanedData.repartition(10);

        // Rounding numeric columns
        String[] numericColumns = {
                "energy-kcal_100g", "fat_100g", "saturated-fat_100g", "carbohydrates_100g",
                "sugars_100g", "fiber_100g", "proteins_100g", "salt_100g", "sodium_100g"
        };
        for (String column : numericColumns) {
            cleanedData = cleanedData.withColumn(column, round(col(column), 1));
        }

        // Explicitly casting columns to match the final schema
        cleanedData = cleanedData
                .withColumn("code", col("code").cast("int"))
                .withColumn("nutriscore_score", col("nutriscore_score").cast("float"))
                .withColumn("nova_group", col("nova_group").cast("int"));

        return cleanedData;
    }
}
