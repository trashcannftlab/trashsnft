
SET FOREIGN_KEY_CHECKS=0;

CREATE TABLE `user` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `account` varchar(255) NOT NULL COMMENT '',
  `password` varchar(255) NOT NULL COMMENT '',
  `my_invite_code` varchar(255) NOT NULL COMMENT '',
  `invite_code` varchar(255) NOT NULL COMMENT '',
  `status` int(11) NOT NULL DEFAULT '1' COMMENT '',
  `game_coin_amount` bigint(20) NOT NULL DEFAULT '0' COMMENT '',
  PRIMARY KEY (`id`),
  UNIQUE KEY `account_unique` (`account`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE nft.user ADD user_type varchar(10) DEFAULT '1' NULL COMMENT '';

CREATE TABLE `eth_log` (
  `block_num` bigint DEFAULT NULL,
  `from` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `to` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `timestamp` bigint DEFAULT NULL,
  `hash` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `input` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `token` varchar(100) COLLATE utf8mb4_bin DEFAULT NULL,
  `contract` varchar(500) COLLATE utf8mb4_bin DEFAULT NULL,
  UNIQUE KEY `hash_unique` (`hash`),
  KEY `eth_log_from_IDX` (`from`) USING BTREE,
  KEY `eth_log_to_IDX` (`to`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- nft.user_wallet definition

CREATE TABLE `user_wallet` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '',
  `encrypt_key` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `encrypted_private` varchar(500) COLLATE utf8mb4_bin DEFAULT NULL,
  `status` int NOT NULL DEFAULT '0' COMMENT '',
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_wallet_UN` (`address`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- nft.user_log definition

CREATE TABLE `user_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `log` varchar(100) COLLATE utf8mb4_bin DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `data` varchar(100) COLLATE utf8mb4_bin DEFAULT NULL,
  `type` varchar(100) COLLATE utf8mb4_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
--


CREATE TABLE `system_nft_events` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `block_number` varchar(100) DEFAULT NULL COMMENT 'block_number',
  `block_timestamp` varchar(100) DEFAULT NULL COMMENT 'block_timestamp',
  `block_hash` varchar(200) DEFAULT NULL COMMENT 'block_hash',
  `transaction_hash` varchar(200) DEFAULT NULL COMMENT 'transaction_hash',
  `transaction_index` varchar(10) DEFAULT NULL COMMENT 'transaction_index',
  `log_index` varchar(10) DEFAULT NULL COMMENT 'log_index',
  `value` varchar(10) DEFAULT NULL COMMENT 'value',
  `contract_type` varchar(50) DEFAULT NULL COMMENT 'contract_type',
  `transaction_type` varchar(50) DEFAULT NULL COMMENT 'transaction_type',
  `token_address` varchar(300) DEFAULT NULL COMMENT 'nft',
  `chain_type` varchar(100) DEFAULT 'mainnet' COMMENT 'mainnet',
  `token_id` varchar(100) DEFAULT '' COMMENT 'token_id',
  `from_address` varchar(100) DEFAULT '' COMMENT 'from_address',
  `to_address` varchar(100) DEFAULT '' COMMENT 'to_address',
  `amount` varchar(100) DEFAULT '' COMMENT 'amount',
  `operator` varchar(100) DEFAULT '' COMMENT 'operator',
  `verified` varchar(100) DEFAULT '' COMMENT 'verified',
  PRIMARY KEY (`id`),
  UNIQUE KEY `system_nft_events_UN` (`token_address`,`token_id`),
  KEY `from_account` (`from_address`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `system_trash_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nft_info` varchar(500) DEFAULT NULL COMMENT '',
  `nft_count` varchar(100) DEFAULT NULL COMMENT '',
  `system_account` varchar(100) DEFAULT NULL COMMENT 'from_account',
  `to_account` varchar(255) DEFAULT NULL COMMENT 'to_account',
  `trash_coin` varchar(255) DEFAULT NULL COMMENT '',
  `trash_coin_send` varchar(255) DEFAULT NULL COMMENT '',
  `reward_nft_count` varchar(255) DEFAULT NULL COMMENT '',
  `trash_nft_send` varchar(255) DEFAULT NULL COMMENT '',
  `trash_coin_claim` varchar(255) DEFAULT NULL COMMENT '',
  `trash_nft_claim` varchar(255) DEFAULT NULL COMMENT '',
  `timestamp` bigint  COMMENT '',
  `contract_address` varchar(300) DEFAULT NULL COMMENT '',
  `chain_type` varchar(100) DEFAULT 'mainnet' COMMENT '',
  `merkle_root_coin` varchar(100) DEFAULT '' COMMENT 'merkle_root_coin',
  `merkle_root_nft` varchar(100) DEFAULT '' COMMENT 'merkle_root_nft',
  `can_claim_time` varchar(100) DEFAULT '' COMMENT '',
  `can_claim_time_nft` varchar(100) DEFAULT '' COMMENT '',
  `status` varchar(100) DEFAULT '1' COMMENT '',
  `amp` varchar(100) DEFAULT '1' COMMENT '',
  PRIMARY KEY (`id`),
  UNIQUE KEY `system_trash_order_UN` (`to_account`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE `order_report` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `recycle_nft_sum` varchar(500) DEFAULT NULL COMMENT '',
  `users_count` varchar(100) DEFAULT NULL COMMENT '',
  `trash_coin_sum` varchar(100) DEFAULT NULL COMMENT '',
  `trash_nft_sum` varchar(255) DEFAULT NULL COMMENT '',
  `claimed_trash_coin` varchar(255) DEFAULT NULL COMMENT '',
  `claimed_trash_nft` varchar(255) DEFAULT NULL COMMENT '',
  `start_time` varchar(100) DEFAULT '' COMMENT '',
  `end_time` varchar(100) DEFAULT '' COMMENT '',
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_report_UN` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


---clean
truncate table order_report;
truncate table system_trash_order;
truncate table system_nft_events;
truncate table user_log;
truncate table eth_log;
truncate table user_wallet;
truncate table `user`;