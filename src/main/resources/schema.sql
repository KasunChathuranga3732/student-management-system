CREATE TABLE IF NOT EXISTS Student(
    id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(200) NOT NULL,
    contact VARCHAR(20) NOT NULL,
    gender ENUM('MALE', 'FEMALE') NOT NULL
);

CREATE TABLE IF NOT EXISTS Picture(
      student_id VARCHAR(20) PRIMARY KEY,
      picture MEDIUMBLOB NOT NULL,
      CONSTRAINT fk_picture FOREIGN KEY(student_id) REFERENCES Student(id)
);

