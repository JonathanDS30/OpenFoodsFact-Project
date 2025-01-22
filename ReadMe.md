# OpenFoodFacts ETL and Menu Generator

This project is designed to extract, clean, and transform data from OpenFoodFacts and generate personalized menus based on user preferences, diets, allergies, and country information. The processed data is stored in a Data Warehouse for further use.

---

## Table of Contents

1. Prerequisites
2. Installation and Setup
3. Usage
4. Project Overview
5. Authors

---

## Prerequisites

Before you can run this project, ensure you have the following installed:

- **Docker**: Used for containerization of services. - [Get Docker here](https://www.docker.com/get-started)
- **Java 17**: Required to run the Java-based ETL pipeline. - [Download Java 17 here](https://download.oracle.com/java/17/archive/jdk-17.0.12_windows-x64_bin.msi)
- **OpenFoodFacts Data**: The raw data is downloaded from OpenFoodFacts for processing. - [Download OpenFoodFacts CSV](https://static.openfoodfacts.org/data/en.openfoodfacts.org.products.csv.gz)

---

## Installation and Setup

1. **Clone the Repository**

```
git clone https://github.com/OpenFoodsFact-Project.git
cd OpenFoodsFact-Project
```

2. **Set Up the Environment**
    
    - Place the downloaded OpenFoodFacts CSV file in the **"data/"** folder.

3. **Build and Run with Docker**
       
    ```
    docker build -t openfoodfacts-etl
    docker-compose up
    ```
    
4. **Run the Project Locally**
    
    Build the project using your preferred IDE or a terminal with Maven/Gradle.
    
    Example with Maven:
        
    ```
    mvn clean install
    java -jar target/openfoodfacts-etl.jar   
    ```
    

---

## Usage

### Pipeline Steps

The ETL pipeline consists of the following steps:

1. **Data Extraction:**
    - Extract raw data from the OpenFoodFacts CSV and user information from the database.
2. **Data Cleaning:**
    - Remove outliers and ensure only high-quality product data is used for processing.
3. **Menu Generation:**
    - Generate personalized menus using the cleaned data based on user preferences, diets, and allergies.
4. **Data Insertion:**
    - Store the generated menus into the Data Warehouse for analysis and use.

---

## Project Overview

The workflow of this project includes the following key modules:

- Extractor.java: Handles data extraction from CSV and database.
- Transformer.java: Cleans and filters the raw data.
- Generator.java: Generates menus based on cleaned data and user preferences.
- Loader.java: Inserts the generated menus into the Data Warehouse.

### Global Schema of the Project

Here is the Global Schema for the project below.

![image](https://github.com/user-attachments/assets/fde3451b-c591-4c5a-8fc9-69523ba06ba8)



### Conceptual Data Model (CDM)

Here is the Conceptual Data Model for the project below.

![image](https://github.com/user-attachments/assets/7e3936f7-6c55-4b26-8614-2b0ec5cb732b)


### Workflow Diagram

The project follows this workflow:

![image](https://github.com/user-attachments/assets/2fdab7eb-13b8-49df-a253-e0fb7be91c9d)



---

## Authors

This project was developed by:

- GARCIA Thomas : [GitHub profile](https://github.com/Keods30)
- DELLA SANTINA Jonathan : [GitHub profile](https://github.com/JonathanDS30)
- MANDELBAUM Léon : [GitHub profile](https://github.com/ml704457)
  
For questions or suggestions, feel free to contact us!

## Contributing

Contributions are welcome! Please fork the repository and submit a pull request with your changes.

## License

This project is licensed under the MIT License. See the LICENSE file for more details.
