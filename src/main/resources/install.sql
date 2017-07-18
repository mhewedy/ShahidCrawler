-- MYSQL

-- sid means "shahid id"

CREATE DATABASE shahid CHARACTER SET utf8 COLLATE utf8_general_ci;

USE shahid;

CREATE TABLE tag(
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tag   VARCHAR(50)
);


CREATE TABLE series(
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  sid   VARCHAR(50),
  title VARCHAR(250),
  poster_url	VARCHAR(4000)
);


CREATE TABLE series_tag(
   id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
   series_id	int,
   tag_id	int
);


CREATE TABLE episode
(
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  sid   VARCHAR(50),
  video_url VARCHAR(250),
  duration_seconds	int,
  watched tinyint,
  series_id	int
);

CREATE TABLE recent
(
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  series_id	int
);
