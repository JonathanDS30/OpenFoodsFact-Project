package com.epsi.openfoodfacts;

public class Config {
	// DF Config (CSV)
    // Path to the products data file "en.openfoodfacts.org.products.csv"
    public static final String DATA_FILE_PRODUCTS = "data/en.openfoodfacts.org.products.csv";

    // DataBase Config
    // Database host (URL)
    public static final String DB_HOST = "jdbc:mysql://localhost:3306/openfoodfact";

    // Database user
    public static final String DB_USER = "root";

    // Database password
    public static final String DB_PASSWORD = "root_password";
}
