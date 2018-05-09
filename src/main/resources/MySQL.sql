-- phpMyAdmin SQL Dump
-- version 4.6.5.2
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Nov 02, 2017 at 10:50 AM
-- Server version: 10.1.21-MariaDB
-- PHP Version: 7.1.1

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `scs_admin`
--

-- --------------------------------------------------------

--
-- Table structure for table `t_action`
--

CREATE TABLE `t_action` (
  `action_id` bigint(20) NOT NULL,
  `intent_id` bigint(20) DEFAULT NULL,
  `action_name` longtext,
  `webhook_url` longtext,
  `request_body` longtext,
  `call_method` longtext,
  `created` DATETIME DEFAULT   null,
  `kuid` bigint(20) DEFAULT NULL,
  `entity_id` bigint(20) DEFAULT NULL,
  `success_code` longtext,
  `error_code` longtext,
  `session_parameter` longtext
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `t_action`
--

INSERT INTO `t_action` (`action_id`, `intent_id`, `action_name`, `webhook_url`, `request_body`, `call_method`, `created`, `kuid`, `entity_id`, `success_code`, `error_code`, `session_parameter`) VALUES
(4, NULL, 'Error Codes', 'http://10.10.10.212:7001/SCS/api/mockResponse', NULL, 'GET', '2017-09-18 17:45:06', NULL, 4963, NULL, '#$.errorCode#', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `t_action_confirm`
--

CREATE TABLE `t_action_confirm` (
  `confirm_id` bigint(20) NOT NULL,
  `kuid` bigint(20) DEFAULT NULL,
  `confirm_text` varchar(255) DEFAULT NULL,
  `action_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `t_a_error_response`
--

CREATE TABLE `t_a_error_response` (
  `error_id` bigint(20) DEFAULT NULL,
  `action_id` bigint(20) DEFAULT NULL,
  `action_error_response` longtext,
  `created` DATETIME DEFAULT   null
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `t_a_error_response`
--

INSERT INTO `t_a_error_response` (`error_id`, `action_id`, `action_error_response`, `created`) VALUES
(1, 10, 'Please ask the receiver to register to the service', '2017-08-10 17:32:53'),
(2, 10, 'The mobile number you entered should be registered to receive money transfer', '2017-08-10 17:32:53'),
(3, 10, 'Please register the service', '2017-08-10 17:32:53');

-- --------------------------------------------------------

--
-- Table structure for table `t_conversation`
--

CREATE TABLE `t_conversation` (
  `conversation_id` bigint(20) NOT NULL,
  `intent_id` bigint(20) DEFAULT NULL,
  `message_id` longtext,
  `probability` longtext,
  `total_intent` bigint(20) DEFAULT NULL,
  `created` DATETIME DEFAULT   null,
  `kuid` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `t_conversation`
--

--
-- Table structure for table `t_entity`
--

CREATE TABLE `t_entity` (
  `entity_id` bigint(20) NOT NULL,
  `entity_name` longtext,
  `example` longtext,
  `created` DATETIME DEFAULT   null,
  `entity_type_code` longtext,
  `kuid` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `t_entity_question`
--

CREATE TABLE `t_entity_question` (
  `question_id` bigint(20) NOT NULL,
  `entity_id` bigint(20) DEFAULT NULL,
  `question` longtext,
  `created` DATETIME DEFAULT   null,
  `kuid` bigint(20) DEFAULT NULL,
  `title` longtext,
  `button_text` longtext,
  `sub_title` longtext,
  `entity_query` longtext
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `t_entity_type`
--

CREATE TABLE `t_entity_type` (
  `entity_type_id` bigint(20) NOT NULL,
  `entity_type_code` longtext,
  `entity_type_name` longtext,
  `invalid_message` longtext,
  `created` DATETIME DEFAULT   null,
  `kuid` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `t_entity_type`
--

INSERT INTO `t_entity_type` (`entity_type_id`, `entity_type_code`, `entity_type_name`, `invalid_message`, `created`, `kuid`) VALUES
(1, 'GEN', 'Generic', 'Default', '2017-07-15 17:22:03', NULL),
(2, 'MON', 'Amount', 'Invalid Amount', '2017-07-15 17:22:03', NULL),
(3, 'PHN', 'Beneficiary Phone Number', 'Invalid Phone Number', '2017-07-15 17:22:03', NULL),
(4, 'ELS', 'External Service Listing', 'No Info Available', '2017-07-15 17:22:03', NULL),
(5, 'LST', 'Multiple Choice Intents ', 'No Info Available', '2017-07-15 17:22:03', NULL),
(6, 'PIN', 'pin number', 'enter valid pin number', '2017-08-23 11:19:16', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `t_error_response`
--

CREATE TABLE `t_error_response` (
  `id` bigint(20) DEFAULT NULL,
  `error_code` longtext,
  `error_response` longtext,
  `kuid` bigint(20) DEFAULT NULL,
  `created` DATETIME DEFAULT   null,
  `action_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `t_error_response`
--

INSERT INTO `t_error_response` (`id`, `error_code`, `error_response`, `kuid`, `created`, `action_id`) VALUES
(1, 'SCE_INTENT_NOT_FOUND', 'sorry, I dont understand what you are tring to do.', NULL, '2017-08-10 17:22:23', NULL),
(2, 'SCE_ENTITY_NOT_FOUND', 'sorry, I dont understand what you are tring to do.', NULL, '2017-08-10 17:22:23', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `t_flowchart_session`
--

CREATE TABLE `t_flowchart_session` (
  `session_id` longtext,
  `flowchart_id` bigint(20) DEFAULT NULL,
  `intent_id` bigint(20) DEFAULT NULL,
  `entity_id` bigint(20) DEFAULT NULL,
  `flowchart_key` bigint(20) DEFAULT NULL,
  `entry_type` longtext,
  `entry_name` longtext,
  `entry_id` bigint(20) DEFAULT NULL,
  `parameter_value` longtext
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `t_im_session`
--

CREATE TABLE `t_im_session` (
  `session_id` longtext,
  `im_user_id` longtext,
  `created_at` longtext,
  `expired_at` longtext,
  `reason` longtext
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `t_im_session`
--

INSERT INTO `t_im_session` (`session_id`, `im_user_id`, `created_at`, `expired_at`, `reason`) VALUES
('b4229278-d390-4a7d-b90e-09549230f085', '1515431231857175', '2017-09-27T17:02:20.313+04:00', '2017-09-27T17:02:20.380+04:00', 'Message Received from Facebook Messanger'),
('ad4d5eb3-61cd-4cb7-9c06-3e00fa8ddd16', '1519748554755299', '2017-09-27T17:17:12.618+04:00', '2017-09-27T17:17:12.618+04:00', 'Message Received from Facebook Messanger'),
('fdf40193-5620-43e7-8559-3f44af0d12bb', '1613351135364985', '2017-10-01T17:08:01.539+04:00', '2017-10-01T17:08:01.605+04:00', 'Message Received from Facebook Messanger'),
('3eb1b01c-b976-4a6f-85f9-5051fdb99e06', '1407852122643105', '2017-10-03T12:56:37.132+04:00', '2017-10-03T12:56:37.141+04:00', 'Message Received from Facebook Messanger');

-- --------------------------------------------------------

--
-- Table structure for table `t_im_session_log`
--

CREATE TABLE `t_im_session_log` (
  `log_id` bigint(20) NOT NULL,
  `session_id` longtext,
  `message` longtext,
  `source` longtext,
  `created` DATETIME DEFAULT   null,
  `intent_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `t_im_session_log`
--

--
-- Table structure for table `t_intent`
--

CREATE TABLE `t_intent` (
  `intent_id` bigint(20) NOT NULL,
  `intent_definition` longtext,
  `kuid` bigint(20) DEFAULT NULL,
  `created` DATETIME DEFAULT   null
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `t_intent`
--

INSERT INTO `t_intent` (`intent_id`, `intent_definition`, `kuid`, `created`) VALUES
(4906, 'Mepay', 4905, '2017-09-18 15:57:30'),
(4921, 'DewaPayment', 4920, '2017-09-18 16:25:58'),
(4925, 'DewaOutstanding', 4920, '2017-09-18 16:26:32'),
(4949, 'Choicelisting', 4905, '2017-09-18 17:18:37');

-- --------------------------------------------------------

--
-- Table structure for table `t_intent_entity`
--

CREATE TABLE `t_intent_entity` (
  `intent_id` bigint(20) DEFAULT NULL,
  `entity_id` bigint(20) DEFAULT NULL,
  `created` DATETIME DEFAULT   null,
  `order_id` bigint(20) DEFAULT NULL,
  `kuid` bigint(20) DEFAULT NULL,
  `map_id` bigint(20) NOT NULL,
  `required` longtext,
  `flowchart_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `t_keyword`
--

CREATE TABLE `t_keyword` (
  `keyword_id` bigint(20) NOT NULL,
  `intent_id` bigint(20) DEFAULT NULL,
  `keyword` longtext,
  `polarity` longtext,
  `created` DATETIME DEFAULT   null
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


insert into `t_keyword`(keyword_id,intent_id,keyword,polarity) with values(1,2,'hi','P')


-- --------------------------------------------------------

--
-- Table structure for table `t_kr_log`
--

CREATE TABLE `t_kr_log` (
  `kr_log_id` bigint(20) NOT NULL,
  `im_session_log_id` bigint(20) DEFAULT NULL,
  `intent_id` bigint(20) DEFAULT NULL,
  `keyword_rate` int(11) DEFAULT NULL,
  `created` DATETIME DEFAULT   null
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `t_kr_log`
--

-- --------------------------------------------------------

--
-- Table structure for table `t_ku`
--

CREATE TABLE `t_ku` (
  `kuid` bigint(20) NOT NULL,
  `created` DATETIME DEFAULT   null,
  `ku_name` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `t_ku`
--

INSERT INTO `t_ku` (`kuid`, `created`, `ku_name`) VALUES
(11, '2017-10-03 13:09:54', 'Sewa'),
(4905, '2017-09-18 15:57:00', 'Banking'),
(4920, '2017-09-18 16:25:16', 'DEWA');

-- --------------------------------------------------------

--
-- Table structure for table `t_map_regex`
--

CREATE TABLE `t_map_regex` (
  `regex_id` bigint(20) NOT NULL,
  `map_id` bigint(20) NOT NULL,
  `created` DATETIME DEFAULT   null,
  `kuid` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `t_regex`
--

CREATE TABLE `t_regex` (
  `regex_id` bigint(20) NOT NULL,
  `expression` longtext,
  `nm_message` longtext,
  `created` DATETIME DEFAULT   null,
  `kuid` bigint(20) DEFAULT NULL,
  `regex_name` longtext
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `t_response`
--

CREATE TABLE `t_response` (
  `response_id` bigint(20) NOT NULL,
  `intent_id` bigint(20) DEFAULT NULL,
  `entity_id` bigint(20) DEFAULT NULL,
  `response` longtext,
  `created` DATETIME DEFAULT   null,
  `kuid` bigint(20) DEFAULT NULL,
  `error_response` longtext COMMENT 'errorresponse'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `t_session_record`
--

CREATE TABLE `t_session_record` (
  `intent_id` bigint(20) DEFAULT NULL,
  `parameter_name` longtext,
  `parameter_type` longtext,
  `session_id` longtext,
  `entity_id` bigint(20) DEFAULT NULL,
  `created` DATETIME DEFAULT   null,
  `log_status` longtext,
  `flowchart_id` bigint(20) DEFAULT NULL,
  `flowchart_key` bigint(20) DEFAULT NULL,
  `entity_order` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `t_user_mapping`
--

CREATE TABLE `t_user_mapping` (
  `mapping_id` bigint(20) NOT NULL,
  `im_user_id` longtext,
  `im_platform` longtext,
  `backend_accesscode` longtext,
  `a_code_expiry` longtext,
  `created` DATETIME DEFAULT   null
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `t_user_mapping`
--

INSERT INTO `t_user_mapping` (`mapping_id`, `im_user_id`, `im_platform`, `backend_accesscode`, `a_code_expiry`, `created`) VALUES
(1, '1515431231857175', 'Facebook', 'K+u0K8R52vMZErhD4Yt4h50nvbFQA0QMOrUOE2PcMgw=', NULL, '2017-09-27 17:04:45'),
(2, '1519748554755299', 'Facebook', 'K+u0K8R52vMZErhD4Yt4h50nvbFQA0QMOrUOE2PcMgw=', NULL, '2017-09-27 17:20:07');

-- --------------------------------------------------------

--
-- Table structure for table `t_user_rating`
--

CREATE TABLE `t_user_rating` (
  `rating_id` bigint(20) NOT NULL,
  `user_id` longtext,
  `im_session_log_id` bigint(20) DEFAULT NULL,
  `keywords` longtext,
  `intent_id` bigint(20) DEFAULT NULL,
  `created` DATETIME DEFAULT   null
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` bigint(20) NOT NULL,
  `username` varchar(255) NOT NULL,
  `enabled` smallint(6) DEFAULT NULL,
  `full_name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `role` varchar(500) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `username`, `enabled`, `full_name`, `password`, `role`, `email`) VALUES
(1, 'admin', 1, 'Admin Khan', '$2a$10$/ovbxT7w1Ycj/y4jFh0woOozgu3xv/hx52i41yhsKXngc75Ip4a5q', 'ROLE_ADMIN', NULL),
(3, 'userJJ', 1, 'JJ Khan', '$2a$10$9WLWSdh.2jlYTxhz2TYTRuMTKBoPMxrwoaqMzK2ixRBBApD2tOCDG', 'ROLE_ROLE_USER', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `user_details`
--

CREATE TABLE `user_details` (
  `user_id` bigint(20) NOT NULL,
  `username` longtext,
  `first_name` longtext,
  `last_name` longtext,
  `gender` longtext,
  `password` longtext,
  `status` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `user_details`
--

INSERT INTO `user_details` (`user_id`, `username`, `first_name`, `last_name`, `gender`, `password`, `status`) VALUES
(1, 'rogers63', 'david', 'john', 'Female', 'e6a33eee180b07e563d74fee8c2c66b8', 1),
(2, 'mike28', 'rogers', 'paul', 'Male', '2e7dc6b8a1598f4f75c3eaa47958ee2f', 1),
(3, 'rivera92', 'david', 'john', 'Male', '1c3a8e03f448d211904161a6f5849b68', 1),
(4, 'ross95', 'maria', 'sanders', 'Male', '62f0a68a4179c5cdd997189760cbcf18', 1),
(5, 'paul85', 'morris', 'miller', 'Female', '61bd060b07bddfecccea56a82b850ecf', 1),
(6, 'smith34', 'daniel', 'michael', 'Female', '7055b3d9f5cb2829c26cd7e0e601cde5', 1),
(7, 'james84', 'sanders', 'paul', 'Female', 'b7f72d6eb92b45458020748c8d1a3573', 1),
(8, 'daniel53', 'mark', 'mike', 'Male', '299cbf7171ad1b2967408ed200b4e26c', 1),
(9, 'brooks80', 'morgan', 'maria', 'Female', 'aa736a35dc15934d67c0a999dccff8f6', 1),
(10, 'morgan65', 'paul', 'miller', 'Female', 'a28dca31f5aa5792e1cefd1dfd098569', 1);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `t_action`
--
ALTER TABLE `t_action`
  ADD PRIMARY KEY (`action_id`),
  ADD UNIQUE KEY `t_action_intent_uid` (`intent_id`),
  ADD KEY `taction_taconfirm_aid_fkey` (`kuid`);

--
-- Indexes for table `t_action_confirm`
--
ALTER TABLE `t_action_confirm`
  ADD PRIMARY KEY (`confirm_id`),
  ADD KEY `taction_taconfirm_aid_fkey_1` (`action_id`);

--
-- Indexes for table `t_conversation`
--
ALTER TABLE `t_conversation`
  ADD PRIMARY KEY (`conversation_id`),
  ADD KEY `tku_kuid_fkey` (`kuid`),
  ADD KEY `tconversation_tintent_intentid_fkey` (`intent_id`);

--
-- Indexes for table `t_entity`
--
ALTER TABLE `t_entity`
  ADD PRIMARY KEY (`entity_id`),
  ADD UNIQUE KEY `entityname` (`entity_name`(255)),
  ADD UNIQUE KEY `unique_entity` (`entity_name`(255),`kuid`),
  ADD KEY `tentity_kuid_idx` (`kuid`);

--
-- Indexes for table `t_entity_question`
--
ALTER TABLE `t_entity_question`
  ADD PRIMARY KEY (`question_id`),
  ADD KEY `tentityquestion_entityid_idx` (`entity_id`),
  ADD KEY `tentityquestion_kuid_idx` (`kuid`);

--
-- Indexes for table `t_entity_type`
--
ALTER TABLE `t_entity_type`
  ADD PRIMARY KEY (`entity_type_id`),
  ADD UNIQUE KEY `entitytypecode` (`entity_type_code`(255)),
  ADD KEY `tku_kuid_fkey_1` (`kuid`);

--
-- Indexes for table `t_error_response`
--
ALTER TABLE `t_error_response`
  ADD KEY `tku_kuid_fkey_2` (`kuid`),
  ADD KEY `terrresp_tact_aid_fkey` (`action_id`);

--
-- Indexes for table `t_im_session_log`
--
ALTER TABLE `t_im_session_log`
  ADD PRIMARY KEY (`log_id`);

--
-- Indexes for table `t_intent`
--
ALTER TABLE `t_intent`
  ADD PRIMARY KEY (`intent_id`),
  ADD UNIQUE KEY `intentname` (`intent_definition`(255)),
  ADD UNIQUE KEY `unique_intent` (`kuid`,`intent_definition`(255)),
  ADD KEY `tintent_kuid_idx` (`kuid`);

--
-- Indexes for table `t_intent_entity`
--
ALTER TABLE `t_intent_entity`
  ADD PRIMARY KEY (`map_id`),
  ADD KEY `tintententity_entityid_idx` (`entity_id`),
  ADD KEY `tintententity_intentid_idx` (`intent_id`),
  ADD KEY `tintententity_kuid_idx` (`kuid`);

--
-- Indexes for table `t_keyword`
--
ALTER TABLE `t_keyword`
  ADD PRIMARY KEY (`keyword_id`),
  ADD KEY `tkeyword_intentid_idx` (`intent_id`),
  ADD KEY `tkeyword_keyword_idx` (`keyword`(255));

--
-- Indexes for table `t_kr_log`
--
ALTER TABLE `t_kr_log`
  ADD PRIMARY KEY (`kr_log_id`),
  ADD KEY `tkrlog_tintent_intentid_fkey` (`intent_id`);

--
-- Indexes for table `t_ku`
--
ALTER TABLE `t_ku`
  ADD PRIMARY KEY (`kuid`),
  ADD UNIQUE KEY `kuname` (`ku_name`);

--
-- Indexes for table `t_map_regex`
--
ALTER TABLE `t_map_regex`
  ADD KEY `fki_tmap_mapid_intent` (`map_id`),
  ADD KEY `t_ku_kuid_fkey` (`kuid`);

--
-- Indexes for table `t_regex`
--
ALTER TABLE `t_regex`
  ADD PRIMARY KEY (`regex_id`),
  ADD KEY `tku_treg_kuid_fkey` (`kuid`);

--
-- Indexes for table `t_response`
--
ALTER TABLE `t_response`
  ADD PRIMARY KEY (`response_id`),
  ADD KEY `tresponse_entityid_idx` (`entity_id`),
  ADD KEY `tresponse_intentid_idx` (`intent_id`),
  ADD KEY `tresponse_kuid_idx` (`kuid`);

--
-- Indexes for table `t_user_mapping`
--
ALTER TABLE `t_user_mapping`
  ADD PRIMARY KEY (`mapping_id`);

--
-- Indexes for table `t_user_rating`
--
ALTER TABLE `t_user_rating`
  ADD PRIMARY KEY (`rating_id`),
  ADD KEY `turat_timslog_imsid_fkey` (`im_session_log_id`),
  ADD KEY `turat_tintent_intentid_fkey` (`intent_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `t_action`
--
ALTER TABLE `t_action`
  MODIFY `action_id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;
--
-- AUTO_INCREMENT for table `t_conversation`
--
ALTER TABLE `t_conversation`
  MODIFY `conversation_id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=1270;
--
-- AUTO_INCREMENT for table `t_im_session_log`
--
ALTER TABLE `t_im_session_log`
  MODIFY `log_id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4699;
--
-- AUTO_INCREMENT for table `t_intent_entity`
--
ALTER TABLE `t_intent_entity`
  MODIFY `map_id` bigint(20) NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT for table `t_kr_log`
--
ALTER TABLE `t_kr_log`
  MODIFY `kr_log_id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=395;
--
-- AUTO_INCREMENT for table `t_user_rating`
--
ALTER TABLE `t_user_rating`
  MODIFY `rating_id` bigint(20) NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;
--
-- Constraints for dumped tables
--

--
-- Constraints for table `t_action`
--
ALTER TABLE `t_action`
  ADD CONSTRAINT `taction_taconfirm_aid_fkey` FOREIGN KEY (`kuid`) REFERENCES `t_ku` (`kuid`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `t_action_confirm`
--
ALTER TABLE `t_action_confirm`
  ADD CONSTRAINT `taction_taconfirm_aid_fkey_1` FOREIGN KEY (`action_id`) REFERENCES `t_action` (`action_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `t_conversation`
--
ALTER TABLE `t_conversation`
  ADD CONSTRAINT `tconversation_tintent_intentid_fkey` FOREIGN KEY (`intent_id`) REFERENCES `t_intent` (`intent_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `tku_kuid_fkey` FOREIGN KEY (`kuid`) REFERENCES `t_ku` (`kuid`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `t_entity`
--
ALTER TABLE `t_entity`
  ADD CONSTRAINT `tentity_tkuid_kuid_fkey` FOREIGN KEY (`kuid`) REFERENCES `t_ku` (`kuid`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `t_entity_question`
--
ALTER TABLE `t_entity_question`
  ADD CONSTRAINT `tentity_question_eid_fkey` FOREIGN KEY (`entity_id`) REFERENCES `t_entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `tentity_questions_eid_fkey` FOREIGN KEY (`entity_id`) REFERENCES `t_entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `tentityquestion_tku_kuid_fkey` FOREIGN KEY (`kuid`) REFERENCES `t_ku` (`kuid`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `tku_tent_kuid_fkey` FOREIGN KEY (`kuid`) REFERENCES `t_ku` (`kuid`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `t_entity_type`
--
ALTER TABLE `t_entity_type`
  ADD CONSTRAINT `tku_kuid_fkey_1` FOREIGN KEY (`kuid`) REFERENCES `t_ku` (`kuid`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `t_error_response`
--
ALTER TABLE `t_error_response`
  ADD CONSTRAINT `terror_response_aid_fkey` FOREIGN KEY (`action_id`) REFERENCES `t_action` (`action_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `terrresp_tact_aid_fkey` FOREIGN KEY (`action_id`) REFERENCES `t_action` (`action_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `tku_kuid_fkey_2` FOREIGN KEY (`kuid`) REFERENCES `t_ku` (`kuid`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `t_intent`
--
ALTER TABLE `t_intent`
  ADD CONSTRAINT `tintent_tkuid_kuid_fkey` FOREIGN KEY (`kuid`) REFERENCES `t_ku` (`kuid`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `tku_tint_kuid_fkey` FOREIGN KEY (`kuid`) REFERENCES `t_ku` (`kuid`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `t_intent_entity`
--
ALTER TABLE `t_intent_entity`
  ADD CONSTRAINT `fkmdqeu5rrmdic384dspovvhrr8` FOREIGN KEY (`intent_id`) REFERENCES `t_intent` (`intent_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  ADD CONSTRAINT `fkqmr66tf62xavhgopf2sefy00r` FOREIGN KEY (`entity_id`) REFERENCES `t_entity` (`entity_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  ADD CONSTRAINT `tintent_entity_kuid_fkey` FOREIGN KEY (`kuid`) REFERENCES `t_ku` (`kuid`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `t_keyword`
--
ALTER TABLE `t_keyword`
  ADD CONSTRAINT `tkeyword_intent_intentid_fkey` FOREIGN KEY (`intent_id`) REFERENCES `t_intent` (`intent_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `tkeyword_tintent_intentid_fkey` FOREIGN KEY (`intent_id`) REFERENCES `t_intent` (`intent_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `t_kr_log`
--
ALTER TABLE `t_kr_log`
  ADD CONSTRAINT `tkrlog_tintent_intentid_fkey` FOREIGN KEY (`intent_id`) REFERENCES `t_intent` (`intent_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `t_map_regex`
--
ALTER TABLE `t_map_regex`
  ADD CONSTRAINT `t_ku_kuid_fkey` FOREIGN KEY (`kuid`) REFERENCES `t_ku` (`kuid`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `tmap_mapid_intent` FOREIGN KEY (`map_id`) REFERENCES `t_intent_entity` (`map_id`) ON DELETE NO ACTION ON UPDATE NO ACTION;

--
-- Constraints for table `t_regex`
--
ALTER TABLE `t_regex`
  ADD CONSTRAINT `tku_treg_kuid_fkey` FOREIGN KEY (`kuid`) REFERENCES `t_ku` (`kuid`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `t_response`
--
ALTER TABLE `t_response`
  ADD CONSTRAINT `fkioej7rbk6uwq3yk1si5euckvo` FOREIGN KEY (`intent_id`) REFERENCES `t_intent` (`intent_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  ADD CONSTRAINT `tresponse_intent_intentid_fkey` FOREIGN KEY (`intent_id`) REFERENCES `t_intent` (`intent_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `tresponse_tentity_entityid_fkey` FOREIGN KEY (`entity_id`) REFERENCES `t_entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `tresponse_tintent_intentid_fkey` FOREIGN KEY (`intent_id`) REFERENCES `t_intent` (`intent_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `t_user_rating`
--
ALTER TABLE `t_user_rating`
  ADD CONSTRAINT `turat_timslog_imsid_fkey` FOREIGN KEY (`im_session_log_id`) REFERENCES `t_im_session_log` (`log_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `turat_tintent_intentid_fkey` FOREIGN KEY (`intent_id`) REFERENCES `t_intent` (`intent_id`) ON DELETE CASCADE ON UPDATE CASCADE;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;






Alter table t_intent add column active_ind longtext(1) ;
 
  Alter table t_ku add column active_ind longtext(1) ;
 
  update t_intent set active_ind = 'Y';
 
 
  update t_ku set active_ind = 'Y';



CREATE TABLE t_filler_keyword (
   filler_keyword_id bigint(20) AUTO_INCREMENT NOT NULL,
   filler_keyword longtext NOT NULL,
   created DATETIME DEFAULT   null
);


CREATE TABLE t_cancel_keyword (
   cancel_keyword_id bigint(20) AUTO_INCREMENT NOT NULL,
   cancel_keyword longtext NOT NULL,
   created DATETIME DEFAULT   null
);
 
 
  Alter table t_session_record add column cancel_intent longtext ;

Alter table t_session_record add column confirmation_type longtext ;






