CREATE DATABASE IF NOT EXISTS medline;

USE medline;

CREATE TABLE IF NOT EXISTS citation (
        citation_id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
        pubmed_id       VARCHAR(10) UNIQUE NOT NULL,
        title   VARCHAR(500) CHARSET utf8 NOT NULL,
        abstract VARCHAR(10000) CHARSET utf8 NULL
)

