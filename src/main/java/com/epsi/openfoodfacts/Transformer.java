package com.epsi.openfoodfacts;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import static org.apache.spark.sql.functions.*;

public class Transformer {

    /**
     * Transforme et nettoie les données brutes en appliquant des filtres et des validations.
     *
     * @param rawData      Le Dataset brut à transformer.
     * @param sparkSession La session Spark utilisée pour les transformations.
     * @return Un Dataset transformé et nettoyé.
     */
    public static Dataset<Row> transformData(Dataset<Row> rawData, SparkSession sparkSession) {
        // Sélection et conversion des colonnes nécessaires
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
                col("carbohydrates_100g").cast("float"),
                col("fiber_100g").cast("float"),
                col("proteins_100g").cast("float"),
                col("salt_100g").cast("float"),
                col("sodium_100g").cast("float")
        );

        // Suppression des lignes avec des valeurs nulles dans les colonnes essentielles
        String[] essentialColumns = {
                "code", "product_name", "categories", "nutriscore_score", "nutriscore_grade",
                "nova_group", "energy-kcal_100g", "fat_100g", "carbohydrates_100g", "proteins_100g"
        };
        Dataset<Row> cleanedData = selectedData.na().drop(essentialColumns);

        // Filtrage pour garder uniquement les produits de France
        cleanedData = cleanedData.filter(col("countries_tags").contains("en:france"));

        // Filtrage des données sur les plages de valeurs valides
        cleanedData = cleanedData.filter(
                col("nutriscore_score").between(-15, 40)
                        .and(col("nutriscore_grade").isin("a", "b", "c", "d", "e"))
                        .and(col("nova_group").between(1, 4))
                        .and(col("energy-kcal_100g").between(0, 1000))
                        .and(col("fat_100g").between(0, 100))
                        .and(col("carbohydrates_100g").between(0, 100))
                        .and(col("fiber_100g").between(0, 100))
                        .and(col("proteins_100g").between(0, 100))
                        .and(col("salt_100g").between(0, 100))
                        .and(col("sodium_100g").between(0, 100))
        );

        // Suppression des doublons
        cleanedData = cleanedData.dropDuplicates();

        // Nettoyage des colonnes contenant des chaînes de caractères
        cleanedData = cleanedData.filter(
                col("product_name").rlike("^[\\x00-\\x7F]+$")
                        .and(col("categories").rlike("^[\\x00-\\x7F]+$"))
                        .and(col("countries_tags").rlike("^[\\x00-\\x7F]+$"))
        );

        // Répartition pour équilibrer les tâches (en fonction de la taille des données)
        cleanedData = cleanedData.repartition(10);

        // Mise en cache pour éviter les recalculs
        cleanedData = cleanedData.persist();

        // Arrondi des colonnes numériques
        String[] numericColumns = {
                "energy-kcal_100g", "fat_100g", "carbohydrates_100g", "fiber_100g",
                "proteins_100g", "salt_100g", "sodium_100g"
        };
        for (String column : numericColumns) {
            cleanedData = cleanedData.withColumn(column, round(col(column), 2));
        }

        return cleanedData;
    }
}
