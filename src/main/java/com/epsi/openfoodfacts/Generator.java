package com.epsi.openfoodfacts;

import org.apache.spark.sql.*;
import org.apache.spark.sql.types.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.spark.sql.functions.*;

public class Generator {

    public static Map<String, Dataset<Row>> generateWeeklyMenusForAllUsers(
            Dataset<Row> cleanedData,
            SparkSession sparkSession
    ) {
        Dataset<Row> usersDataset = sparkSession.read()
                .format("jdbc")
                .option("url", Config.DB_HOST)
                .option("dbtable", "users")
                .option("user", Config.DB_USER)
                .option("password", Config.DB_PASSWORD)
                .load();

        AtomicInteger menuIdCounter = new AtomicInteger(1);

        List<Row> allMenuDays = new ArrayList<>();
        List<Row> allMenus = new ArrayList<>();
        Set<Integer> usedProductIdsGlobal = new HashSet<>(); // Conserver tous les IDs produits déjà utilisés
        List<Row> uniqueProducts = new ArrayList<>(); // Liste pour stocker les produits uniques

        for (Row user : usersDataset.collectAsList()) {
            try {
                int userId = user.getAs("id");

                int menuId = menuIdCounter.getAndIncrement();
                allMenus.add(RowFactory.create(menuId, userId));

                List<Row> userMenuDays = generateWeeklyMenu(cleanedData, userId, menuId, usedProductIdsGlobal, uniqueProducts, sparkSession);
                allMenuDays.addAll(userMenuDays);

            } catch (Exception e) {
                System.err.println("Failed to generate menu for user ID: " + user.getAs("id"));
                e.printStackTrace();
            }
        }

        Dataset<Row> menusDataset = sparkSession.createDataFrame(allMenus, getMenusSchema()).withColumnRenamed("menu_id", "id");
        Dataset<Row> menuDaysDataset = sparkSession.createDataFrame(allMenuDays, getMenuDaysSchema())
        	    .withColumnRenamed("breakfast", "breakfast_id")
        	    .withColumnRenamed("lunch_1", "lunch_id_1")
        	    .withColumnRenamed("lunch_2", "lunch_id_2")
        	    .withColumnRenamed("dinner_1", "dinner_id_1")
        	    .withColumnRenamed("dinner_2", "dinner_id_2")
        	    // Ajouter une colonne unique combinant `menu_id` et `day_of_week`
        	    .withColumn("id", functions.concat_ws("_", col("menu_id"), col("day_of_week")));
        Dataset<Row> uniqueProductsDataset = sparkSession.createDataFrame(uniqueProducts, getProductsSchema())    .withColumnRenamed("code", "id")
        	    .withColumnRenamed("categories", "categories_en")
        	    .withColumnRenamed("countries_tags", "origins_en");

        // Retourner les DataFrames sous forme d'une Map
        Map<String, Dataset<Row>> resultMap = new HashMap<>();
        resultMap.put("menus", menusDataset);
        resultMap.put("menu_days", menuDaysDataset);
        resultMap.put("products", uniqueProductsDataset);

        return resultMap;
    }

    public static List<Row> generateWeeklyMenu(
            Dataset<Row> cleanedData,
            int userId,
            int menuId,
            Set<Integer> usedProductIdsGlobal,
            List<Row> uniqueProducts,
            SparkSession sparkSession
    ) {
        Dataset<Row> userDataset = sparkSession.read()
                .format("jdbc")
                .option("url", Config.DB_HOST)
                .option("dbtable", "(SELECT * FROM users WHERE id = " + userId + ") AS user_data")
                .option("user", Config.DB_USER)
                .option("password", Config.DB_PASSWORD)
                .load();

        Row user = userDataset.first();
        if (user == null) {
            throw new IllegalArgumentException("Utilisateur avec l'ID " + userId + " introuvable !");
        }

        Integer dietId = user.getAs("diet_id");
        Integer allergyId = user.getAs("allergy_id");

        if (dietId == null) {
            throw new IllegalArgumentException("Aucun régime associé à l'utilisateur ID " + userId);
        }

        Dataset<Row> regimesDataset = sparkSession.read()
                .format("jdbc")
                .option("url", Config.DB_HOST)
                .option("dbtable", "(SELECT * FROM regimes WHERE regime_id = " + dietId + ") AS regime_data")
                .option("user", Config.DB_USER)
                .option("password", Config.DB_PASSWORD)
                .load();

        Double maxCalories = regimesDataset.select("calories_max").as(Encoders.DOUBLE()).first();

        List<Integer> userAllergyIds = new ArrayList<>();
        if (allergyId != null) {
            userAllergyIds.add(allergyId);
        }

        Dataset<Row> filteredProducts = cleanedData
                .filter(not(col("allergens").isin((Object[]) userAllergyIds.toArray())))
                .filter(col("energy-kcal_100g").leq(maxCalories));

        List<Row> menuDays = new ArrayList<>();
        int menuDaysIdCounter = 1;

        for (int day = 1; day <= 7; day++) {
            Set<Integer> usedProductIdsDaily = new HashSet<>(); // Conserver les produits utilisés pour la journée

            Integer breakfast = selectUniqueProduct(filteredProducts, usedProductIdsDaily, usedProductIdsGlobal, uniqueProducts);
            Integer lunch1 = selectUniqueProduct(filteredProducts, usedProductIdsDaily, usedProductIdsGlobal, uniqueProducts);
            Integer lunch2 = selectUniqueProduct(filteredProducts, usedProductIdsDaily, usedProductIdsGlobal, uniqueProducts);
            Integer dinner1 = selectUniqueProduct(filteredProducts, usedProductIdsDaily, usedProductIdsGlobal, uniqueProducts);
            Integer dinner2 = selectUniqueProduct(filteredProducts, usedProductIdsDaily, usedProductIdsGlobal, uniqueProducts);

            Row menuDay = RowFactory.create(
                    menuDaysIdCounter++,
                    menuId,
                    day,
                    breakfast,
                    lunch1,
                    lunch2,
                    dinner1,
                    dinner2
            );
            menuDays.add(menuDay);
        }

        return menuDays;
    }

    private static Integer selectUniqueProduct(
            Dataset<Row> filteredProducts,
            Set<Integer> usedProductIdsDaily,
            Set<Integer> usedProductIdsGlobal,
            List<Row> uniqueProducts
    ) {
        Row productRow = filteredProducts
                .filter(not(col("code").isin((Object[]) usedProductIdsDaily.toArray())))
                .filter(not(col("code").isin((Object[]) usedProductIdsGlobal.toArray())))
                .sample(false, 0.1)
                .limit(1)
                .first();

        if (productRow != null) {
            Integer productId = productRow.getAs("code");
            usedProductIdsDaily.add(productId);
            usedProductIdsGlobal.add(productId);

            // Ajout des informations du produit à la liste des produits uniques
            uniqueProducts.add(RowFactory.create(
                    productRow.getAs("code"),
                    productRow.getAs("product_name"),
                    productRow.getAs("categories"),
                    productRow.getAs("brands"),
                    productRow.getAs("countries_tags"),
                    productRow.getAs("energy-kcal_100g"),
                    productRow.getAs("fat_100g"),
                    productRow.getAs("carbohydrates_100g"),
                    productRow.getAs("fiber_100g"),
                    productRow.getAs("proteins_100g"),
                    productRow.getAs("salt_100g"),
                    productRow.getAs("sodium_100g"),
                    productRow.getAs("allergens"),
                    productRow.getAs("nutriscore_grade"),
                    productRow.getAs("nova_group")
            ));

            return productId;
        }

        throw new IllegalArgumentException("Aucun produit unique disponible pour le filtrage !");
    }

    private static StructType getMenusSchema() {
        return new StructType(new StructField[]{
                DataTypes.createStructField("menu_id", DataTypes.IntegerType, false),
                DataTypes.createStructField("user_id", DataTypes.IntegerType, false)
        });
    }

    private static StructType getMenuDaysSchema() {
        return new StructType(new StructField[]{
                DataTypes.createStructField("id", DataTypes.IntegerType, false),
                DataTypes.createStructField("menu_id", DataTypes.IntegerType, false),
                DataTypes.createStructField("day_of_week", DataTypes.IntegerType, false),
                DataTypes.createStructField("breakfast", DataTypes.IntegerType, true),
                DataTypes.createStructField("lunch_1", DataTypes.IntegerType, true),
                DataTypes.createStructField("lunch_2", DataTypes.IntegerType, true),
                DataTypes.createStructField("dinner_1", DataTypes.IntegerType, true),
                DataTypes.createStructField("dinner_2", DataTypes.IntegerType, true)
        });
    }

    private static StructType getProductsSchema() {
        return new StructType(new StructField[]{
                DataTypes.createStructField("code", DataTypes.IntegerType, false),
                DataTypes.createStructField("product_name", DataTypes.StringType, true),
                DataTypes.createStructField("categories", DataTypes.StringType, true),
                DataTypes.createStructField("brands", DataTypes.StringType, true),
                DataTypes.createStructField("countries_tags", DataTypes.StringType, true),
                DataTypes.createStructField("energy-kcal_100g", DataTypes.FloatType, true),
                DataTypes.createStructField("fat_100g", DataTypes.FloatType, true),
                DataTypes.createStructField("carbohydrates_100g", DataTypes.FloatType, true),
                DataTypes.createStructField("fiber_100g", DataTypes.FloatType, true),
                DataTypes.createStructField("proteins_100g", DataTypes.FloatType, true),
                DataTypes.createStructField("salt_100g", DataTypes.FloatType, true),
                DataTypes.createStructField("sodium_100g", DataTypes.FloatType, true),
                DataTypes.createStructField("allergens", DataTypes.StringType, true),
                DataTypes.createStructField("nutriscore_grade", DataTypes.StringType, true),
                DataTypes.createStructField("nova_group", DataTypes.IntegerType, true)
        });
    }
}
