package com.epsi.openfoodfacts;

import org.apache.spark.sql.*;
import org.apache.spark.sql.types.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.spark.sql.functions.*;

public class Generator {

    /**
     * Generates weekly menus for all users by processing cleaned data and user-specific constraints.
     *
     * @param cleanedData   Cleaned Dataset containing product information.
     * @param sparkSession  The Spark session used for processing.
     * @return A map containing three datasets: menus, menu_days, and unique products.
     */
    public static Map<String, Dataset<Row>> generateWeeklyMenusForAllUsers(
            Dataset<Row> cleanedData,
            SparkSession sparkSession
    ) {
        // Load the users dataset from the database
    	Dataset<Row> usersDataset = Extractor.extractFromDatabase(
    	        sparkSession,
    	        Config.DB_HOST,
    	        Config.DB_USER,
    	        Config.DB_PASSWORD,
    	        "users"
    	);

        AtomicInteger menuIdCounter = new AtomicInteger(1); // Counter for generating unique menu IDs

        // Initialize collections for storing menus and menu days
        List<Row> allMenuDays = new ArrayList<>();
        List<Row> allMenus = new ArrayList<>();
        Set<Integer> usedProductIdsGlobal = new HashSet<>();
        List<Row> uniqueProducts = new ArrayList<>();

        // Loop through each user to generate their menu
        for (Row user : usersDataset.collectAsList()) {
            try {
                int userId = user.getAs("id");

                // Generate a unique menu ID for the user
                int menuId = menuIdCounter.getAndIncrement();
                allMenus.add(RowFactory.create(menuId, userId));

                // Generate the weekly menu for the user
                List<Row> userMenuDays = generateWeeklyMenu(cleanedData, userId, menuId, usedProductIdsGlobal, uniqueProducts, sparkSession);
                allMenuDays.addAll(userMenuDays);

            } catch (Exception e) {
                System.err.println("Failed to generate menu for user ID: " + user.getAs("id"));
                e.printStackTrace();
            }
        }

        // Create datasets for menus, menu days, and unique products
        Dataset<Row> menusDataset = sparkSession.createDataFrame(allMenus, getMenusSchema()).withColumnRenamed("menu_id", "id");
        Dataset<Row> menuDaysDataset = sparkSession.createDataFrame(allMenuDays, getMenuDaysSchema())
                .withColumnRenamed("breakfast", "breakfast_id")
                .withColumnRenamed("lunch_1", "lunch_id_1")
                .withColumnRenamed("lunch_2", "lunch_id_2")
                .withColumnRenamed("dinner_1", "dinner_id_1")
                .withColumnRenamed("dinner_2", "dinner_id_2")
                // Add a unique column combining `menu_id` and `day_of_week`
                .withColumn("id", functions.concat_ws("_", col("menu_id"), col("day_of_week")));
        Dataset<Row> uniqueProductsDataset = sparkSession.createDataFrame(uniqueProducts, getProductsSchema())
                .withColumnRenamed("code", "id")
                .withColumnRenamed("categories", "categories_en");

        // Return the datasets as a map
        Map<String, Dataset<Row>> resultMap = new HashMap<>();
        resultMap.put("menus", menusDataset);
        resultMap.put("menu_days", menuDaysDataset);
        resultMap.put("products", uniqueProductsDataset);

        return resultMap;
    }

    /**
     * Generates a weekly menu for a specific user based on their dietary constraints.
     *
     * @param cleanedData         Cleaned product data.
     * @param userId              The ID of the user.
     * @param menuId              The menu ID for the user.
     * @param usedProductIdsGlobal A global set of used product IDs to avoid duplication.
     * @param uniqueProducts      A list of unique products for the menu.
     * @param sparkSession        The Spark session used for processing.
     * @return A list of rows representing menu days for the user.
     */
    public static List<Row> generateWeeklyMenu(
            Dataset<Row> cleanedData,
            int userId,
            int menuId,
            Set<Integer> usedProductIdsGlobal,
            List<Row> uniqueProducts,
            SparkSession sparkSession
    ) {
        // Load user data from the database
    	Dataset<Row> userDataset = Extractor.extractFromDatabase(
    	        sparkSession,
    	        Config.DB_HOST,
    	        Config.DB_USER,
    	        Config.DB_PASSWORD,
    	        "(SELECT * FROM users WHERE id = " + userId + ") AS user_data"
    	);


        Row user = userDataset.first();
        if (user == null) {
            throw new IllegalArgumentException("User with ID " + userId + " not found!");
        }

        Integer dietId = user.getAs("diet_id");
        Integer allergyId = user.getAs("allergy_id");

        if (dietId == null) {
            throw new IllegalArgumentException("No diet associated with user ID " + userId);
        }

        // Load dietary information from the database
        Dataset<Row> regimesDataset = Extractor.extractFromDatabase(
                sparkSession,
                Config.DB_HOST,
                Config.DB_USER,
                Config.DB_PASSWORD,
                "(SELECT * FROM regimes WHERE regime_id = " + dietId + ") AS regime_data"
        );


        Double maxCalories = regimesDataset.select("calories_max").as(Encoders.DOUBLE()).first();

        // List of allergy IDs for the user
        List<Integer> userAllergyIds = new ArrayList<>();
        if (allergyId != null) {
            userAllergyIds.add(allergyId);
        }

        // Filter products based on allergies and calorie constraints
        Dataset<Row> filteredProducts = cleanedData
                .filter(not(col("allergens").isin((Object[]) userAllergyIds.toArray())))
                .filter(col("energy-kcal_100g").leq(maxCalories));

        List<Row> menuDays = new ArrayList<>();
        int menuDaysIdCounter = 1;

        // Generate menu for each day of the week
        for (int day = 1; day <= 7; day++) {
            Set<Integer> usedProductIdsDaily = new HashSet<>();

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

    /**
     * Selects a unique product from the filtered products Dataset.
     */
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

            // Add product information to the list of unique products
            uniqueProducts.add(RowFactory.create(
            	    productRow.getAs("code"),
            	    productRow.getAs("product_name"),
            	    productRow.getAs("categories"),
            	    productRow.getAs("brands"),
            	    productRow.getAs("countries_tags"),
            	    productRow.getAs("energy-kcal_100g"),
            	    productRow.getAs("fat_100g"),
            	    productRow.getAs("saturated-fat_100g"),  // Ajouté
            	    productRow.getAs("carbohydrates_100g"),
            	    productRow.getAs("sugars_100g"),        // Ajouté
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

        throw new IllegalArgumentException("No unique product available for filtering!");
    }

    // Schema definitions for menus, menu days, and products
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
            DataTypes.createStructField("saturated-fat_100g", DataTypes.FloatType, true), // Ajouté
            DataTypes.createStructField("carbohydrates_100g", DataTypes.FloatType, true),
            DataTypes.createStructField("sugars_100g", DataTypes.FloatType, true),       // Ajouté
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
