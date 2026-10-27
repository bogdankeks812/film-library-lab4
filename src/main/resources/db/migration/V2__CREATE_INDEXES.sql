-- Индекс для фильтрации фильмов по жанру (GET /api/films?genre=...)
CREATE INDEX idx_films_genre ON films (genre);

-- Индекс для проверки дубликатов по названию и году выпуска
CREATE INDEX idx_films_title_year ON films (title, release_year);
