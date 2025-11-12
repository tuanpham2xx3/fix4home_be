-- MySQL dump 10.13  Distrib 8.0.44, for Linux (x86_64)
--
-- Host: localhost    Database: fix4home_db
-- ------------------------------------------------------
-- Server version	8.0.44

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
-- Current Database: `fix4home_db`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `fix4home_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `fix4home_db`;

--
-- Table structure for table `addresses`
--

DROP TABLE IF EXISTS `addresses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `addresses` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL,
  `recipient_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `recipient_phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `address_line` text COLLATE utf8mb4_unicode_ci,
  `ward` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `district` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `city` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `latitude` decimal(10,6) DEFAULT NULL,
  `longitude` decimal(10,6) DEFAULT NULL,
  `province_code` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Vietnam Administrative API province code (e.g., "01" for Hà Nội)',
  `ward_code` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Vietnam Administrative API ward/district code for precise location identification',
  PRIMARY KEY (`id`),
  KEY `idx_addresses_user` (`user_id`),
  KEY `idx_addresses_location` (`latitude`,`longitude`),
  KEY `idx_addresses_province_code` (`province_code`),
  KEY `idx_addresses_ward_code` (`ward_code`),
  KEY `idx_addresses_province_ward` (`province_code`,`ward_code`),
  KEY `idx_addresses_user_city_district` (`user_id`,`city`,`district`),
  CONSTRAINT `addresses_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `addresses`
--

LOCK TABLES `addresses` WRITE;
/*!40000 ALTER TABLE `addresses` DISABLE KEYS */;
/*!40000 ALTER TABLE `addresses` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `audit_logs`
--

DROP TABLE IF EXISTS `audit_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `audit_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL COMMENT 'ID of the user who made the request (null for anonymous)',
  `username` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `action` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Action performed (CREATE, UPDATE, DELETE, VIEW, etc.)',
  `resource` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Resource type being accessed (USER, SERVICE, etc.)',
  `resource_id` bigint DEFAULT NULL,
  `method` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `endpoint` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ip_address` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Client IP address (supports IPv6)',
  `user_agent` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `request_body` text COLLATE utf8mb4_unicode_ci,
  `response_status` int DEFAULT NULL,
  `processing_time` bigint DEFAULT NULL COMMENT 'Request processing time in milliseconds',
  `session_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `success` tinyint(1) DEFAULT NULL COMMENT 'Whether the request was successful',
  `error_message` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `additional_data` text COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_audit_logs_user_id` (`user_id`),
  KEY `idx_audit_logs_action` (`action`),
  KEY `idx_audit_logs_resource` (`resource`),
  KEY `idx_audit_logs_created_at` (`created_at`),
  KEY `idx_audit_logs_ip_address` (`ip_address`),
  KEY `idx_audit_logs_endpoint` (`endpoint`),
  KEY `idx_audit_logs_success` (`success`),
  KEY `idx_audit_logs_response_status` (`response_status`),
  CONSTRAINT `audit_logs_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Audit logs for security monitoring and request tracking';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `audit_logs`
--

LOCK TABLES `audit_logs` WRITE;
/*!40000 ALTER TABLE `audit_logs` DISABLE KEYS */;
/*!40000 ALTER TABLE `audit_logs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `complaints`
--

DROP TABLE IF EXISTS `complaints`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `complaints` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `service_request_id` bigint NOT NULL,
  `complainant_id` bigint NOT NULL,
  `accused_id` bigint NOT NULL,
  `reason` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  `admin_response` text COLLATE utf8mb4_unicode_ci,
  `resolved_by` bigint DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `resolved_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_complaint_per_service_request` (`service_request_id`,`complainant_id`),
  KEY `idx_complaints_service_request` (`service_request_id`),
  KEY `idx_complaints_complainant` (`complainant_id`),
  KEY `idx_complaints_accused` (`accused_id`),
  KEY `idx_complaints_resolved_by` (`resolved_by`),
  KEY `idx_complaints_status` (`status`),
  KEY `idx_complaints_created_at` (`created_at`),
  KEY `idx_complaints_resolved_at` (`resolved_at`),
  KEY `idx_complaints_service_request_status` (`service_request_id`,`status`),
  KEY `idx_complaints_complainant_status` (`complainant_id`,`status`),
  KEY `idx_complaints_accused_status` (`accused_id`,`status`),
  KEY `idx_complaints_status_created_at` (`status`,`created_at`),
  KEY `idx_complaints_resolved_by_resolved_at` (`resolved_by`,`resolved_at`),
  KEY `idx_complaints_pending_by_created_at` (`status`,`created_at`),
  CONSTRAINT `fk_complaints_accused` FOREIGN KEY (`accused_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_complaints_complainant` FOREIGN KEY (`complainant_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_complaints_resolved_by` FOREIGN KEY (`resolved_by`) REFERENCES `users` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_complaints_service_request` FOREIGN KEY (`service_request_id`) REFERENCES `service_requests` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_complaints_description_length` CHECK ((char_length(trim(`description`)) between 20 and 2000)),
  CONSTRAINT `chk_complaints_description_not_empty` CHECK ((char_length(trim(`description`)) > 0)),
  CONSTRAINT `chk_complaints_different_users` CHECK ((`complainant_id` <> `accused_id`)),
  CONSTRAINT `chk_complaints_reason_length` CHECK ((char_length(trim(`reason`)) between 10 and 200)),
  CONSTRAINT `chk_complaints_reason_not_empty` CHECK ((char_length(trim(`reason`)) > 0)),
  CONSTRAINT `chk_complaints_status` CHECK ((`status` in (_utf8mb4'PENDING',_utf8mb4'INVESTIGATING',_utf8mb4'RESOLVED',_utf8mb4'REJECTED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `complaints`
--

LOCK TABLES `complaints` WRITE;
/*!40000 ALTER TABLE `complaints` DISABLE KEYS */;
/*!40000 ALTER TABLE `complaints` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `consultations`
--

DROP TABLE IF EXISTS `consultations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `consultations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `service_post_id` bigint NOT NULL,
  `technician_id` bigint NOT NULL,
  `proposal` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `quoted_price` decimal(12,2) NOT NULL,
  `notes` text COLLATE utf8mb4_unicode_ci,
  `status` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  `submitted_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `responded_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_consultation_per_technician` (`service_post_id`,`technician_id`),
  KEY `idx_consultations_service_post` (`service_post_id`),
  KEY `idx_consultations_technician` (`technician_id`),
  KEY `idx_consultations_status` (`status`),
  KEY `idx_consultations_submitted_at` (`submitted_at`),
  KEY `idx_consultations_responded_at` (`responded_at`),
  KEY `idx_consultations_quoted_price` (`quoted_price`),
  KEY `idx_consultations_service_post_status` (`service_post_id`,`status`),
  KEY `idx_consultations_technician_status` (`technician_id`,`status`),
  KEY `idx_consultations_status_submitted_at` (`status`,`submitted_at`),
  CONSTRAINT `fk_consultations_service_post` FOREIGN KEY (`service_post_id`) REFERENCES `service_posts` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_consultations_technician` FOREIGN KEY (`technician_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_consultations_proposal_not_empty` CHECK ((char_length(trim(`proposal`)) > 0)),
  CONSTRAINT `chk_consultations_quoted_price` CHECK ((`quoted_price` >= 0)),
  CONSTRAINT `chk_consultations_status` CHECK ((`status` in (_utf8mb4'PENDING',_utf8mb4'ACCEPTED',_utf8mb4'REJECTED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `consultations`
--

LOCK TABLES `consultations` WRITE;
/*!40000 ALTER TABLE `consultations` DISABLE KEYS */;
/*!40000 ALTER TABLE `consultations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `conversations`
--

DROP TABLE IF EXISTS `conversations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conversations` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `service_request_id` bigint DEFAULT NULL COMMENT 'Link to service request (if conversation is about a service request)',
  `service_post_id` bigint DEFAULT NULL COMMENT 'Link to service post (if conversation is about a service post)',
  `consultation_id` bigint DEFAULT NULL COMMENT 'Link to consultation (if conversation is about a consultation)',
  `customer_id` bigint NOT NULL COMMENT 'Customer participant in the conversation',
  `technician_id` bigint NOT NULL COMMENT 'Technician participant in the conversation',
  `status` enum('ACTIVE','ARCHIVED','BLOCKED') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE' COMMENT 'Conversation status - ACTIVE: can send messages, ARCHIVED: read-only, BLOCKED: disabled',
  `last_message_at` timestamp NULL DEFAULT NULL COMMENT 'Timestamp of the last message in this conversation',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'When the conversation was created',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'When the conversation was last updated',
  PRIMARY KEY (`id`),
  KEY `idx_conversations_customer` (`customer_id`),
  KEY `idx_conversations_technician` (`technician_id`),
  KEY `idx_conversations_participants` (`customer_id`,`technician_id`),
  KEY `idx_conversations_service_request` (`service_request_id`),
  KEY `idx_conversations_service_post` (`service_post_id`),
  KEY `idx_conversations_consultation` (`consultation_id`),
  KEY `idx_conversations_status` (`status`),
  KEY `idx_conversations_last_message` (`last_message_at`),
  KEY `idx_conversations_created_at` (`created_at`),
  CONSTRAINT `conversations_ibfk_1` FOREIGN KEY (`service_request_id`) REFERENCES `service_requests` (`id`) ON DELETE SET NULL,
  CONSTRAINT `conversations_ibfk_2` FOREIGN KEY (`service_post_id`) REFERENCES `service_posts` (`id`) ON DELETE SET NULL,
  CONSTRAINT `conversations_ibfk_3` FOREIGN KEY (`consultation_id`) REFERENCES `consultations` (`id`) ON DELETE SET NULL,
  CONSTRAINT `conversations_ibfk_4` FOREIGN KEY (`customer_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `conversations_ibfk_5` FOREIGN KEY (`technician_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Chat conversations between customers and technicians';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `conversations`
--

LOCK TABLES `conversations` WRITE;
/*!40000 ALTER TABLE `conversations` DISABLE KEYS */;
/*!40000 ALTER TABLE `conversations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customer_profiles`
--

DROP TABLE IF EXISTS `customer_profiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customer_profiles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `full_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `gender` enum('MALE','FEMALE','OTHER') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`),
  KEY `idx_customer_profiles_user` (`user_id`),
  CONSTRAINT `customer_profiles_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customer_profiles`
--

LOCK TABLES `customer_profiles` WRITE;
/*!40000 ALTER TABLE `customer_profiles` DISABLE KEYS */;
/*!40000 ALTER TABLE `customer_profiles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `device_tokens`
--

DROP TABLE IF EXISTS `device_tokens`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `device_tokens` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT 'User who owns the device',
  `token` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Device push notification token',
  `platform` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Device platform (ANDROID, IOS, WEB)',
  `device_id` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Unique device identifier',
  `device_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'User-friendly device name',
  `app_version` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'App version',
  `os_version` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Operating system version',
  `is_active` tinyint(1) DEFAULT '1' COMMENT 'Whether token is active',
  `notifications_enabled` tinyint(1) DEFAULT '1' COMMENT 'Whether notifications are enabled',
  `last_used_at` timestamp NULL DEFAULT NULL COMMENT 'Last time token was used',
  `failed_attempts` int DEFAULT '0' COMMENT 'Number of failed delivery attempts',
  `last_failure_at` timestamp NULL DEFAULT NULL COMMENT 'Last failure timestamp',
  `user_agent` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Browser user agent for web devices',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Registration timestamp',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
  PRIMARY KEY (`id`),
  UNIQUE KEY `token` (`token`),
  KEY `idx_device_tokens_user_id` (`user_id`),
  KEY `idx_device_tokens_token` (`token`),
  KEY `idx_device_tokens_platform` (`platform`),
  KEY `idx_device_tokens_is_active` (`is_active`),
  KEY `idx_device_tokens_last_used` (`last_used_at`),
  KEY `idx_device_tokens_user_platform_active` (`user_id`,`platform`,`is_active`,`notifications_enabled`),
  CONSTRAINT `fk_device_tokens_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_device_platform_enum` CHECK ((`platform` in (_utf8mb4'ANDROID',_utf8mb4'IOS',_utf8mb4'WEB',_utf8mb4'UNKNOWN'))),
  CONSTRAINT `chk_failed_attempts_non_negative` CHECK ((`failed_attempts` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Device tokens for push notifications';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `device_tokens`
--

LOCK TABLES `device_tokens` WRITE;
/*!40000 ALTER TABLE `device_tokens` DISABLE KEYS */;
/*!40000 ALTER TABLE `device_tokens` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `feedbacks`
--

DROP TABLE IF EXISTS `feedbacks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `feedbacks` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `service_request_id` bigint NOT NULL,
  `rating` int NOT NULL,
  `comment` text COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_feedbacks_service_request` (`service_request_id`),
  KEY `idx_feedbacks_rating` (`rating`),
  KEY `idx_feedbacks_created_at` (`created_at`),
  KEY `idx_feedbacks_rating_created` (`rating`,`created_at` DESC),
  CONSTRAINT `feedbacks_ibfk_1` FOREIGN KEY (`service_request_id`) REFERENCES `service_requests` (`id`) ON DELETE CASCADE,
  CONSTRAINT `feedbacks_chk_1` CHECK (((`rating` >= 1) and (`rating` <= 5)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `feedbacks`
--

LOCK TABLES `feedbacks` WRITE;
/*!40000 ALTER TABLE `feedbacks` DISABLE KEYS */;
/*!40000 ALTER TABLE `feedbacks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `file_metadata`
--

DROP TABLE IF EXISTS `file_metadata`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `file_metadata` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `original_filename` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Original filename as uploaded',
  `stored_filename` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Unique stored filename (UUID-based)',
  `file_path` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Full file path on storage',
  `file_url` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Public URL to access the file',
  `content_type` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'MIME content type',
  `file_size` bigint NOT NULL COMMENT 'File size in bytes',
  `file_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'File type category (IMAGE, DOCUMENT, etc.)',
  `user_id` bigint NOT NULL COMMENT 'User who uploaded the file',
  `entity_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Type of entity this file belongs to',
  `entity_id` bigint DEFAULT NULL COMMENT 'ID of the entity this file belongs to',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT 'User-provided description of the file',
  `is_public` tinyint(1) DEFAULT '0' COMMENT 'Whether file is publicly accessible',
  `thumbnail_url` text COLLATE utf8mb4_unicode_ci COMMENT 'URL to thumbnail (for images)',
  `width` int DEFAULT NULL COMMENT 'Image width in pixels',
  `height` int DEFAULT NULL COMMENT 'Image height in pixels',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Upload timestamp',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
  PRIMARY KEY (`id`),
  UNIQUE KEY `stored_filename` (`stored_filename`),
  UNIQUE KEY `idx_file_metadata_stored_filename` (`stored_filename`),
  KEY `idx_file_metadata_user_id` (`user_id`),
  KEY `idx_file_metadata_entity_type` (`entity_type`,`entity_id`),
  KEY `idx_file_metadata_file_type` (`file_type`),
  KEY `idx_file_metadata_created_at` (`created_at`),
  KEY `idx_file_metadata_public` (`is_public`,`created_at`),
  KEY `idx_file_metadata_user_type` (`user_id`,`file_type`,`created_at`),
  KEY `idx_file_metadata_original_filename` (`original_filename`),
  CONSTRAINT `fk_file_metadata_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_file_size_positive` CHECK ((`file_size` > 0)),
  CONSTRAINT `chk_file_type_enum` CHECK ((`file_type` in (_utf8mb4'IMAGE',_utf8mb4'DOCUMENT',_utf8mb4'VIDEO',_utf8mb4'AUDIO',_utf8mb4'OTHER'))),
  CONSTRAINT `chk_image_dimensions` CHECK ((((`width` is null) and (`height` is null)) or ((`width` > 0) and (`height` > 0))))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='File metadata and management information';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `file_metadata`
--

LOCK TABLES `file_metadata` WRITE;
/*!40000 ALTER TABLE `file_metadata` DISABLE KEYS */;
/*!40000 ALTER TABLE `file_metadata` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flyway_schema_history`
--

DROP TABLE IF EXISTS `flyway_schema_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flyway_schema_history` (
  `installed_rank` int NOT NULL,
  `version` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `script` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `checksum` int DEFAULT NULL,
  `installed_by` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  KEY `flyway_schema_history_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flyway_schema_history`
--

LOCK TABLES `flyway_schema_history` WRITE;
/*!40000 ALTER TABLE `flyway_schema_history` DISABLE KEYS */;
INSERT INTO `flyway_schema_history` VALUES (1,'001','Create Base Tables','SQL','V001__Create_Base_Tables.sql',1501802161,'fix4home','2025-11-12 08:27:11',2867,1),(2,'006','Create Service Posts Tables','SQL','V006__Create_Service_Posts_Tables.sql',1290815244,'fix4home','2025-11-12 08:27:15',3580,1),(3,'007','Create Consultations Table','SQL','V007__Create_Consultations_Table.sql',898854067,'fix4home','2025-11-12 08:27:17',1824,1),(4,'008','Create Complaints Table','SQL','V008__Create_Complaints_Table.sql',-2041536144,'fix4home','2025-11-12 08:27:20',3126,1),(5,'009','Add Online Status To Technician Profiles','SQL','V009__Add_Online_Status_To_Technician_Profiles.sql',-550309099,'fix4home','2025-11-12 08:27:22',2190,1),(6,'010','Add Technician Approval Fields','SQL','V010__Add_Technician_Approval_Fields.sql',-1015068545,'fix4home','2025-11-12 08:27:25',2184,1),(7,'011','Create Chat System Tables','SQL','V011__Create_Chat_System_Tables.sql',1778044439,'fix4home','2025-11-12 08:27:26',1288,1),(8,'012','Add Email Verification Status','SQL','V012__Add_Email_Verification_Status.sql',1235405628,'fix4home','2025-11-12 08:27:26',49,1),(9,'013','Add Province Ward Codes To Addresses','SQL','V013__Add_Province_Ward_Codes_To_Addresses.sql',-611618116,'fix4home','2025-11-12 08:27:27',907,1),(10,'014','Create File Management Tables','SQL','V014__Create_File_Management_Tables.sql',1292979638,'fix4home','2025-11-12 08:27:28',820,1),(11,'015','Create Enhanced Search Tables','SQL','V015__Create_Enhanced_Search_Tables.sql',-1603404187,'fix4home','2025-11-12 08:27:29',1488,1),(12,'016','Create Push Notification Tables','SQL','V016__Create_Push_Notification_Tables.sql',823961033,'fix4home','2025-11-12 08:27:32',2777,1),(13,'017','Create Audit Logs Table','SQL','V017__Create_Audit_Logs_Table.sql',-666087493,'fix4home','2025-11-12 08:27:33',889,1),(14,'018','Database Performance Optimization','SQL','V018__Database_Performance_Optimization.sql',481929919,'fix4home','2025-11-12 08:27:34',1115,0);
/*!40000 ALTER TABLE `flyway_schema_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `messages`
--

DROP TABLE IF EXISTS `messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `messages` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `conversation_id` bigint NOT NULL COMMENT 'Reference to the conversation this message belongs to',
  `sender_id` bigint DEFAULT NULL COMMENT 'User who sent the message (NULL for system messages)',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Message content/text',
  `message_type` enum('TEXT','IMAGE','LOCATION','SYSTEM','QUOTATION','FILE') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'TEXT' COMMENT 'Type of message - TEXT: plain text, IMAGE: image attachment, LOCATION: location sharing, SYSTEM: automated message, QUOTATION: price quote, FILE: file attachment',
  `attachment_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'URL to attached file/image',
  `metadata` json DEFAULT NULL COMMENT 'Additional message metadata (coordinates for location, pricing for quotation, etc.)',
  `is_read` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Whether the message has been read by the recipient',
  `sent_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'When the message was sent',
  PRIMARY KEY (`id`),
  KEY `idx_messages_conversation` (`conversation_id`),
  KEY `idx_messages_sender` (`sender_id`),
  KEY `idx_messages_conversation_time` (`conversation_id`,`sent_at` DESC),
  KEY `idx_messages_sent_at` (`sent_at`),
  KEY `idx_messages_unread` (`conversation_id`,`is_read`),
  KEY `idx_messages_type` (`message_type`),
  KEY `idx_messages_unread_user` (`conversation_id`,`sender_id`,`is_read`),
  CONSTRAINT `messages_ibfk_1` FOREIGN KEY (`conversation_id`) REFERENCES `conversations` (`id`) ON DELETE CASCADE,
  CONSTRAINT `messages_ibfk_2` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Individual messages within conversations';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `messages`
--

LOCK TABLES `messages` WRITE;
/*!40000 ALTER TABLE `messages` DISABLE KEYS */;
/*!40000 ALTER TABLE `messages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `migration_log`
--

DROP TABLE IF EXISTS `migration_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `migration_log` (
  `version` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `executed_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `migration_log`
--

LOCK TABLES `migration_log` WRITE;
/*!40000 ALTER TABLE `migration_log` DISABLE KEYS */;
INSERT INTO `migration_log` VALUES ('V001','Create base tables - users, services, addresses, profiles, service_requests, etc.','2025-11-12 08:27:11'),('V011','Create Chat System Tables - conversations and messages tables with indexes and constraints','2025-11-12 08:27:26');
/*!40000 ALTER TABLE `migration_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notifications`
--

DROP TABLE IF EXISTS `notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notifications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `message` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` enum('INFO','WARNING','SUCCESS','ERROR') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'INFO',
  `is_read` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_notifications_user` (`user_id`),
  KEY `idx_notifications_is_read` (`is_read`),
  KEY `idx_notifications_created_at` (`created_at`),
  KEY `idx_notifications_user_read_created` (`user_id`,`is_read`,`created_at` DESC),
  CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notifications`
--

LOCK TABLES `notifications` WRITE;
/*!40000 ALTER TABLE `notifications` DISABLE KEYS */;
/*!40000 ALTER TABLE `notifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payments`
--

DROP TABLE IF EXISTS `payments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `service_request_id` bigint NOT NULL,
  `amount` decimal(12,2) NOT NULL,
  `payment_method` enum('CASH','BANK_TRANSFER','CREDIT_CARD','E_WALLET') COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('PENDING','COMPLETED','FAILED','REFUNDED') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  `transaction_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `notes` text COLLATE utf8mb4_unicode_ci,
  `paid_at` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_payments_service_request` (`service_request_id`),
  KEY `idx_payments_status` (`status`),
  KEY `idx_payments_transaction_id` (`transaction_id`),
  KEY `idx_payments_status_created` (`status`,`created_at` DESC),
  CONSTRAINT `payments_ibfk_1` FOREIGN KEY (`service_request_id`) REFERENCES `service_requests` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payments`
--

LOCK TABLES `payments` WRITE;
/*!40000 ALTER TABLE `payments` DISABLE KEYS */;
/*!40000 ALTER TABLE `payments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `push_notifications`
--

DROP TABLE IF EXISTS `push_notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `push_notifications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT 'Target user for notification',
  `device_token_id` bigint DEFAULT NULL COMMENT 'Specific device token (optional)',
  `type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Notification type',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT 'Notification status',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Notification title',
  `message` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Notification message',
  `data_payload` text COLLATE utf8mb4_unicode_ci COMMENT 'JSON data payload',
  `image_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Notification image URL',
  `action_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Action URL when clicked',
  `badge_count` int DEFAULT NULL COMMENT 'Badge count for iOS',
  `sound` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT 'default' COMMENT 'Sound to play',
  `priority` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'normal' COMMENT 'Notification priority',
  `time_to_live` int DEFAULT '86400' COMMENT 'TTL in seconds',
  `scheduled_at` timestamp NULL DEFAULT NULL COMMENT 'Scheduled delivery time',
  `sent_at` timestamp NULL DEFAULT NULL COMMENT 'Actual send time',
  `delivered_at` timestamp NULL DEFAULT NULL COMMENT 'Delivery confirmation time',
  `clicked_at` timestamp NULL DEFAULT NULL COMMENT 'User click time',
  `failed_at` timestamp NULL DEFAULT NULL COMMENT 'Failure time',
  `failure_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Failure reason',
  `retry_count` int DEFAULT '0' COMMENT 'Number of retry attempts',
  `max_retries` int DEFAULT '3' COMMENT 'Maximum retry attempts',
  `external_id` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'External service message ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation timestamp',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
  PRIMARY KEY (`id`),
  KEY `idx_push_notifications_user_id` (`user_id`),
  KEY `idx_push_notifications_device_token_id` (`device_token_id`),
  KEY `idx_push_notifications_status` (`status`),
  KEY `idx_push_notifications_type` (`type`),
  KEY `idx_push_notifications_scheduled_at` (`scheduled_at`),
  KEY `idx_push_notifications_sent_at` (`sent_at`),
  KEY `idx_push_notifications_pending_processing` (`status`,`scheduled_at`,`created_at`),
  KEY `idx_push_notifications_analytics` (`type`,`status`,`created_at`),
  KEY `idx_push_notifications_user_history` (`user_id`,`created_at`),
  KEY `idx_push_notifications_retry` (`status`,`retry_count`,`max_retries`,`failed_at`),
  CONSTRAINT `fk_push_notifications_device_token` FOREIGN KEY (`device_token_id`) REFERENCES `device_tokens` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_push_notifications_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_notification_priority_enum` CHECK ((`priority` in (_utf8mb4'normal',_utf8mb4'high'))),
  CONSTRAINT `chk_notification_status_enum` CHECK ((`status` in (_utf8mb4'PENDING',_utf8mb4'SENT',_utf8mb4'DELIVERED',_utf8mb4'FAILED',_utf8mb4'CANCELLED',_utf8mb4'EXPIRED'))),
  CONSTRAINT `chk_retry_counts` CHECK ((`retry_count` <= `max_retries`)),
  CONSTRAINT `chk_time_to_live_positive` CHECK (((`time_to_live` is null) or (`time_to_live` > 0)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Push notification records and delivery tracking';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `push_notifications`
--

LOCK TABLES `push_notifications` WRITE;
/*!40000 ALTER TABLE `push_notifications` DISABLE KEYS */;
/*!40000 ALTER TABLE `push_notifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `refresh_tokens`
--

DROP TABLE IF EXISTS `refresh_tokens`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `refresh_tokens` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `token` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `expiry_date` timestamp NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `token` (`token`),
  KEY `idx_refresh_tokens_user` (`user_id`),
  KEY `idx_refresh_tokens_token` (`token`),
  KEY `idx_refresh_tokens_expiry` (`expiry_date`),
  KEY `idx_refresh_tokens_expiry_created` (`expiry_date`,`created_at`),
  CONSTRAINT `refresh_tokens_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `refresh_tokens`
--

LOCK TABLES `refresh_tokens` WRITE;
/*!40000 ALTER TABLE `refresh_tokens` DISABLE KEYS */;
/*!40000 ALTER TABLE `refresh_tokens` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `saved_searches`
--

DROP TABLE IF EXISTS `saved_searches`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `saved_searches` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT 'User who owns the saved search',
  `search_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'User-defined name for the search',
  `search_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Type of search (TECHNICIAN, SERVICE_POST, SERVICE)',
  `search_criteria` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'JSON of the complete search criteria',
  `description` varchar(300) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'User description of the saved search',
  `is_active` tinyint(1) DEFAULT '1' COMMENT 'Whether the saved search is active',
  `notification_enabled` tinyint(1) DEFAULT '0' COMMENT 'Whether notifications are enabled',
  `last_executed_at` timestamp NULL DEFAULT NULL COMMENT 'Last time this search was executed',
  `execution_count` int DEFAULT '0' COMMENT 'Number of times this search has been executed',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation timestamp',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_search_name` (`user_id`,`search_name`,`is_active`),
  KEY `idx_saved_searches_user_id` (`user_id`),
  KEY `idx_saved_searches_search_type` (`search_type`),
  KEY `idx_saved_searches_is_active` (`is_active`),
  KEY `idx_saved_searches_user_active` (`user_id`,`is_active`,`created_at`),
  KEY `idx_saved_searches_notifications` (`notification_enabled`,`is_active`),
  KEY `idx_saved_searches_execution` (`execution_count`,`last_executed_at`),
  KEY `idx_saved_searches_name` (`search_name`),
  CONSTRAINT `fk_saved_searches_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_execution_count_non_negative` CHECK ((`execution_count` >= 0)),
  CONSTRAINT `chk_saved_search_type_enum` CHECK ((`search_type` in (_utf8mb4'TECHNICIAN',_utf8mb4'SERVICE_POST',_utf8mb4'SERVICE')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User saved searches for quick access';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `saved_searches`
--

LOCK TABLES `saved_searches` WRITE;
/*!40000 ALTER TABLE `saved_searches` DISABLE KEYS */;
/*!40000 ALTER TABLE `saved_searches` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `search_history`
--

DROP TABLE IF EXISTS `search_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `search_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT 'User who performed the search',
  `search_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Type of search (TECHNICIAN, SERVICE_POST, SERVICE)',
  `search_query` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'The actual search keywords',
  `search_criteria` text COLLATE utf8mb4_unicode_ci COMMENT 'JSON of the complete search criteria',
  `results_count` int DEFAULT NULL COMMENT 'Number of results found',
  `search_location` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Search location text',
  `latitude` double DEFAULT NULL COMMENT 'Search location latitude',
  `longitude` double DEFAULT NULL COMMENT 'Search location longitude',
  `radius` int DEFAULT NULL COMMENT 'Search radius in kilometers',
  `filters_applied` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Comma-separated list of applied filters',
  `sort_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Sort criteria used',
  `sort_direction` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Sort direction (asc/desc)',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Search timestamp',
  PRIMARY KEY (`id`),
  KEY `idx_search_history_user_id` (`user_id`),
  KEY `idx_search_history_created_at` (`created_at`),
  KEY `idx_search_history_search_type` (`search_type`),
  KEY `idx_search_history_user_type_created` (`user_id`,`search_type`,`created_at`),
  KEY `idx_search_history_location` (`latitude`,`longitude`,`created_at`),
  KEY `idx_search_history_query` (`search_query`),
  CONSTRAINT `fk_search_history_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_radius_positive` CHECK (((`radius` is null) or (`radius` > 0))),
  CONSTRAINT `chk_results_count_non_negative` CHECK (((`results_count` is null) or (`results_count` >= 0))),
  CONSTRAINT `chk_search_type_enum` CHECK ((`search_type` in (_utf8mb4'TECHNICIAN',_utf8mb4'SERVICE_POST',_utf8mb4'SERVICE'))),
  CONSTRAINT `chk_sort_direction_enum` CHECK ((`sort_direction` in (_utf8mb4'asc',_utf8mb4'desc')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Search history tracking for users';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `search_history`
--

LOCK TABLES `search_history` WRITE;
/*!40000 ALTER TABLE `search_history` DISABLE KEYS */;
/*!40000 ALTER TABLE `search_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `service_post_responses`
--

DROP TABLE IF EXISTS `service_post_responses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `service_post_responses` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `service_post_id` bigint NOT NULL,
  `technician_id` bigint NOT NULL,
  `message` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `quoted_price` decimal(12,2) NOT NULL,
  `estimated_duration` int DEFAULT NULL,
  `proposed_time` datetime DEFAULT NULL,
  `is_selected` tinyint(1) DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_response_per_technician` (`service_post_id`,`technician_id`),
  KEY `idx_service_post_responses_post` (`service_post_id`),
  KEY `idx_service_post_responses_technician` (`technician_id`),
  KEY `idx_service_post_responses_created_at` (`created_at`),
  KEY `idx_service_post_responses_is_selected` (`is_selected`),
  CONSTRAINT `fk_service_post_responses_post` FOREIGN KEY (`service_post_id`) REFERENCES `service_posts` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_service_post_responses_technician` FOREIGN KEY (`technician_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_service_post_responses_estimated_duration` CHECK (((`estimated_duration` is null) or (`estimated_duration` > 0))),
  CONSTRAINT `chk_service_post_responses_quoted_price` CHECK ((`quoted_price` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `service_post_responses`
--

LOCK TABLES `service_post_responses` WRITE;
/*!40000 ALTER TABLE `service_post_responses` DISABLE KEYS */;
/*!40000 ALTER TABLE `service_post_responses` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `service_posts`
--

DROP TABLE IF EXISTS `service_posts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `service_posts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `customer_id` bigint NOT NULL,
  `service_id` bigint NOT NULL,
  `address_id` bigint NOT NULL,
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `estimated_budget` decimal(12,2) DEFAULT NULL,
  `preferred_time` datetime DEFAULT NULL,
  `type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'SCHEDULED',
  `status` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'DRAFT',
  `max_technicians` int DEFAULT '5',
  `expires_at` datetime DEFAULT NULL,
  `selected_technician_id` bigint DEFAULT NULL,
  `selected_at` datetime DEFAULT NULL,
  `final_price` decimal(12,2) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_service_posts_address` (`address_id`),
  KEY `idx_service_posts_customer` (`customer_id`),
  KEY `idx_service_posts_service` (`service_id`),
  KEY `idx_service_posts_status` (`status`),
  KEY `idx_service_posts_type` (`type`),
  KEY `idx_service_posts_created_at` (`created_at`),
  KEY `idx_service_posts_expires_at` (`expires_at`),
  KEY `idx_service_posts_selected_technician` (`selected_technician_id`),
  CONSTRAINT `fk_service_posts_address` FOREIGN KEY (`address_id`) REFERENCES `addresses` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_service_posts_customer` FOREIGN KEY (`customer_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_service_posts_selected_technician` FOREIGN KEY (`selected_technician_id`) REFERENCES `users` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_service_posts_service` FOREIGN KEY (`service_id`) REFERENCES `services` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_service_posts_estimated_budget` CHECK (((`estimated_budget` is null) or (`estimated_budget` >= 0))),
  CONSTRAINT `chk_service_posts_final_price` CHECK (((`final_price` is null) or (`final_price` >= 0))),
  CONSTRAINT `chk_service_posts_max_technicians` CHECK (((`max_technicians` > 0) and (`max_technicians` <= 50))),
  CONSTRAINT `chk_service_posts_status` CHECK ((`status` in (_utf8mb4'DRAFT',_utf8mb4'POSTED',_utf8mb4'RESPONSES_RECEIVED',_utf8mb4'TECHNICIAN_SELECTED',_utf8mb4'IN_PROGRESS',_utf8mb4'COMPLETED',_utf8mb4'CANCELLED',_utf8mb4'EXPIRED'))),
  CONSTRAINT `chk_service_posts_type` CHECK ((`type` in (_utf8mb4'URGENT',_utf8mb4'CONSULTATION',_utf8mb4'SCHEDULED',_utf8mb4'QUOTATION')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `service_posts`
--

LOCK TABLES `service_posts` WRITE;
/*!40000 ALTER TABLE `service_posts` DISABLE KEYS */;
/*!40000 ALTER TABLE `service_posts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `service_request_logs`
--

DROP TABLE IF EXISTS `service_request_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `service_request_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `service_request_id` bigint NOT NULL,
  `old_status` enum('PENDING','ACCEPTED','IN_PROGRESS','COMPLETED','CANCELLED','COMPLAINING') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `new_status` enum('PENDING','ACCEPTED','IN_PROGRESS','COMPLETED','CANCELLED','COMPLAINING') COLLATE utf8mb4_unicode_ci NOT NULL,
  `changed_by` bigint DEFAULT NULL,
  `changed_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `changed_by` (`changed_by`),
  KEY `idx_service_request_logs_request` (`service_request_id`),
  KEY `idx_service_request_logs_changed_at` (`changed_at`),
  CONSTRAINT `service_request_logs_ibfk_1` FOREIGN KEY (`service_request_id`) REFERENCES `service_requests` (`id`) ON DELETE CASCADE,
  CONSTRAINT `service_request_logs_ibfk_2` FOREIGN KEY (`changed_by`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `service_request_logs`
--

LOCK TABLES `service_request_logs` WRITE;
/*!40000 ALTER TABLE `service_request_logs` DISABLE KEYS */;
/*!40000 ALTER TABLE `service_request_logs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `service_requests`
--

DROP TABLE IF EXISTS `service_requests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `service_requests` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `customer_id` bigint NOT NULL,
  `service_id` bigint NOT NULL,
  `technician_id` bigint DEFAULT NULL,
  `address_id` bigint NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `status` enum('PENDING','ACCEPTED','IN_PROGRESS','COMPLETED','CANCELLED','COMPLAINING') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  `scheduled_time` timestamp NULL DEFAULT NULL,
  `completed_time` timestamp NULL DEFAULT NULL,
  `price` decimal(12,2) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `address_id` (`address_id`),
  KEY `idx_service_requests_customer` (`customer_id`),
  KEY `idx_service_requests_service` (`service_id`),
  KEY `idx_service_requests_technician` (`technician_id`),
  KEY `idx_service_requests_status` (`status`),
  KEY `idx_service_requests_created_at` (`created_at`),
  KEY `idx_service_requests_customer_status` (`customer_id`,`status`,`created_at` DESC),
  KEY `idx_service_requests_technician_status` (`technician_id`,`status`,`created_at` DESC),
  KEY `idx_service_requests_status_created` (`status`,`created_at` DESC),
  CONSTRAINT `service_requests_ibfk_1` FOREIGN KEY (`customer_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `service_requests_ibfk_2` FOREIGN KEY (`service_id`) REFERENCES `services` (`id`) ON DELETE CASCADE,
  CONSTRAINT `service_requests_ibfk_3` FOREIGN KEY (`technician_id`) REFERENCES `users` (`id`) ON DELETE SET NULL,
  CONSTRAINT `service_requests_ibfk_4` FOREIGN KEY (`address_id`) REFERENCES `addresses` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `service_requests`
--

LOCK TABLES `service_requests` WRITE;
/*!40000 ALTER TABLE `service_requests` DISABLE KEYS */;
/*!40000 ALTER TABLE `service_requests` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `services`
--

DROP TABLE IF EXISTS `services`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `services` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `base_price` decimal(12,2) DEFAULT '0.00',
  `status` enum('ACTIVE','INACTIVE') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY (`id`),
  KEY `idx_services_name` (`name`),
  KEY `idx_services_status` (`status`),
  KEY `idx_services_status_name` (`status`,`name`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `services`
--

LOCK TABLES `services` WRITE;
/*!40000 ALTER TABLE `services` DISABLE KEYS */;
INSERT INTO `services` VALUES (1,'Sửa chữa điện','Sửa chữa hệ thống điện trong nhà',200000.00,'ACTIVE'),(2,'Sửa chữa nước','Sửa chữa hệ thống nước, ống nước',150000.00,'ACTIVE'),(3,'Sửa chữa điều hòa','Sửa chữa và bảo trì điều hòa không khí',300000.00,'ACTIVE'),(4,'Sửa chữa tủ lạnh','Sửa chữa và bảo trì tủ lạnh',250000.00,'ACTIVE'),(5,'Sửa chữa máy giặt','Sửa chữa và bảo trì máy giặt',200000.00,'ACTIVE');
/*!40000 ALTER TABLE `services` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `skills`
--

DROP TABLE IF EXISTS `skills`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `skills` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `status` enum('ACTIVE','INACTIVE') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `idx_skills_name` (`name`),
  KEY `idx_skills_status` (`status`),
  KEY `idx_skills_status_name` (`status`,`name`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `skills`
--

LOCK TABLES `skills` WRITE;
/*!40000 ALTER TABLE `skills` DISABLE KEYS */;
INSERT INTO `skills` VALUES (1,'Điện dân dụng','Kiến thức về hệ thống điện dân dụng','ACTIVE'),(2,'Nước và ống nước','Kiến thức về hệ thống nước và ống dẫn','ACTIVE'),(3,'Điều hòa không khí','Sửa chữa và bảo trì điều hòa','ACTIVE'),(4,'Điện lạnh','Kiến thức về hệ thống điện lạnh','ACTIVE'),(5,'Cơ khí','Kiến thức cơ khí cơ bản','ACTIVE');
/*!40000 ALTER TABLE `skills` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `technician_profiles`
--

DROP TABLE IF EXISTS `technician_profiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `technician_profiles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `full_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `skills` text COLLATE utf8mb4_unicode_ci,
  `experience` text COLLATE utf8mb4_unicode_ci,
  `rating` float DEFAULT '0',
  `status` enum('ACTIVE','INACTIVE','PENDING_APPROVAL','REJECTED') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING_APPROVAL',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_online` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Indicates if technician is currently online and available',
  `last_seen_at` timestamp NULL DEFAULT NULL COMMENT 'Timestamp when technician was last seen online',
  `current_latitude` double DEFAULT NULL COMMENT 'Current latitude coordinate of technician',
  `current_longitude` double DEFAULT NULL COMMENT 'Current longitude coordinate of technician',
  `current_address` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Human-readable current address of technician',
  `working_radius` int NOT NULL DEFAULT '10' COMMENT 'Working radius in kilometers from current location',
  `verification_documents` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'URL or path to verification documents uploaded by technician',
  `rejection_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Reason provided by admin when rejecting technician application',
  `approved_at` timestamp NULL DEFAULT NULL COMMENT 'Timestamp when technician was approved by admin',
  `approved_by` bigint DEFAULT NULL COMMENT 'User ID of admin who approved the technician',
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`),
  KEY `idx_technician_profiles_user` (`user_id`),
  KEY `idx_technician_profiles_status` (`status`),
  KEY `idx_technician_profiles_rating` (`rating`),
  KEY `idx_technician_profiles_online_status` (`is_online`),
  KEY `idx_technician_profiles_location` (`current_latitude`,`current_longitude`),
  KEY `idx_technician_profiles_last_seen` (`last_seen_at`),
  KEY `idx_technician_profiles_approved_by` (`approved_by`),
  KEY `idx_technician_profiles_approved_at` (`approved_at`),
  KEY `idx_technician_profiles_rating_status` (`rating` DESC,`status`),
  CONSTRAINT `fk_technician_profiles_approved_by` FOREIGN KEY (`approved_by`) REFERENCES `users` (`id`),
  CONSTRAINT `technician_profiles_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `technician_profiles`
--

LOCK TABLES `technician_profiles` WRITE;
/*!40000 ALTER TABLE `technician_profiles` DISABLE KEYS */;
/*!40000 ALTER TABLE `technician_profiles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `technician_skills`
--

DROP TABLE IF EXISTS `technician_skills`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `technician_skills` (
  `technician_id` bigint NOT NULL,
  `skill_id` bigint NOT NULL,
  `years_experience` int DEFAULT '0',
  `certification_level` enum('BEGINNER','INTERMEDIATE','ADVANCED','EXPERT') COLLATE utf8mb4_unicode_ci DEFAULT 'BEGINNER',
  PRIMARY KEY (`technician_id`,`skill_id`),
  KEY `idx_technician_skills_technician` (`technician_id`),
  KEY `idx_technician_skills_skill` (`skill_id`),
  CONSTRAINT `technician_skills_ibfk_1` FOREIGN KEY (`technician_id`) REFERENCES `technician_profiles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `technician_skills_ibfk_2` FOREIGN KEY (`skill_id`) REFERENCES `skills` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `technician_skills`
--

LOCK TABLES `technician_skills` WRITE;
/*!40000 ALTER TABLE `technician_skills` DISABLE KEYS */;
/*!40000 ALTER TABLE `technician_skills` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone_number` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `role` enum('CUSTOMER','TECHNICIAN','ADMIN') COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('ACTIVE','INACTIVE','PENDING_APPROVAL','REJECTED','PENDING_EMAIL_VERIFICATION') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`),
  KEY `idx_users_username` (`username`),
  KEY `idx_users_email` (`email`),
  KEY `idx_users_role` (`role`),
  KEY `idx_users_status` (`status`),
  KEY `idx_users_role_status_email` (`role`,`status`,`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
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

-- Dump completed on 2025-11-12  8:40:15
