package com.epsi.openfoodfacts;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

/**
 * This utility class provides methods to load data into relational databases.
 * It is designed to be modular and reusable in data pipelines.
 */
public class Loader {

    /**
     * Loads a Spark Dataset into a MySQL table.
     *
     * @param dataset The Spark Dataset<Row> to load into the database.
     * @param dbHost The JDBC URL of the database server, including the protocol and port.
     * @param dbUser The username for authenticating with the database.
     * @param dbPassword The password associated with the given username.
     * @param dbTable The name of the table to load the data into.
     * @param overwrite Whether to overwrite the existing table or append to it.
     */
    public static void loadToDatabase(Dataset<Row> dataset, String dbHost, String dbUser, String dbPassword, String dbTable) {
        dataset.write()
                .format("jdbc") // Use JDBC format for database writes.
                .option("url", dbHost) // The JDBC URL for the database connection.
                .option("dbtable", dbTable) // The table to write data into.
                .option("user", dbUser) // The database user for authentication.
                .option("password", dbPassword) // The corresponding password for the user.
                .mode("append") // Choose the write mode.
                .save(); // Execute the write operation.
    }
}
