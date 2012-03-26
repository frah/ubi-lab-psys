CREATE DATABASE IF NOT EXISTS `ubilab_pos` DEFAULT CHARACTER SET utf8;

USE `ubilab_pos`;

DROP TABLE IF EXISTS `buy_history`;
DROP TABLE IF EXISTS `fund_history`;
DROP TABLE IF EXISTS `receipt`;
DROP TABLE IF EXISTS `items`;
DROP TABLE IF EXISTS `users`;

CREATE TABLE `users` (
    `id` integer unsigned NOT NULL AUTO_INCREMENT,
    `IDm` varchar(16) NOT NULL,
    `name` varchar(255) NOT NULL,
    `nick` varchar(255) NOT NULL,
    `mail` varchar(255) NOT NULL,
    `password` blob(32) NOT NULL,
    `twitter` varchar(255),
    `skin` varchar(255) NOT NULL DEFAULT 'default.xml',
    `flags` bit(5) NOT NULL DEFAULT b'00000',
    `remainder` integer NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `IDm` (`IDm`),
    KEY `mail` (`mail`),
    KEY `twitter` (`twitter`),
    KEY `Flags` (`flags`),
    UNIQUE `unique` (`IDm`, `mail`)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8;

CREATE TABLE `items` (
    `id` integer unsigned NOT NULL AUTO_INCREMENT,
    `name` varchar(255) NOT NULL,
    `JAN` varchar(13) NOT NULL,
    `price` integer NOT NULL DEFAULT '0',
    `cost` integer NOt NULL DEFAULT '0',
    `num` integer NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `JAN` (`JAN`),
    UNIQUE (`name`, `JAN`)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8;

CREATE TABLE `receipt` (
    `id` integer unsigned NOT NULL AUTO_INCREMENT,
    `uid` integer unsigned NOT NULL,
    `amount` integer NOT NULL DEFAULT '0',
    `purchase_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY (`uid`, `purchase_date`),
    FOREIGN KEY (`uid`) REFERENCES users (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8;

CREATE TABLE `buy_history` (
    `id` integer unsigned NOT NULL AUTO_INCREMENT,
    `uid` integer unsigned NOT NULL,
    `item_id` integer unsigned NOT NULL,
    `num` integer NOT NULL DEFAULT '1',
    `receipt_id` integer unsigned NOT NULL,
    `purchase_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY (`uid`, `item_id`, `purchase_date`),
    FOREIGN KEY (`uid`) REFERENCES users (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`item_id`) REFERENCES items (`id`) ON DELETE RESTRICT,
    FOREIGN KEY (`receipt_id`) REFERENCES receipt (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8;

CREATE TABLE `fund_history` (
    `id` integer unsigned NOT NULL AUTO_INCREMENT,
    `uid` integer unsigned,
    `item_id` integer unsigned,
    `amount` integer NOT NULL DEFAULT '1',
    `process_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY (`uid`, `process_date`),
    FOREIGN KEY (`uid`) REFERENCES users (`id`) ON DELETE SET NULL ON UPDATE SET NULL,
    FOREIGN KEY (`item_id`) REFERENCES items (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8;


DELIMITER |

CREATE TRIGGER buy AFTER INSERT ON `buy_history`
    FOR EACH ROW BEGIN
        UPDATE users SET remainder = remainder - (SELECT price FROM items WHERE id=NEW.item_id) * NEW.num WHERE id=NEW.uid;
        UPDATE items SET num = num - NEW.num WHERE id=NEW.item_id;
    END;
|
CREATE TRIGGER fund AFTER INSERT ON `fund_history`
    FOR EACH ROW BEGIN
        IF NEW.uid IS NULL THEN
            UPDATE items SET num = num + NEW.amount WHERE id=NEW.item_id;
        ELSE
            UPDATE users SET remainder = remainder + NEW.amount WHERE id=NEW.uid;
        END IF;
    END;
|

DELIMITER ;

INSERT INTO `items` (`id`, `JAN`, `name`) VALUES (0, '0000000000000', '入金');


CREATE USER 'ubilab_admin'@'localhost' IDENTIFIED BY 'ubilab_admin';
CREATE USER 'ubilab_payment'@'163.221.%' IDENTIFIED BY 'ubilab_payment';
GRANT SELECT, INSERT, UPDATE, DELETE ON ubilab_pos.users TO 'ubilab_admin'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON ubilab_pos.items TO 'ubilab_admin'@'localhost';
GRANT SELECT ON ubilab_pos.receipt TO 'ubilab_admin'@'localhost';
GRANT SELECT ON ubilab_pos.buy_history TO 'ubilab_admin'@'localhost';
GRANT SELECT, INSERT ON ubilab_pos.fund_history TO 'ubilab_admin'@'localhost';
GRANT SELECT ON ubilab_pos.users TO 'ubilab_payment'@'163.221.%';
GRANT SELECT ON ubilab_pos.items TO 'ubilab_payment'@'163.221.%';
GRANT INSERT ON ubilab_pos.buy_history TO 'ubilab_payment'@'163.221.%';
