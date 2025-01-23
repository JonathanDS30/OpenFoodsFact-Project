-- MySQL dump 10.13  Distrib 8.0.40, for Linux (x86_64)
--
-- Host: localhost    Database: openFoodFact
-- ------------------------------------------------------
-- Server version	8.0.40

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `allergies`
--

DROP TABLE IF EXISTS `allergies`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `allergies` (
  `id` int NOT NULL,
  `name` varchar(100) NOT NULL,
  `description` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `allergies`
--

LOCK TABLES `allergies` WRITE;
/*!40000 ALTER TABLE `allergies` DISABLE KEYS */;
INSERT INTO `allergies` VALUES (1,'None','No allergies'),(2,'Gluten','Includes wheat gluten and derivatives'),(3,'Milk and Dairy','Includes milk, lactose, and dairy products'),(4,'Nuts','Includes nuts like peanuts, almonds, cashews, etc.'),(5,'Shellfish','Includes shellfish and molluscs'),(6,'Eggs','Includes eggs and egg derivatives'),(7,'Fish','Includes fish and seafood'),(8,'Soy','Includes soybeans and soy-based products'),(9,'Sulphites','Includes sulphur dioxide and sulphites'),(10,'Wheat','Includes wheat and wheat-based products'),(11,'Fruits','Includes fruits like oranges, bananas, apples, etc.'),(12,'Sesame Seeds','Includes sesame seeds and sesame oil');
/*!40000 ALTER TABLE `allergies` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `countries`
--

DROP TABLE IF EXISTS `countries`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `countries` (
  `country_code` varchar(10) NOT NULL,
  `country_name` varchar(255) NOT NULL,
  PRIMARY KEY (`country_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `countries`
--

LOCK TABLES `countries` WRITE;
/*!40000 ALTER TABLE `countries` DISABLE KEYS */;
INSERT INTO `countries` VALUES ('AD','Andorra'),('AE','United Arab Emirates'),('AF','Afghanistan'),('AG','Antigua and Barbuda'),('AI','Anguilla'),('AL','Albania'),('AM','Armenia'),('AN','Netherlands Antilles'),('AO','Angola'),('AQ','Antarctica'),('AR','Argentina'),('AS','American Samoa'),('AT','Austria'),('AU','Australia'),('AW','Aruba'),('AZ','Azerbaijan'),('BA','Bosnia and Herzegovina'),('BB','Barbados'),('BD','Bangladesh'),('BE','Belgium'),('BF','Burkina Faso'),('BG','Bulgaria'),('BH','Bahrain'),('BI','Burundi'),('BJ','Benin'),('BM','Bermuda'),('BN','Brunei'),('BO','Bolivia'),('BR','Brazil'),('BS','Bahamas'),('BT','Bhutan'),('BV','Bouvet Island'),('BW','Botswana'),('BY','Belarus'),('BZ','Belize'),('CA','Canada'),('CC','Cocos [Keeling] Islands'),('CD','Congo [DRC]'),('CF','Central African Republic'),('CG','Congo [Republic]'),('CH','Switzerland'),('CI','CÔö£├óÔö¼Ôöñte d\'Ivoire'),('CK','Cook Islands'),('CL','Chile'),('CM','Cameroon'),('CN','China'),('CO','Colombia'),('CR','Costa Rica'),('CU','Cuba'),('CV','Cape Verde'),('CX','Christmas Island'),('CY','Cyprus'),('CZ','Czech Republic'),('DE','Germany'),('DJ','Djibouti'),('DK','Denmark'),('DM','Dominica'),('DO','Dominican Republic'),('DZ','Algeria'),('EC','Ecuador'),('EE','Estonia'),('EG','Egypt'),('EH','Western Sahara'),('ER','Eritrea'),('ES','Spain'),('ET','Ethiopia'),('FI','Finland'),('FJ','Fiji'),('FK','Falkland Islands [Islas Malvinas]'),('FM','Micronesia'),('FO','Faroe Islands'),('FR','France'),('GA','Gabon'),('GB','United Kingdom'),('GD','Grenada'),('GE','Georgia'),('GF','French Guiana'),('GG','Guernsey'),('GH','Ghana'),('GI','Gibraltar'),('GL','Greenland'),('GM','Gambia'),('GN','Guinea'),('GP','Guadeloupe'),('GQ','Equatorial Guinea'),('GR','Greece'),('GS','South Georgia and the South Sandwich Islands'),('GT','Guatemala'),('GU','Guam'),('GW','Guinea-Bissau'),('GY','Guyana'),('GZ','Gaza Strip'),('HK','Hong Kong'),('HM','Heard Island and McDonald Islands'),('HN','Honduras'),('HR','Croatia'),('HT','Haiti'),('HU','Hungary'),('ID','Indonesia'),('IE','Ireland'),('IL','Israel'),('IM','Isle of Man'),('IN','India'),('IO','British Indian Ocean Territory'),('IQ','Iraq'),('IR','Iran'),('IS','Iceland'),('IT','Italy'),('JE','Jersey'),('JM','Jamaica'),('JO','Jordan'),('JP','Japan'),('KE','Kenya'),('KG','Kyrgyzstan'),('KH','Cambodia'),('KI','Kiribati'),('KM','Comoros'),('KN','Saint Kitts and Nevis'),('KP','North Korea'),('KR','South Korea'),('KW','Kuwait'),('KY','Cayman Islands'),('KZ','Kazakhstan'),('LA','Laos'),('LB','Lebanon'),('LC','Saint Lucia'),('LI','Liechtenstein'),('LK','Sri Lanka'),('LR','Liberia'),('LS','Lesotho'),('LT','Lithuania'),('LU','Luxembourg'),('LV','Latvia'),('LY','Libya'),('MA','Morocco'),('MC','Monaco'),('MD','Moldova'),('ME','Montenegro'),('MG','Madagascar'),('MH','Marshall Islands'),('MK','Macedonia [FYROM]'),('ML','Mali'),('MM','Myanmar [Burma]'),('MN','Mongolia'),('MO','Macau'),('MP','Northern Mariana Islands'),('MQ','Martinique'),('MR','Mauritania'),('MS','Montserrat'),('MT','Malta'),('MU','Mauritius'),('MV','Maldives'),('MW','Malawi'),('MX','Mexico'),('MY','Malaysia'),('MZ','Mozambique'),('NA','Namibia'),('NC','New Caledonia'),('NE','Niger'),('NF','Norfolk Island'),('NG','Nigeria'),('NI','Nicaragua'),('NL','Netherlands'),('NO','Norway'),('NP','Nepal'),('NR','Nauru'),('NU','Niue'),('NZ','New Zealand'),('OM','Oman'),('PA','Panama'),('PE','Peru'),('PF','French Polynesia'),('PG','Papua New Guinea'),('PH','Philippines'),('PK','Pakistan'),('PL','Poland'),('PM','Saint Pierre and Miquelon'),('PN','Pitcairn Islands'),('PR','Puerto Rico'),('PS','Palestinian Territories'),('PT','Portugal'),('PW','Palau'),('PY','Paraguay'),('QA','Qatar'),('RE','RÔö£├óÔö¼┬«union'),('RO','Romania'),('RS','Serbia'),('RU','Russia'),('RW','Rwanda'),('SA','Saudi Arabia'),('SB','Solomon Islands'),('SC','Seychelles'),('SD','Sudan'),('SE','Sweden'),('SG','Singapore'),('SH','Saint Helena'),('SI','Slovenia'),('SJ','Svalbard and Jan Mayen'),('SK','Slovakia'),('SL','Sierra Leone'),('SM','San Marino'),('SN','Senegal'),('SO','Somalia'),('SR','Suriname'),('ST','SÔö£├óÔö¼├║o TomÔö£├óÔö¼┬« and PrÔö£├óÔö¼┬íncipe'),('SV','El Salvador'),('SY','Syria'),('SZ','Swaziland'),('TC','Turks and Caicos Islands'),('TD','Chad'),('TF','French Southern Territories'),('TG','Togo'),('TH','Thailand'),('TJ','Tajikistan'),('TK','Tokelau'),('TL','Timor-Leste'),('TM','Turkmenistan'),('TN','Tunisia'),('TO','Tonga'),('TR','Turkey'),('TT','Trinidad and Tobago'),('TV','Tuvalu'),('TW','Taiwan'),('TZ','Tanzania'),('UA','Ukraine'),('UG','Uganda'),('UM','U.S. Minor Outlying Islands'),('US','United States'),('UY','Uruguay'),('UZ','Uzbekistan'),('VA','Vatican City'),('VC','Saint Vincent and the Grenadines'),('VE','Venezuela'),('VG','British Virgin Islands'),('VI','U.S. Virgin Islands'),('VN','Vietnam'),('VU','Vanuatu'),('WF','Wallis and Futuna'),('WS','Samoa'),('XK','Kosovo'),('YE','Yemen'),('YT','Mayotte'),('ZA','South Africa'),('ZM','Zambia'),('ZW','Zimbabwe');
/*!40000 ALTER TABLE `countries` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `menu_days`
--

DROP TABLE IF EXISTS `menu_days`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `menu_days` (
  `id` varchar(255) NOT NULL,
  `menu_id` int DEFAULT NULL,
  `day_of_week` int DEFAULT NULL,
  `breakfast_id` int DEFAULT NULL,
  `lunch_id_1` int DEFAULT NULL,
  `lunch_id_2` int DEFAULT NULL,
  `dinner_id_1` int DEFAULT NULL,
  `dinner_id_2` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `menu_id` (`menu_id`),
  KEY `breakfast_id` (`breakfast_id`),
  KEY `lunch_id_1` (`lunch_id_1`),
  KEY `lunch_id_2` (`lunch_id_2`),
  KEY `dinner_id_1` (`dinner_id_1`),
  KEY `dinner_id_2` (`dinner_id_2`),
  CONSTRAINT `menu_days_ibfk_1` FOREIGN KEY (`menu_id`) REFERENCES `menus` (`id`),
  CONSTRAINT `menu_days_ibfk_2` FOREIGN KEY (`breakfast_id`) REFERENCES `products` (`id`),
  CONSTRAINT `menu_days_ibfk_3` FOREIGN KEY (`lunch_id_1`) REFERENCES `products` (`id`),
  CONSTRAINT `menu_days_ibfk_4` FOREIGN KEY (`lunch_id_2`) REFERENCES `products` (`id`),
  CONSTRAINT `menu_days_ibfk_5` FOREIGN KEY (`dinner_id_1`) REFERENCES `products` (`id`),
  CONSTRAINT `menu_days_ibfk_6` FOREIGN KEY (`dinner_id_2`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `menu_days`
--

LOCK TABLES `menu_days` WRITE;
/*!40000 ALTER TABLE `menu_days` DISABLE KEYS */;
/*!40000 ALTER TABLE `menu_days` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `menus`
--

DROP TABLE IF EXISTS `menus`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `menus` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`),
  CONSTRAINT `menus_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `menus`
--

LOCK TABLES `menus` WRITE;
/*!40000 ALTER TABLE `menus` DISABLE KEYS */;
/*!40000 ALTER TABLE `menus` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `id` int NOT NULL AUTO_INCREMENT,
  `product_name` varchar(255) DEFAULT NULL,
  `categories_en` text,
  `brands` text,
  `energy-kcal_100g` float DEFAULT NULL,
  `fat_100g` float DEFAULT NULL,
  `saturated-fat_100g` float DEFAULT NULL,
  `carbohydrates_100g` float DEFAULT NULL,
  `sugars_100g` float DEFAULT NULL,
  `fiber_100g` float DEFAULT NULL,
  `proteins_100g` float DEFAULT NULL,
  `salt_100g` float DEFAULT NULL,
  `sodium_100g` float DEFAULT NULL,
  `allergens` text,
  `countries_tags` text,
  `nutriscore_grade` char(1) DEFAULT NULL,
  `nova_group` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `regimes`
--

DROP TABLE IF EXISTS `regimes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `regimes` (
  `regime_id` int NOT NULL,
  `regime_name` varchar(100) NOT NULL,
  `description` text,
  `calories_max` float DEFAULT NULL,
  `fat_max` float DEFAULT NULL,
  `saturated_fat_max` float DEFAULT NULL,
  `carbs_max` float DEFAULT NULL,
  `fiber_max` float DEFAULT NULL,
  `proteins_max` float DEFAULT NULL,
  `sodium_max` float DEFAULT NULL,
  `sugars_max` float DEFAULT NULL,
  `cholesterol_max` float DEFAULT NULL,
  PRIMARY KEY (`regime_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `regimes`
--

LOCK TABLES `regimes` WRITE;
/*!40000 ALTER TABLE `regimes` DISABLE KEYS */;
INSERT INTO `regimes` VALUES (1,'Keto','Low-carb, high-fat diet focused on ketosis',2000,100,30,50,10,150,2000,10,300),(2,'Vegetarian','Plant-based diet allowing dairy and eggs',2000,60,15,250,30,100,1500,50,200),(3,'Vegan','Strictly plant-based diet with no animal products',2000,60,15,250,30,100,1500,50,0),(4,'DASH','Dietary Approach to Stop Hypertension, low sodium',2500,70,20,200,25,100,1500,30,200),(5,'Mediterranean','Rich in fruits, vegetables, and healthy fats',2500,80,25,230,30,120,1800,30,250),(6,'Low-Carb','Low carbohydrate intake to control blood sugar',1800,80,20,100,20,130,1500,25,200),(7,'Paleo','Focus on whole foods, no processed items',2500,100,25,150,25,150,2000,25,300),(8,'High-Protein','High-protein intake for muscle maintenance',2500,60,15,200,20,200,1500,30,250),(9,'Low-Fat','Low-fat diet for weight management',2000,30,10,300,25,100,1500,20,200),(10,'Diabetic','Diet for blood sugar control',2000,60,15,150,20,100,1500,20,200),(11,'FODMAP','Low FODMAP to reduce digestive symptoms',2000,60,15,150,20,120,1200,20,200),(12,'Low-Sodium','Very low sodium intake for heart health',2000,60,15,150,20,120,1000,20,200),(13,'Weight-Loss','Calorie-restricted diet for weight loss',1500,50,10,150,20,100,1500,15,150),(14,'High-Calorie','High-calorie diet for energy and weight gain',3500,120,40,400,35,150,2000,50,400);
/*!40000 ALTER TABLE `regimes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `age` int DEFAULT NULL,
  `gender` char(1) DEFAULT NULL,
  `weight` float DEFAULT NULL,
  `height` float DEFAULT NULL,
  `diet_id` int DEFAULT NULL,
  `allergy_id` int DEFAULT NULL,
  `country_code` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `diet_id` (`diet_id`),
  KEY `allergy_id` (`allergy_id`),
  KEY `country_code` (`country_code`),
  CONSTRAINT `users_ibfk_1` FOREIGN KEY (`diet_id`) REFERENCES `regimes` (`regime_id`),
  CONSTRAINT `users_ibfk_2` FOREIGN KEY (`allergy_id`) REFERENCES `allergies` (`id`),
  CONSTRAINT `users_ibfk_3` FOREIGN KEY (`country_code`) REFERENCES `countries` (`country_code`),
  CONSTRAINT `users_chk_1` CHECK ((`gender` in (_utf8mb4'M',_utf8mb4'F')))
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'Alice Dupont',28,'F',65,170,1,2,'FR'),(2,'Jean Martin',35,'M',80,180,3,1,'FR'),(3,'Emma Dubois',22,'F',55,160,4,3,'FR');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-01-23 18:12:32
