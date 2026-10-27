CREATE TABLE films (
    id           BIGINT       PRIMARY KEY AUTO_INCREMENT,
    title        VARCHAR(255) NOT NULL,
    director     VARCHAR(255) NOT NULL,
    release_year INT          NOT NULL,
    genre        VARCHAR(20)  NOT NULL,
    rating       INT,
    review       TEXT,
    watched      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   DATE,
    updated_at   DATE,
    CONSTRAINT chk_films_release_year CHECK (release_year BETWEEN 1888 AND 2100),
    CONSTRAINT chk_films_rating CHECK (rating IS NULL OR rating BETWEEN 1 AND 10)
);
