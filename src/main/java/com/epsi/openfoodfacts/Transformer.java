package com.epsi.openfoodfacts;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import static org.apache.spark.sql.functions.*;

public class Transformer {
    
    /**
     * Transforme les données brutes du CSV en un Dataset propre et validé.
     *
     * @param rawData Le Dataset contenant les données brutes
     * @return Dataset<Row> Les données nettoyées et transformées
     */
    public static Dataset<Row> transformData(Dataset<Row> rawData) {
        // Sélection des colonnes pertinentes
        Dataset<Row> selectedData = rawData.select(
            "code",
            "product_name",
            "brands",
            "categories",
            "countries_tags",
            "pnns_groups_1",
            "pnns_groups_2",
            "food_groups",
            "ingredients_text",
            "allergens",
            "nutriscore_score",
            "nutriscore_grade",
            "nova_group",
            "energy-kcal_100g",
            "fat_100g",
            "saturated-fat_100g",
            "carbohydrates_100g",
            "sugars_100g",
            "fiber_100g",
            "proteins_100g",
            "salt_100g",
            "sodium_100g"
        );

        // Nettoyage des valeurs nulles ou inconnues
        for (String column : selectedData.columns()) {
            selectedData = selectedData.withColumn(column,
                when(col(column).equalTo("Unknown")
                    .or(col(column).equalTo(""))
                    .or(col(column).isNull()), null)
                .otherwise(col(column)));
        }

        // Suppression des lignes avec valeurs nulles
        selectedData = selectedData.na().drop();

        // Validation des données
        Dataset<Row> validatedData = selectedData.filter(
            "`code` IS NOT NULL AND " +
            "`product_name` IS NOT NULL AND " +
            "`categories` IS NOT NULL AND " +
            "`countries_tags` IS NOT NULL AND " +
            "`pnns_groups_1` RLIKE '^[^0-9]*$' AND " +  // Pas de chiffres
            "`pnns_groups_2` RLIKE '^[^0-9]*$' AND " +  // Pas de chiffres
            "`nutriscore_score` BETWEEN -15 AND 40 AND " +
            "`nutriscore_grade` IN ('a', 'b', 'c', 'd', 'e') AND " +
            "`nova_group` BETWEEN 1 AND 4 AND " +
            "`energy-kcal_100g` BETWEEN 0 AND 1000 AND " +
            "`fat_100g` BETWEEN 0 AND 100 AND " +
            "`saturated-fat_100g` BETWEEN 0 AND 100 AND " +
            "`carbohydrates_100g` BETWEEN 0 AND 100 AND " +
            "`sugars_100g` BETWEEN 0 AND 100 AND " +
            "`fiber_100g` BETWEEN 0 AND 100 AND " +
            "`proteins_100g` BETWEEN 0 AND 100 AND " +
            "`salt_100g` BETWEEN 0 AND 100 AND " +
            "`sodium_100g` BETWEEN 0 AND 100"  // Supprimé le AND final
        );

        // Arrondi des valeurs numériques
        String[] numericColumns = {
            "energy-kcal_100g",
            "fat_100g",
            "saturated-fat_100g",
            "carbohydrates_100g",
            "sugars_100g",
            "fiber_100g",
            "proteins_100g",
            "salt_100g",
            "sodium_100g"
        };

        for (String column : numericColumns) {
            validatedData = validatedData.withColumn(column, round(col(column), 2));
        }

        return validatedData;
    }

    /**
     * Charge les données depuis un CSV
     *
     * @param sparkSession La session Spark active
     * @return Dataset<Row> Les données brutes du CSV
     */
    public static Dataset<Row> loadAndTransform(SparkSession sparkSession) {
        // Chargement du CSV
        Dataset<Row> rawData = sparkSession.read()
            .format("csv")
            .option("header", "true")
            .option("encoding", "UTF-8")
            .option("multiline", "true")
            .option("quote", "\"")
            .option("escape", "\"")
            .option("delimiter", "\t")
            .load(Config.DATA_FILE_PRODUCTS);

        // Application des transformations
        return transformData(rawData);
    }

    /**
     * Charge le CSV, transforme les données et les charge dans MySQL.
     *
     * @param sparkSession La session Spark active
     * @param filePath Le chemin vers le fichier CSV
     */
    public static void loadTransformAndSave(SparkSession sparkSession) {
        // Chargement et transformation des données
        Dataset<Row> transformedData = loadAndTransform(sparkSession);
    	//Show 20 top results
		transformedData.show(20,false);
        
        
        
        // Chargement direct dans MySQL
       // Loader.loadToDatabase(
            //transformedData,
            //Config.DB_HOST,
            //Config.DB_USER,
            //Config.DB_PASSWORD,
            //"products"  // nom de la table
        //);
    }
}