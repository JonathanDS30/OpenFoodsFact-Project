package com.epsi.openfoodfacts;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import static org.apache.spark.sql.functions.*;

public class Generator {

    /**
     * Génère un menu pour un utilisateur en fonction de ses contraintes de régime et d'allergies.
     *
     * @param productsDataset Dataset des produits nettoyés.
     * @param usersDataset    Dataset des utilisateurs.
     * @param regimesDataset  Dataset des régimes.
     * @param userId          ID de l'utilisateur pour lequel générer un menu.
     * @return Dataset contenant les produits sélectionnés pour le menu.
     */
    public static Dataset<Row> generateMenu(
            Dataset<Row> productsDataset,
            Dataset<Row> usersDataset,
            Dataset<Row> regimesDataset,
            int userId
    ) {
        // Récupération des informations de l'utilisateur
        Dataset<Row> user = usersDataset.filter(col("id").equalTo(userId));
        if (user.isEmpty()) {
            throw new IllegalArgumentException("Utilisateur avec ID " + userId + " introuvable.");
        }

        // Récupération des contraintes alimentaires de l'utilisateur
        int dietId = user.select("diet_id").head().getInt(0);
        int allergies = user.select("allergy_id").head().getInt(0);

        // Récupération des contraintes nutritionnelles
        Dataset<Row> diet = regimesDataset.filter(col("regime_id").equalTo(dietId));
        if (diet.isEmpty()) {
            throw new IllegalArgumentException("Régime avec ID " + dietId + " introuvable.");
        }

        Row dietConstraints = diet.first();

        // Filtrer les produits selon les contraintes
        Dataset<Row> filteredProducts = productsDataset
                .filter(col("energy-kcal_100g").leq(dietConstraints.getAs("calories_max")))
                .filter(col("fat_100g").leq(dietConstraints.getAs("fat_max")))
                .filter(col("carbohydrates_100g").leq(dietConstraints.getAs("carbs_max")))
                .filter(col("fiber_100g").leq(dietConstraints.getAs("fiber_max")))
                .filter(col("proteins_100g").leq(dietConstraints.getAs("proteins_max")))
                .filter(not(col("allergens").contains(allergies)));

        return filteredProducts;
    }
}
