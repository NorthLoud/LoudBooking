CREATE DATABASE  IF NOT EXISTS `loud_hotel` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `loud_hotel`;
-- MySQL dump 10.13  Distrib 8.0.42, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: loud_hotel
-- ------------------------------------------------------
-- Server version	9.3.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `bill_details`
--

DROP TABLE IF EXISTS `bill_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bill_details` (
  `bill_detail_id` bigint NOT NULL AUTO_INCREMENT,
  `guest_count` int DEFAULT NULL,
  `nights` int DEFAULT NULL,
  `price_at_booking` double DEFAULT NULL,
  `bill_id` bigint NOT NULL,
  `type_id` bigint NOT NULL,
  PRIMARY KEY (`bill_detail_id`),
  KEY `FKfwm4sko9p82ndh6belyxx12bj` (`bill_id`),
  KEY `FK4cvcod50iedw3lu7kuh000pht` (`type_id`),
  CONSTRAINT `FK4cvcod50iedw3lu7kuh000pht` FOREIGN KEY (`type_id`) REFERENCES `room_types` (`type_id`),
  CONSTRAINT `FKfwm4sko9p82ndh6belyxx12bj` FOREIGN KEY (`bill_id`) REFERENCES `bills` (`bill_id`)
) ENGINE=InnoDB AUTO_INCREMENT=314 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bill_extra_fees`
--

DROP TABLE IF EXISTS `bill_extra_fees`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bill_extra_fees` (
  `extra_fee_id` bigint NOT NULL AUTO_INCREMENT,
  `amount` double NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `is_paid` bit(1) NOT NULL,
  `reason` varchar(255) NOT NULL,
  `bill_id` bigint NOT NULL,
  PRIMARY KEY (`extra_fee_id`),
  KEY `FK23o4nten0ovgbrrfi1b7y3lp7` (`bill_id`),
  CONSTRAINT `FK23o4nten0ovgbrrfi1b7y3lp7` FOREIGN KEY (`bill_id`) REFERENCES `bills` (`bill_id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bills`
--

DROP TABLE IF EXISTS `bills`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bills` (
  `bill_id` bigint NOT NULL AUTO_INCREMENT,
  `actual_check_in_time` datetime(6) DEFAULT NULL,
  `actual_check_out_time` datetime(6) DEFAULT NULL,
  `bill_code` varchar(255) NOT NULL,
  `bill_status` enum('PENDING','PAID','CANCELED') DEFAULT NULL,
  `cancel_reason` enum('USER_CANCEL','HOTEL_CANCEL','NO_SHOW','VNPAY_CANCEL') DEFAULT NULL,
  `check_in_date` date DEFAULT NULL,
  `check_out_date` date DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `id_card_code` varchar(255) DEFAULT NULL,
  `order_email` varchar(255) DEFAULT NULL,
  `order_name` varchar(255) DEFAULT NULL,
  `order_phone` varchar(255) DEFAULT NULL,
  `payment_method` enum('VNPAY','CASH') DEFAULT NULL,
  `total_cost` double DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `vnp_transaction_no` varchar(255) DEFAULT NULL,
  `vnp_txn_ref` varchar(255) DEFAULT NULL,
  `hotel_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `discount_amount` double DEFAULT NULL,
  `voucher_id` bigint DEFAULT NULL,
  PRIMARY KEY (`bill_id`),
  UNIQUE KEY `UK_8li8r7cktrea79tpv6ewf9oc9` (`bill_code`),
  KEY `FK4hx4n4l70fxpftolai9gqi0wa` (`hotel_id`),
  KEY `FKk8vs7ac9xknv5xp18pdiehpp1` (`user_id`),
  KEY `FK1p232jm7oedinqkn0pmlj3upi` (`voucher_id`),
  CONSTRAINT `FK1p232jm7oedinqkn0pmlj3upi` FOREIGN KEY (`voucher_id`) REFERENCES `vouchers` (`voucher_id`),
  CONSTRAINT `FK4hx4n4l70fxpftolai9gqi0wa` FOREIGN KEY (`hotel_id`) REFERENCES `hotels` (`hotel_id`),
  CONSTRAINT `FKk8vs7ac9xknv5xp18pdiehpp1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=158 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `chat_images`
--

DROP TABLE IF EXISTS `chat_images`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_images` (
  `image_id` bigint NOT NULL AUTO_INCREMENT,
  `image_url` varchar(255) DEFAULT NULL,
  `chat_id` bigint NOT NULL,
  PRIMARY KEY (`image_id`),
  KEY `FK5ylie1y7p0lnpgyfd0j0es3fw` (`chat_id`),
  CONSTRAINT `FK5ylie1y7p0lnpgyfd0j0es3fw` FOREIGN KEY (`chat_id`) REFERENCES `chats` (`chat_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `chats`
--

DROP TABLE IF EXISTS `chats`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chats` (
  `chat_id` bigint NOT NULL AUTO_INCREMENT,
  `content_text` text,
  `created_at` datetime(6) DEFAULT NULL,
  `is_deleted` bit(1) DEFAULT NULL,
  `is_edited` bit(1) DEFAULT NULL,
  `is_read` bit(1) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `hotel_id` bigint DEFAULT NULL,
  `recipient_id` bigint NOT NULL,
  `reply_to_id` bigint DEFAULT NULL,
  `sender_id` bigint NOT NULL,
  PRIMARY KEY (`chat_id`),
  KEY `FK9usi9ip09s0x0km7gvcw6sarg` (`hotel_id`),
  KEY `FK66qywv3f3xheb6sk2vxu6vftb` (`recipient_id`),
  KEY `FK2eqcur5inlkv6fy4bjli9g46e` (`reply_to_id`),
  KEY `FKla7peq6fislsxok7a4wxv5p36` (`sender_id`),
  CONSTRAINT `FK2eqcur5inlkv6fy4bjli9g46e` FOREIGN KEY (`reply_to_id`) REFERENCES `chats` (`chat_id`),
  CONSTRAINT `FK66qywv3f3xheb6sk2vxu6vftb` FOREIGN KEY (`recipient_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `FK9usi9ip09s0x0km7gvcw6sarg` FOREIGN KEY (`hotel_id`) REFERENCES `hotels` (`hotel_id`),
  CONSTRAINT `FKla7peq6fislsxok7a4wxv5p36` FOREIGN KEY (`sender_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hotel_images`
--

DROP TABLE IF EXISTS `hotel_images`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hotel_images` (
  `image_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `image_url` varchar(255) NOT NULL,
  `is_main` bit(1) DEFAULT NULL,
  `hotel_id` bigint NOT NULL,
  PRIMARY KEY (`image_id`),
  KEY `FKrj3n45f8oqy1yr996g14j757i` (`hotel_id`),
  CONSTRAINT `FKrj3n45f8oqy1yr996g14j757i` FOREIGN KEY (`hotel_id`) REFERENCES `hotels` (`hotel_id`)
) ENGINE=InnoDB AUTO_INCREMENT=182 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hotels`
--

DROP TABLE IF EXISTS `hotels`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hotels` (
  `hotel_id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(255) DEFAULT NULL,
  `average_rating` double DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `hotel_name` varchar(255) NOT NULL,
  `hotel_status` enum('ACTIVE','INACTIVE','MAINTENANCE') NOT NULL DEFAULT 'ACTIVE',
  `introduction` text,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `updated_at` datetime(6) DEFAULT NULL,
  `manager_id` bigint NOT NULL,
  PRIMARY KEY (`hotel_id`),
  KEY `FKaq04sfgwojtl4faui62h7my9n` (`manager_id`),
  CONSTRAINT `FKaq04sfgwojtl4faui62h7my9n` FOREIGN KEY (`manager_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `reviews`
--

DROP TABLE IF EXISTS `reviews`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reviews` (
  `review_id` bigint NOT NULL AUTO_INCREMENT,
  `comment` text,
  `created_at` datetime(6) DEFAULT NULL,
  `rate` double DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `bill_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`review_id`),
  KEY `FK9f3soldiok3ogwh4fjphong5o` (`bill_id`),
  KEY `FKcgy7qjc1r99dp117y9en6lxye` (`user_id`),
  CONSTRAINT `FK9f3soldiok3ogwh4fjphong5o` FOREIGN KEY (`bill_id`) REFERENCES `bills` (`bill_id`),
  CONSTRAINT `FKcgy7qjc1r99dp117y9en6lxye` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `room_assignments`
--

DROP TABLE IF EXISTS `room_assignments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `room_assignments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `assigned_at` datetime(6) DEFAULT NULL,
  `bill_detail_id` bigint DEFAULT NULL,
  `room_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKqda3men7fayroc33jet371fci` (`bill_detail_id`),
  KEY `FKt96wkyclodjlrg52xftxyve1h` (`room_id`),
  CONSTRAINT `FKqda3men7fayroc33jet371fci` FOREIGN KEY (`bill_detail_id`) REFERENCES `bill_details` (`bill_detail_id`),
  CONSTRAINT `FKt96wkyclodjlrg52xftxyve1h` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`room_id`)
) ENGINE=InnoDB AUTO_INCREMENT=169 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `room_type_images`
--

DROP TABLE IF EXISTS `room_type_images`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `room_type_images` (
  `image_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `image_url` varchar(255) NOT NULL,
  `is_main` bit(1) DEFAULT NULL,
  `type_id` bigint NOT NULL,
  PRIMARY KEY (`image_id`),
  KEY `FKh5ppjdpf1hbet7xr9kqu74l2y` (`type_id`),
  CONSTRAINT `FKh5ppjdpf1hbet7xr9kqu74l2y` FOREIGN KEY (`type_id`) REFERENCES `room_types` (`type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=333 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `room_types`
--

DROP TABLE IF EXISTS `room_types`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `room_types` (
  `type_id` bigint NOT NULL AUTO_INCREMENT,
  `area` double DEFAULT NULL,
  `bed_count` int DEFAULT NULL,
  `bed_type` enum('SINGLE','DOUBLE','TWIN','QUEEN','KING') DEFAULT NULL,
  `capacity` int DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `price` double DEFAULT NULL,
  `type_name` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `hotel_id` bigint DEFAULT NULL,
  PRIMARY KEY (`type_id`),
  KEY `FK42cc0t2sr43om89u1loqh7arj` (`hotel_id`),
  CONSTRAINT `FK42cc0t2sr43om89u1loqh7arj` FOREIGN KEY (`hotel_id`) REFERENCES `hotels` (`hotel_id`)
) ENGINE=InnoDB AUTO_INCREMENT=74 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `rooms`
--

DROP TABLE IF EXISTS `rooms`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rooms` (
  `room_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `room_number` int DEFAULT NULL,
  `room_status` enum('AVAILABLE','MAINTENANCE','OCCUPIED','INACTIVE') NOT NULL DEFAULT 'AVAILABLE',
  `updated_at` datetime(6) DEFAULT NULL,
  `type_id` bigint NOT NULL,
  PRIMARY KEY (`room_id`),
  KEY `FK36pnbgx5yxaalc346d0astj9s` (`type_id`),
  CONSTRAINT `FK36pnbgx5yxaalc346d0astj9s` FOREIGN KEY (`type_id`) REFERENCES `room_types` (`type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=664 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `email` varchar(150) NOT NULL,
  `first_name` varchar(100) DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `last_name` varchar(100) DEFAULT NULL,
  `password` varchar(255) NOT NULL,
  `phone` varchar(20) NOT NULL,
  `refresh_token` text,
  `role` enum('ADMIN','MANAGER','USER') NOT NULL,
  `status` enum('ACTIVE','BLOCKED') NOT NULL DEFAULT 'ACTIVE',
  `updated_at` datetime(6) DEFAULT NULL,
  `username` varchar(100) NOT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `UK_6dotkott2kjsp8vw4d0m25fb7` (`email`),
  UNIQUE KEY `UK_du5v5sr43g5bfnji4vb8hg5s3` (`phone`),
  UNIQUE KEY `UK_r43af9ap4edm43mmtq01oddj6` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `utilities`
--

DROP TABLE IF EXISTS `utilities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `utilities` (
  `utilities_id` bigint NOT NULL AUTO_INCREMENT,
  `utilities_name` varchar(255) NOT NULL,
  `utility_type` enum('ROOM','HOTEL') DEFAULT NULL,
  PRIMARY KEY (`utilities_id`),
  UNIQUE KEY `UK_so6ey20dm25han1b9ei93e330` (`utilities_name`)
) ENGINE=InnoDB AUTO_INCREMENT=59 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `utilities_hotel`
--

DROP TABLE IF EXISTS `utilities_hotel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `utilities_hotel` (
  `hotel_id` bigint NOT NULL,
  `utilities_id` bigint NOT NULL,
  PRIMARY KEY (`hotel_id`,`utilities_id`),
  KEY `FKid1c7j06eybmri8rvmmvmo2ou` (`utilities_id`),
  CONSTRAINT `FKid1c7j06eybmri8rvmmvmo2ou` FOREIGN KEY (`utilities_id`) REFERENCES `utilities` (`utilities_id`),
  CONSTRAINT `FKik4htptkq4ip3ymbdh7biqyqp` FOREIGN KEY (`hotel_id`) REFERENCES `hotels` (`hotel_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `utilities_room_type`
--

DROP TABLE IF EXISTS `utilities_room_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `utilities_room_type` (
  `type_id` bigint NOT NULL,
  `utilities_id` bigint NOT NULL,
  PRIMARY KEY (`type_id`,`utilities_id`),
  KEY `FKogl81qs1gt6xsen801p1c98lc` (`utilities_id`),
  CONSTRAINT `FKb536eehs9ggu72nijnieunh5f` FOREIGN KEY (`type_id`) REFERENCES `room_types` (`type_id`),
  CONSTRAINT `FKogl81qs1gt6xsen801p1c98lc` FOREIGN KEY (`utilities_id`) REFERENCES `utilities` (`utilities_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `voucher_hotels`
--

DROP TABLE IF EXISTS `voucher_hotels`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `voucher_hotels` (
  `voucher_id` bigint NOT NULL,
  `hotel_id` bigint NOT NULL,
  KEY `FKm7voubdxernx7rtq1bo9twusc` (`hotel_id`),
  KEY `FK2944q4pfmumrnqqhob41wbp0u` (`voucher_id`),
  CONSTRAINT `FK2944q4pfmumrnqqhob41wbp0u` FOREIGN KEY (`voucher_id`) REFERENCES `vouchers` (`voucher_id`),
  CONSTRAINT `FKm7voubdxernx7rtq1bo9twusc` FOREIGN KEY (`hotel_id`) REFERENCES `hotels` (`hotel_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vouchers`
--

DROP TABLE IF EXISTS `vouchers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vouchers` (
  `voucher_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `discount_type` enum('PERCENT','FIXED') DEFAULT NULL,
  `discount_value` double DEFAULT NULL,
  `end_date` datetime(6) DEFAULT NULL,
  `is_deleted` bit(1) DEFAULT NULL,
  `max_discount_amount` double DEFAULT NULL,
  `min_bill_amount` double DEFAULT NULL,
  `quantity` int DEFAULT NULL,
  `start_date` datetime(6) DEFAULT NULL,
  `status` enum('ACTIVE','INACTIVE','EXPIRED') DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `used_count` int DEFAULT NULL,
  `voucher_code` varchar(255) NOT NULL,
  PRIMARY KEY (`voucher_id`),
  UNIQUE KEY `UK_hvqsc8qffpt5okjmyot3a4b77` (`voucher_code`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping events for database 'loud_hotel'
--

--
-- Dumping routines for database 'loud_hotel'
--
/*!50003 DROP PROCEDURE IF EXISTS `generate_rooms` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `generate_rooms`()
BEGIN
    DECLARE h INT DEFAULT 1;
    DECLARE base_type INT;

    WHILE h <= 10 DO

        -- type đầu tiên của hotel hiện tại
        SET base_type = ((h - 1) * 6) + 1;


        INSERT INTO rooms(room_number, room_status, hotel_id, type_id)

        VALUES

        /* Standard Single */
        (201,'ACTIVE',h,base_type+5),
        (202,'ACTIVE',h,base_type+5),
        (203,'ACTIVE',h,base_type+5),
        (204,'ACTIVE',h,base_type+5),
        (205,'ACTIVE',h,base_type+5),
        (206,'ACTIVE',h,base_type+5),
        (207,'ACTIVE',h,base_type+5),
        (208,'ACTIVE',h,base_type+5),
        (209,'ACTIVE',h,base_type+5),
        (210,'ACTIVE',h,base_type+5),

        /* Standard Double */
        (301,'ACTIVE',h,base_type+4),
        (302,'ACTIVE',h,base_type+4),
        (303,'ACTIVE',h,base_type+4),
        (304,'ACTIVE',h,base_type+4),
        (305,'ACTIVE',h,base_type+4),
        (306,'ACTIVE',h,base_type+4),
        
        (401,'ACTIVE',h,base_type+4),
        (402,'ACTIVE',h,base_type+4),
        (403,'ACTIVE',h,base_type+4),
        (404,'ACTIVE',h,base_type+4),
        (405,'ACTIVE',h,base_type+4),
        (406,'ACTIVE',h,base_type+4),

        (501,'ACTIVE',h,base_type+4),
        (502,'ACTIVE',h,base_type+4),
        (503,'ACTIVE',h,base_type+4),
        (504,'ACTIVE',h,base_type+4),
        (505,'ACTIVE',h,base_type+4),
        (506,'ACTIVE',h,base_type+4),

        
        /* Twin */
        (601,'ACTIVE',h,base_type+3),
        (602,'ACTIVE',h,base_type+3),
        (603,'ACTIVE',h,base_type+3),
        (604,'ACTIVE',h,base_type+3),
        (605,'ACTIVE',h,base_type+3),
        
        (701,'ACTIVE',h,base_type+3),
        (702,'ACTIVE',h,base_type+3),
        (703,'ACTIVE',h,base_type+3),
        (704,'ACTIVE',h,base_type+3),
        (705,'ACTIVE',h,base_type+3),
        
        /* Deluxe */
        (801,'ACTIVE',h,base_type+2),
        (802,'ACTIVE',h,base_type+2),
        (803,'ACTIVE',h,base_type+2),
        (804,'ACTIVE',h,base_type+2),
        (805,'ACTIVE',h,base_type+2),
        (806,'ACTIVE',h,base_type+2),

        (901,'ACTIVE',h,base_type+2),
        (902,'ACTIVE',h,base_type+2),
        (903,'ACTIVE',h,base_type+2),
        (904,'ACTIVE',h,base_type+2),
        (905,'ACTIVE',h,base_type+2),
        (906,'ACTIVE',h,base_type+2),

        /* Family */
        (1001,'ACTIVE',h,base_type+1),
        (1002,'ACTIVE',h,base_type+1),
        (1003,'ACTIVE',h,base_type+1),
        (1004,'ACTIVE',h,base_type+1),
        (1005,'ACTIVE',h,base_type+1),

        (1101,'ACTIVE',h,base_type+1),
        (1102,'ACTIVE',h,base_type+1),
        (1103,'ACTIVE',h,base_type+1),
        (1104,'ACTIVE',h,base_type+1),
        (1105,'ACTIVE',h,base_type+1),

        /* Suite */
        (1201,'ACTIVE',h,base_type),
        (1202,'ACTIVE',h,base_type),
        (1203,'ACTIVE',h,base_type),
        (1204,'ACTIVE',h,base_type),
        (1205,'ACTIVE',h,base_type);

        SET h = h + 1;

    END WHILE;

END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-16 14:45:09
