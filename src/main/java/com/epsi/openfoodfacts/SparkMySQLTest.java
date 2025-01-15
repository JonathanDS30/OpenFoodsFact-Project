package com.epsi.openfoodfacts;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class SparkMySQLTest {

    public static void main(String[] args) {
        // 1. Créer une session Spark
        SparkSession spark = SparkSession.builder()
                .appName("Spark MySQL ETL Test")
                .master("local[*]") // Utilisation de toutes les ressources locales
                .config("spark.driver.host", "localhost") // Configuration pour le driver Spark
                .getOrCreate();

        System.out.println("Spark Session créée avec succès.");

        // 2. Charger les données depuis MySQL
        String jdbcUrl = "jdbc:mysql://localhost:3306/openFoodFact";
        String dbUser = "user";
        String dbPassword = "user_password";

        try {
            // Charger la table "users"
            Dataset<Row> usersDf = spark.read()
                    .format("jdbc")
                    .option("url", jdbcUrl)
                    .option("dbtable", "users")
                    .option("user", dbUser)
                    .option("password", dbPassword)
                    .load();
            System.out.println("Données de la table 'users' :");
            usersDf.show();

            // Charger la table "allergies"
            Dataset<Row> allergiesDf = spark.read()
                    .format("jdbc")
                    .option("url", jdbcUrl)
                    .option("dbtable", "allergies")
                    .option("user", dbUser)
                    .option("password", dbPassword)
                    .load();
            System.out.println("Données de la table 'allergies' :");
            allergiesDf.show();

            // Charger la table "regimes"
            Dataset<Row> regimesDf = spark.read()
                    .format("jdbc")
                    .option("url", jdbcUrl)
                    .option("dbtable", "regimes")
                    .option("user", dbUser)
                    .option("password", dbPassword)
                    .load();
            System.out.println("Données de la table 'regimes' :");
            regimesDf.show();

            // Charger la table "countries"
            Dataset<Row> countriesDf = spark.read()
                    .format("jdbc")
                    .option("url", jdbcUrl)
                    .option("dbtable", "countries")
                    .option("user", dbUser)
                    .option("password", dbPassword)
                    .load();
            System.out.println("Données de la table 'countries' :");
            countriesDf.show();

        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture des données : " + e.getMessage());
        } finally {
            // 4. Arrêter Spark
            spark.stop();
            System.out.println("Spark Session arrêtée.");
        }
    }
}
