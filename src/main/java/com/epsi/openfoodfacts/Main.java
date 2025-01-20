package com.epsi.openfoodfacts;

import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.storage.StorageLevel;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        // Initialisation de la SparkSession
        SparkSession sparkSession = SparkSession.builder()
                .appName("OpenFoodFacts ETL")
                .master("local[*]") // Utilise tous les cœurs disponibles localement
                .getOrCreate();

        try {
            // Étape 1 : Extraction des données brutes depuis le fichier CSV
            System.out.println("Étape 1 : Extraction des données depuis le fichier CSV...");
            Dataset<Row> rawCsvData = Extractor.extractFromCSV(
                    sparkSession,
                    Config.DATA_FILE_PRODUCTS,
                    "\\t" // Fichier délimité par des tabulations
            ).repartition(10); // Répartition des partitions pour équilibrer la charge

            // Étape 2 : Transformation et validation des données
            System.out.println("Étape 2 : Transformation et validation des données...");
            Dataset<Row> transformedData = Transformer.transformData(rawCsvData, sparkSession).persist(StorageLevel.MEMORY_AND_DISK());

            // Affichage d'un échantillon des données transformées
            System.out.println("Échantillon des données transformées :");
            transformedData.show(10, false);

            // Étape 3 : Extraction des données depuis la base de données
            System.out.println("Étape 3 : Extraction des données depuis la base de données...");
            Dataset<Row> usersDataset = Extractor.extractFromDatabase(
                    sparkSession,
                    Config.DB_HOST,
                    Config.DB_USER,
                    Config.DB_PASSWORD,
                    "users"
            ).repartition(5);

            Dataset<Row> regimesDataset = Extractor.extractFromDatabase(
                    sparkSession,
                    Config.DB_HOST,
                    Config.DB_USER,
                    Config.DB_PASSWORD,
                    "regimes"
            ).repartition(5);

            // Mise en cache des jeux de données pour éviter les recalculs
            usersDataset.persist(StorageLevel.MEMORY_AND_DISK());
            regimesDataset.persist(StorageLevel.MEMORY_AND_DISK());

            // Comptage des données pour validation
            System.out.println("Nombre d'utilisateurs : " + usersDataset.count());
            System.out.println("Nombre de régimes : " + regimesDataset.count());
            System.out.println("Nombre de produits transformés : " + transformedData.count());

            // Étape 4 : Extraction de 5 produits pour inspection
            System.out.println("Étape 4 : Extraction de 5 produits pour inspection...");
            List<Row> productList = transformedData.limit(5).collectAsList();
            for (Row product : productList) {
                System.out.println("Nom du produit : " + product.getAs("product_name"));
                System.out.println("Catégories : " + product.getAs("categories"));
                System.out.println("Marques : " + product.getAs("brands"));
                System.out.println("Énergie (kcal/100g) : " + product.getAs("energy-kcal_100g"));
                System.out.println("-------------------------------");
            }

            // Étape 5 : Génération d'un menu personnalisé pour un utilisateur
            int userIdToTest = 1; // ID d'utilisateur à tester
            System.out.println("Étape 5 : Génération d'un menu pour l'utilisateur ID : " + userIdToTest);
            Dataset<Row> userMenu = Generator.generateMenu(
                    transformedData,
                    usersDataset,
                    regimesDataset,
                    userIdToTest
            );

            // Affichage du menu généré
            System.out.println("Menu généré pour l'utilisateur ID : " + userIdToTest);
            userMenu.show();

        } catch (Exception e) {
            // Gestion des erreurs
            e.printStackTrace();
            System.err.println("Une erreur est survenue pendant l'exécution du pipeline ETL : " + e.getMessage());
        } finally {
            // Fermeture de la SparkSession
            sparkSession.stop();
            System.out.println("SparkSession arrêtée.");
        }
    }
}
