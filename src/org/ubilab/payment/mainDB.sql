CREATE DATABASE IF NOT EXISTS `ubilab_pos` DEFAULT CHARACTER SET utf8;

USE `ubilab_pos`;

DROP TABLE IF EXISTS `buy_history`;
DROP TABLE IF EXISTS `fund_history`;
DROP TABLE IF EXISTS `receipt`;
DROP TABLE IF EXISTS `items`;
DROP TABLE IF EXISTS `users`;

/*
<ユーザテーブル> ユーザ情報を格納
id       : ユーザID
IDm      : カードID
name     : 氏名
nick     : 表示名
mail     : メールアドレス
password : パスワード（ハッシュ保存）
twitter  : Twitterユーザ名（任意）
skin     : スキンファイル名
flags    : 汎用フラグ（購入時のつぶやきを希望？・レシート希望？・日報希望？・月報希望？）
remainder: 残高
*/
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

/*
<商品テーブル> 商品情報を格納
id    : 商品ID
name  : 商品名
JAN   : JANコード
price : 売価
num   : 在庫数
*/
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

/*
<レシートテーブル> 決済単位での購入履歴
id           : レシート番号
uid          : 購入者
amount       : 合計金額
purchase_date: 購入日
*/
CREATE TABLE `receipt` (
    `id` integer unsigned NOT NULL AUTO_INCREMENT,
    `uid` integer unsigned NOT NULL,
    `amount` integer NOT NULL DEFAULT '0',
    `purchase_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY (`uid`, `purchase_date`),
    FOREIGN KEY (`uid`) REFERENCES users (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8;

/*
<購入履歴テーブル> 商品単位での購入履歴
id           : 履歴番号
item_id      : 商品ID
num          : 個数
receipt_id   : レシート番号
*/
CREATE TABLE `buy_history` (
    `id` integer unsigned NOT NULL AUTO_INCREMENT,
    `item_id` integer unsigned NOT NULL,
    `num` integer NOT NULL DEFAULT '1',
    `receipt_id` integer unsigned NOT NULL,
    PRIMARY KEY (`id`),
    KEY (`item_id`),
    FOREIGN KEY (`item_id`) REFERENCES items (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
    FOREIGN KEY (`receipt_id`) REFERENCES receipt (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8;

/*
<基金履歴テーブル> 基金の入出金履歴
id          : 履歴番号
uid         : 入金者ID（入荷時はNULL）
item_id     : 商品ID（入金時は0を使用）
amount      : 入金額・入荷数
cost        : 入荷額．入金時は0
process_date: 処理日時
*/
CREATE TABLE `fund_history` (
    `id` integer unsigned NOT NULL AUTO_INCREMENT,
    `uid` integer unsigned,
    `item_id` integer unsigned,
    `amount` integer NOT NULL DEFAULT '1',
    `cost` integer NOT NULL DEFAULT '0',
    `process_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY (`uid`, `process_date`),
    FOREIGN KEY (`uid`) REFERENCES users (`id`) ON DELETE SET NULL ON UPDATE SET NULL,
    FOREIGN KEY (`item_id`) REFERENCES items (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8;


DELIMITER |

-- ユーザ出金処理
CREATE TRIGGER pay AFTER INSERT ON `receipt`
    FOR EACH ROW BEGIN
        UPDATE users SET remainder = remainder - ABS(NEW.amount) WHERE id=NEW.uid;
    END;
|
-- 商品出庫処理
CREATE TRIGGER `leave` AFTER INSERT ON `buy_history`
    FOR EACH ROW BEGIN
        UPDATE items SET num = num - ABS(NEW.num) WHERE id=NEW.item_id;
    END;
|
-- 資金入出金処理
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


/*
<データベース操作例>
・購入
1. トランザクション開始
2. レシートテーブルに購入者と合計金額を追加
3. レシート番号を取得（SELECT LAST_INSERT_ID();）
4. 購入テーブルにアイテムごとの個数をレシート番号を添えて追加
5. トランザクション終了
START TRANSACTION;
INSERT INTO receipt (uid, amount) VALUES (1, 530);
SELECT LAST_INSERT_ID();
INSERT INTO buy_history (item_id, num) VALUES (1, 1);
INSERT INTO buy_history (item_id, num) VALUES (2, 1);
COMMIT;

・入金
1. 基金履歴テーブルにユーザIDと商品ID0，入金額を入力
INSERT INTO fund_history (uid, item_id, amount) VALUES (1, 0, 1000);

・入荷
1. 基金履歴テーブルに商品IDと入荷数，金額を入力
INSERT INTO fund_history (item_id, amount, cost) VALUES (1, 10, 1000);

・指定期間のユーザの購入実績の検索
1. レシートテーブルをuidとpurchase_dateで絞り込み検索
2. amountを合計
3. 購入品目は抽出されたレシート番号で購入履歴テーブルを検索
SELECT nick, SUM(amount) FROM (SELECT u.nick, r.amount FROM receipt r JOIN users u ON r.uid=u.id) GROUP BY nick ORDER BY amount DESC LIMIT 10;
SELECT nick, SUM(amount) FROM receipt r INNER JOIN users u ON r.uid=u.id GROUP BY nick ORDER BY amount DESC LIMIT 10;

・売れ筋ランキング
売上トップテン（未テスト・期間指定する場合はWHERE句を追加）
SELECT name, SUM(num) FROM (SELECT i.name, b.num FROM buy_history b JOIN items i ON b.item_id=i.id) GROUP BY name ORDER BY num DESC LIMIT 10;
SELECT name, SUM(num) FROM buy_history b INNER JOIN items i ON b.item_id=i.id GROUP BY name ORDER BY num DESC LIMIT 10;

・平均原価の計算
1. 基金履歴テーブルから原価を計算したいitem_idを抽出
2. costの合計をamountの合計で割る
SELECT SUM(cost)/SUM(amount) FROM fund_history WHERE item_id=1;

・現在資金の計算
1. 基金履歴テーブルから入金履歴を検索（uid != null）
2. 抽出されたレコードのamountを合計（入金額合計）
3. 基金履歴テーブルから出勤履歴を検索 (uid == null)
4. 抽出されたレコードのcostを合計（出金額合計）
5. 入金額合計-出金額合計
SELECT (SELECT SUM(amount) FROM fund_history WHERE uid IS NOT NULL) - (SELECT SUM(cost) FROM fund_history WHERE uid IS NULL);

・利益の計算
現在のデータベース構造では正確な利益計算は不可能
→ 販売履歴テーブルに販売額を記録するようにすると大体の計算は可能？
→ より正確な値を出すには結構計算が面倒くさい
*/