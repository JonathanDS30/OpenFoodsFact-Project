package com.epsi.openfoodfacts;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class SparkMySQLTest {
    public static void main(String[] args) {
        // 1. Créer une session Spark
        SparkSession spark = SparkSession.builder()
                .appName("Spark MySQL ETL Test")
                .master("local[*]") // Utilisation en local pour les tests
                .config("spark.driver.host", "127.0.0.1")
                .getOrCreate();

        System.out.println("Spark Session créée avec succès.");

        // 2. Configuration de la connexion MySQL
        String jdbcUrl = "jdbc:mysql://localhost:3306/openFoodFact";
        String dbUser = "user";
        String dbPassword = "user_password";

        // 3. Lecture des données depuis MySQL
        try {
            // Lecture de la table users
            Dataset<Row> usersDf = spark.read()
                    .format("jdbc")
                    .option("url", jdbcUrl)
                    .option("dbtable", "users")
                    .option("user", dbUser)
                    .option("password", dbPassword)
                    .load();

            System.out.println("Données de la table users :");
            usersDf.show(5, false);

            // Lecture de la table allergies
            Dataset<Row> allergiesDf = spark.read()
                    .format("jdbc")
                    .option("url", jdbcUrl)
                    .option("dbtable", "allergies")
                    .option("user", dbUser)
                    .option("password", dbPassword)
                    .load();

            System.out.println("Données de la table allergies :");
            allergiesDf.show(5, false);

            // Lecture de la table regimes
            Dataset<Row> regimesDf = spark.read()
                    .format("jdbc")
                    .option("url", jdbcUrl)
                    .option("dbtable", "regimes")
                    .option("user", dbUser)
                    .option("password", dbPassword)
                    .load();

            System.out.println("Données de la table regimes :");
            regimesDf.show(5, false);

            // Lecture de la table products (optionnel si déjà peuplée)
            Dataset<Row> productsDf = spark.read()
                    .format("jdbc")
                    .option("url", jdbcUrl)
                    .option("dbtable", "products")
                    .option("user", dbUser)
                    .option("password", dbPassword)
                    .load();

            System.out.println("Données de la table products :");
            productsDf.show(5, false);

        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture des données MySQL : " + e.getMessage());
        } finally {
            // 4. Arrêter Spark
            spark.stop();
        }
    }
}
