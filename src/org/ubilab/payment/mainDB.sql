CREATE DATABASE IF NOT EXISTS `ubilab_pos` DEFAULT CHARACTER SET utf8;

USE `ubilab_pos`;

DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
    `id` integer unsigned NOT NULL AUTO_INCREMENT,
    `IDm` varchar(16) NOT NULL,
    `name` varchar(255) NOT NULL,
    `uname` varchar(255) NOT NULL,
    `password` blob(32) NOT NULL,
    `mail` varchar(255) NOT NULL,
    `credit` integer NOT NULL DEFAULT '0',
    `skin_fqcn` varchar(255) NOT NULL DEFAULT 'org.ubilab.payment.skin.DefaultSkin',
    `flags` bit(5) NOT NULL DEFAULT b'00000',
    PRIMARY KEY (`id`),
    KEY `IDm` (`IDm`),
    KEY `UserName` (`uname`),
    KEY `Flags` (`flags`),
    UNIQUE `unique` (`IDm`, `uname`, `mail`)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8;

DROP TABLE IF EXISTS `items`;
CREATE TABLE `items` (
    `id` integer unsigned NOT NULL AUTO_INCREMENT,
    `name` varchar(255) NOT NULL,
    `JAN` varchar(13) NOT NULL,
    `price` integer NOT NULL DEFAULT '0',
    `num` integer NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `JAN` (`JAN`),
    UNIQUE (`name`, `JAN`)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8;

DROP TABLE IF EXISTS `history`;
CREATE TABLE `history` (
    `id` integer unsigned NOT NULL AUTO_INCREMENT,
    `user_id` integer unsigned NOT NULL,
    `item_id` integer unsigned NOT NULL,
    `num` integer NOT NULL DEFAULT '1',
    `purchase_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY (`user_id`, `item_id`, `purchase_date`),
    FOREIGN KEY (`user_id`) REFERENCES users (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`item_id`) REFERENCES items (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8;


DELIMITER |

CREATE TRIGGER buy AFTER INSERT ON `history`
    FOR EACH ROW BEGIN
        UPDATE users SET credit = credit - (SELECT price FROM items WHERE id=NEW.item_id) * NEW.num WHERE id=NEW.user_id;
        UPDATE items SET num = num - NEW.num WHERE id=NEW.item_id;
    END;
|

DELIMITER ;


CREATE USER 'ubilab_admin'@'localhost' IDENTIFIED BY 'ubilab_admin';
CREATE USER 'ubilab_payment'@'%' IDENTIFIED BY 'ubilab_payment';
GRANT SELECT, INSERT, UPDATE, DELETE ON ubilab_pos.* TO 'klab_admin'@'localhost';
GRANT INSERT ON ubilab_pos.history TO 'klab_client'@'%';
GRANT SELECT ON ubilab_pos.users TO 'klab_client'@'%';
GRANT SELECT ON ubilab_pos.items TO 'klab_client'@'%';
