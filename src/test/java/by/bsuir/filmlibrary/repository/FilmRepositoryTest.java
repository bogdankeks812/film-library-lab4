package by.bsuir.filmlibrary.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import by.bsuir.filmlibrary.config.JpaConfig;
import by.bsuir.filmlibrary.model.Film;
import by.bsuir.filmlibrary.model.Genre;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

/**
 * Тесты слоя репозитория.
 * @DataJpaTest поднимает только JPA-слой и встроенную H2,
 * каждый тест выполняется в транзакции и откатывается после завершения.
 * Mockito здесь не нужен - проверяется работа с настоящей БД.
 */
@DataJpaTest
@Import(JpaConfig.class)
class FilmRepositoryTest {

    @Autowired
    private FilmRepository filmRepository;

    private Film interstellar;

    @BeforeEach
    void setUp() {
        interstellar = filmRepository.save(
                buildFilm("Interstellar", "Christopher Nolan", 2014, Genre.SCI_FI, true, 10));
    }

    private Film buildFilm(String title, String director, int year, Genre genre, boolean watched, Integer rating) {
        Film film = new Film();
        film.setTitle(title);
        film.setDirector(director);
        film.setReleaseYear(year);
        film.setGenre(genre);
        film.setWatched(watched);
        film.setRating(rating);
        return film;
    }

    @Test
    @DisplayName("save: фильму присваивается id и заполняются даты аудита")
    void saveFilmTest() {
        Film film = filmRepository.save(
                buildFilm("Dune", "Denis Villeneuve", 2021, Genre.SCI_FI, false, null));

        assertThat(film.getId()).isNotNull().isGreaterThan(0L);
        assertThat(film.getCreatedAt()).isNotNull();
        assertThat(film.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("findById: сохранённый фильм находится по id")
    void findByIdTest() {
        Optional<Film> found = filmRepository.findById(interstellar.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Interstellar");
        assertThat(found.get().getGenre()).isEqualTo(Genre.SCI_FI);
    }

    @Test
    @DisplayName("findAll: возвращаются все сохранённые фильмы")
    void findAllTest() {
        filmRepository.save(buildFilm("The Shining", "Stanley Kubrick", 1980, Genre.HORROR, false, null));

        List<Film> films = filmRepository.findAll();

        assertThat(films).hasSize(2);
    }

    @Test
    @DisplayName("save: изменение существующего фильма сохраняется")
    void updateFilmTest() {
        Film film = filmRepository.findById(interstellar.getId()).orElseThrow();
        film.setRating(9);
        film.setReview("Пересмотрел");
        filmRepository.saveAndFlush(film);

        Film updated = filmRepository.findById(interstellar.getId()).orElseThrow();
        assertThat(updated.getRating()).isEqualTo(9);
        assertThat(updated.getReview()).isEqualTo("Пересмотрел");
    }

    @Test
    @DisplayName("deleteById: фильм удаляется из базы")
    void deleteFilmTest() {
        filmRepository.deleteById(interstellar.getId());

        assertThat(filmRepository.findById(interstellar.getId())).isEmpty();
    }

    @Test
    @DisplayName("findByGenre: возвращаются только фильмы нужного жанра")
    void findByGenreTest() {
        filmRepository.save(buildFilm("The Shining", "Stanley Kubrick", 1980, Genre.HORROR, false, null));
        filmRepository.save(buildFilm("Hereditary", "Ari Aster", 2018, Genre.HORROR, true, 8));

        List<Film> horrors = filmRepository.findByGenre(Genre.HORROR);

        assertThat(horrors).hasSize(2)
                .extracting(Film::getTitle)
                .containsExactlyInAnyOrder("The Shining", "Hereditary");
    }

    @Test
    @DisplayName("existsByTitleIgnoreCaseAndReleaseYear: дубликат ищется без учёта регистра")
    void existsByTitleAndYearTest() {
        assertThat(filmRepository.existsByTitleIgnoreCaseAndReleaseYear("INTERSTELLAR", 2014)).isTrue();
        assertThat(filmRepository.existsByTitleIgnoreCaseAndReleaseYear("Interstellar", 2000)).isFalse();
    }

    @Test
    @DisplayName("existsBy...AndIdNot: фильм не считается дубликатом самого себя")
    void existsByTitleAndYearExcludingIdTest() {
        assertThat(filmRepository.existsByTitleIgnoreCaseAndReleaseYearAndIdNot(
                "Interstellar", 2014, interstellar.getId())).isFalse();

        Film other = filmRepository.save(buildFilm("Dune", "Denis Villeneuve", 2021, Genre.SCI_FI, false, null));
        assertThat(filmRepository.existsByTitleIgnoreCaseAndReleaseYearAndIdNot(
                "Interstellar", 2014, other.getId())).isTrue();
    }

    @Test
    @DisplayName("save: фильм без названия не сохраняется (валидация сущности)")
    void saveInvalidFilmTest() {
        Film invalid = buildFilm("", "Nobody", 2020, Genre.DRAMA, false, null);

        Throwable thrown = catchThrowable(() -> filmRepository.saveAndFlush(invalid));

        assertThat(hasCause(thrown, ConstraintViolationException.class)).isTrue();
    }

    /** Проверяет, есть ли в цепочке причин исключение нужного типа. */
    private static boolean hasCause(Throwable thrown, Class<? extends Throwable> type) {
        for (Throwable t = thrown; t != null; t = t.getCause()) {
            if (type.isInstance(t)) {
                return true;
            }
        }
        return false;
    }
}
