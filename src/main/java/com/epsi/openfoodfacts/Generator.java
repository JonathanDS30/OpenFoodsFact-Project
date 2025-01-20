package com.epsi.openfoodfacts;

import org.apache.spark.sql.*;
import org.apache.spark.sql.types.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.spark.sql.functions.*;

public class Generator {

    public static void generateWeeklyMenusForAllUsers(
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
        for (Row user : usersDataset.collectAsList()) {
            try {
                int userId = user.getAs("id");
                String userName = user.getAs("name");

                int menuId = menuIdCounter.getAndIncrement();
                allMenus.add(RowFactory.create(menuId, userId));

                System.out.println("Generating weekly menu for user: " + userName + " (ID: " + userId + ", Menu ID: " + menuId + ")");

                List<Row> userMenuDays = generateWeeklyMenu(cleanedData, userId, menuId, sparkSession);
                allMenuDays.addAll(userMenuDays);

            } catch (Exception e) {
                System.err.println("Failed to generate menu for user ID: " + user.getAs("id"));
                e.printStackTrace();
            }
        }

        Dataset<Row> menusDataset = sparkSession.createDataFrame(allMenus, getMenusSchema());
        menusDataset.write()
                .option("header", true)
                .csv("output/menus.csv");

        Dataset<Row> menuDaysDataset = sparkSession.createDataFrame(allMenuDays, getMenuDaysSchema());
        menuDaysDataset.write()
                .option("header", true)
                .csv("output/menu_days.csv");
    }

    public static List<Row> generateWeeklyMenu(
            Dataset<Row> cleanedData,
            int userId,
            int menuId,
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
            Set<Integer> usedProductIds = new HashSet<>();

            // Breakfast (1 product)
            Integer breakfast = selectUniqueProduct(filteredProducts, usedProductIds, "Breakfast", day, menuId);

            // Lunch (2 products)
            Integer lunch1 = selectUniqueProduct(filteredProducts, usedProductIds, "Lunch 1", day, menuId);
            Integer lunch2 = selectUniqueProduct(filteredProducts, usedProductIds, "Lunch 2", day, menuId);

            // Dinner (2 products)
            Integer dinner1 = selectUniqueProduct(filteredProducts, usedProductIds, "Dinner 1", day, menuId);
            Integer dinner2 = selectUniqueProduct(filteredProducts, usedProductIds, "Dinner 2", day, menuId);

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
            Set<Integer> usedProductIds,
            String mealType,
            int day,
            int menuId
    ) {
        Row productRow = filteredProducts
                .filter(not(col("code").isin((Object[]) usedProductIds.toArray())))
                .sample(false, 0.1)
                .limit(1)
                .first();

        if (productRow != null) {
            Integer productId = productRow.getAs("code");
            String productName = productRow.getAs("product_name");

            // Log the product selection
            System.out.printf("Menu ID: %d | Day: %d | Meal: %s | Product ID: %d | Product Name: %s%n",
                    menuId, day, mealType, productId, productName);

            usedProductIds.add(productId);
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
}
