package com.epsi.openfoodfacts;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

/**
 * This utility class provides methods to extract data from CSV files and relational databases.
 * It is designed to be modular and reusable in data pipelines.
 */
public class Extractor {

	
    /**
     * Connects to a database and extracts data from a specified table.
     *
     * @param sparkSession The active Spark session to use for querying the database.
     * @param dbHost The JDBC URL of the database server, including the protocol and port.
     * @param dbUser The username for authenticating with the database.
     * @param dbPassword The password associated with the given username.
     * @param dbTable The name of the table (or SQL query) to fetch data from.
     * @return A Dataset<Row> containing the data retrieved from the database table.
     */
    public static Dataset<Row> extractFromDatabase(SparkSession sparkSession, String dbHost, String dbUser, String dbPassword, String dbTable) {
        return sparkSession.read()
                .format("jdbc") // Use JDBC format for database connections.
                .option("url", dbHost) // The JDBC URL for the database connection.
                .option("dbtable", dbTable) // The table or query to read data from.
                .option("user", dbUser) // The database user for authentication.
                .option("password", dbPassword) // The corresponding password for the user.
                .load(); // Execute the query and load the data into a Dataset<Row>.
    }
	
	
    /**
     * Reads data from a CSV file and returns it as a Spark Dataset.
     *
     * @param sparkSession The active Spark session to use for reading the file.
     * @param filePath The absolute or relative path to the CSV file.
     * @param delimiter The character used to separate values in the file (e.g., ',', ';').
     * @return A Dataset<Row> containing the data from the CSV file.
     */
    public static Dataset<Row> extractFromCSV(SparkSession sparkSession, String filePath, String delimiter) {
        return sparkSession.read()
                .format("csv")
                .option("header", true) // Use the first row of the file as column names.
                .option("delimiter", delimiter) // Specify the custom delimiter used in the CSV.
                .option("inferSchema", true) // Automatically infer column data types.
                .load(filePath); // Load the file into a Dataset<Row>.
    }


}
