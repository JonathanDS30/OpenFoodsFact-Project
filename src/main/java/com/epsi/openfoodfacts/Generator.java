package com.epsi.openfoodfacts;

import java.sql.*;
import java.util.*;

/**
 * This utility class provides methods to generate a balanced menu for users
 * by querying data from a relational database.
 *
 * @param dbHost The JDBC URL of the database server, including the protocol and port.
 * @param dbUser The username for authenticating with the database.
 * @param dbPassword The password associated with the given username.
 * @param usersTable The name of the table containing user data.
 * @param regimesTable The name of the table containing regime data.
 * @param allergiesTable The name of the table containing allergy data.
 * @param productsTable The name of the table containing product data.
 */
public class Generator {

    /**
     * Generates a menu for a given user based on their dietary and allergy constraints.
     *
     * @param conn   A connection to the database.
     * @param userId The ID of the user for whom the menu is generated.
     * @return A list of products that fit the user's constraints.
     * @throws SQLException If a database access error occurs.
     */
    public static List<Map<String, Object>> generateMenu(Connection conn, int userId) throws SQLException {
        // Query user information
        String userQuery = "SELECT diet_id, allergy_id FROM users WHERE id = ?";
        PreparedStatement userStmt = conn.prepareStatement(userQuery);
        userStmt.setInt(1, userId);
        ResultSet userRs = userStmt.executeQuery();

        if (!userRs.next()) {
            throw new IllegalArgumentException("User with id " + userId + " not found.");
        }
        int dietId = userRs.getInt("diet_id");
        int allergyId = userRs.getInt("allergy_id");

        // Query diet constraints
        String dietQuery = "SELECT * FROM regimes WHERE regime_id = ?";
        PreparedStatement dietStmt = conn.prepareStatement(dietQuery);
        dietStmt.setInt(1, dietId);
        ResultSet dietRs = dietStmt.executeQuery();

        if (!dietRs.next()) {
            throw new IllegalArgumentException("Diet with id " + dietId + " not found.");
        }
        float caloriesMax = dietRs.getFloat("calories_max");
        float fatMax = dietRs.getFloat("fat_max");
        float carbsMax = dietRs.getFloat("carbs_max");
        float fiberMax = dietRs.getFloat("fiber_max");
        float proteinsMax = dietRs.getFloat("proteins_max");

        // Query allergies
        String allergyQuery = "SELECT name FROM allergies WHERE id = ?";
        PreparedStatement allergyStmt = conn.prepareStatement(allergyQuery);
        allergyStmt.setInt(1, allergyId);
        ResultSet allergyRs = allergyStmt.executeQuery();

        List<String> userAllergies = new ArrayList<>();
        if (allergyRs.next()) {
            String allergyNames = allergyRs.getString("name");
            if (allergyNames != null) {
                userAllergies = Arrays.asList(allergyNames.split(","));
            }
        }

        // Query products
        String productsQuery = "SELECT * FROM products";
        Statement productsStmt = conn.createStatement();
        ResultSet productsRs = productsStmt.executeQuery(productsQuery);

        List<Map<String, Object>> products = new ArrayList<>();
        while (productsRs.next()) {
            String productName = safeGetString(productsRs, "product_name");
            float calories = safeGetFloat(productsRs, "energy-kcal_100g");
            float fat = safeGetFloat(productsRs, "fat_100g");
            float carbs = safeGetFloat(productsRs, "carbohydrates_100g");
            float fiber = safeGetFloat(productsRs, "fiber_100g");
            float proteins = safeGetFloat(productsRs, "proteins_100g");
            String allergens = safeGetString(productsRs, "allergens");

            if (userAllergies.stream().noneMatch(allergens::contains)) {
                Map<String, Object> product = new HashMap<>();
                product.put("name", productName);
                product.put("calories", calories);
                product.put("fat", fat);
                product.put("carbs", carbs);
                product.put("fiber", fiber);
                product.put("proteins", proteins);
                products.add(product);
            }
        }

        List<Map<String, Object>> menu = new ArrayList<>();
        float totalCalories = 0;
        float totalFat = 0;
        float totalCarbs = 0;
        float totalFiber = 0;
        float totalProteins = 0;

        for (Map<String, Object> product : products) {
            if (totalCalories + (float) product.get("calories") <= caloriesMax
                    && totalFat + (float) product.get("fat") <= fatMax
                    && totalCarbs + (float) product.get("carbs") <= carbsMax
                    && totalFiber + (float) product.get("fiber") <= fiberMax
                    && totalProteins + (float) product.get("proteins") <= proteinsMax) {
                menu.add(product);
                totalCalories += (float) product.get("calories");
                totalFat += (float) product.get("fat");
                totalCarbs += (float) product.get("carbs");
                totalFiber += (float) product.get("fiber");
                totalProteins += (float) product.get("proteins");
            }
        }

        return menu;
    }

    /**
     * Safely retrieves a string value from a ResultSet, returning an empty string if the value is null.
     *
     * @param rs     The ResultSet object.
     * @param column The column name.
     * @return The string value or an empty string if null.
     * @throws SQLException If a database access error occurs.
     */
    private static String safeGetString(ResultSet rs, String column) throws SQLException {
        String value = rs.getString(column);
        return value == null ? "" : value;
    }

    /**
     * Safely retrieves a float value from a ResultSet, returning 0 if the value is null.
     *
     * @param rs     The ResultSet object.
     * @param column The column name.
     * @return The float value or 0 if null.
     * @throws SQLException If a database access error occurs.
     */
    private static float safeGetFloat(ResultSet rs, String column) throws SQLException {
        float value = rs.getFloat(column);
        return rs.wasNull() ? 0 : value;
    }
}
