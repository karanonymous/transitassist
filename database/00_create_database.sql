-- Run ONCE as a MySQL admin (root). Creates a NEW empty database; your old data is not touched.
CREATE DATABASE IF NOT EXISTS transitassist_map CHARACTER SET utf8mb4;
GRANT ALL PRIVILEGES ON transitassist_map.* TO 'transitapp'@'localhost';
FLUSH PRIVILEGES;
