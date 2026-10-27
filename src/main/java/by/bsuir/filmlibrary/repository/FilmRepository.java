package by.bsuir.filmlibrary.repository;

import by.bsuir.filmlibrary.model.Film;
import by.bsuir.filmlibrary.model.Genre;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FilmRepository extends JpaRepository<Film, Long> {

    /** Фильмы определённого жанра. */
    List<Film> findByGenre(Genre genre);

    /** Есть ли уже фильм с таким названием и годом выпуска. */
    boolean existsByTitleIgnoreCaseAndReleaseYear(String title, Integer releaseYear);

    /** То же самое, но без учёта фильма с указанным id (нужно при обновлении). */
    boolean existsByTitleIgnoreCaseAndReleaseYearAndIdNot(String title, Integer releaseYear, Long id);
}
