package by.bsuir.filmlibrary.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import by.bsuir.filmlibrary.dto.FilmCreateDTO;
import by.bsuir.filmlibrary.model.Film;
import by.bsuir.filmlibrary.model.Genre;
import by.bsuir.filmlibrary.repository.FilmRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Интеграционные тесты REST API: поднимается весь контекст Spring,
 * запросы идут через MockMvc, данные сохраняются в H2.
 */
@SpringBootTest
@AutoConfigureMockMvc
class FilmControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private FilmRepository filmRepository;

    private Film testFilm;

    @BeforeEach
    void setUp() {
        filmRepository.deleteAll();
        testFilm = filmRepository.save(
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

    private FilmCreateDTO buildCreateDto(String title, int year, boolean watched, Integer rating) {
        FilmCreateDTO dto = new FilmCreateDTO();
        dto.setTitle(title);
        dto.setDirector("Denis Villeneuve");
        dto.setReleaseYear(year);
        dto.setGenre(Genre.SCI_FI);
        dto.setWatched(watched);
        dto.setRating(rating);
        return dto;
    }

    @Test
    void testIndex() throws Exception {
        mockMvc.perform(get("/api/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Interstellar"));
    }

    @Test
    void testIndexFilterByGenre() throws Exception {
        filmRepository.save(buildFilm("The Shining", "Stanley Kubrick", 1980, Genre.HORROR, false, null));

        mockMvc.perform(get("/api/films").param("genre", "HORROR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("The Shining"));
    }

    @Test
    void testShow() throws Exception {
        mockMvc.perform(get("/api/films/" + testFilm.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testFilm.getId().intValue()))
                .andExpect(jsonPath("$.title").value("Interstellar"))
                .andExpect(jsonPath("$.director").value("Christopher Nolan"))
                .andExpect(jsonPath("$.genre").value("SCI_FI"))
                .andExpect(jsonPath("$.rating").value(10))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void testShowNotFound() throws Exception {
        mockMvc.perform(get("/api/films/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreate() throws Exception {
        var dto = buildCreateDto("Dune", 2021, true, 8);

        mockMvc.perform(post("/api/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Dune"));

        assertThat(filmRepository.count()).isEqualTo(2);
        assertThat(filmRepository.existsByTitleIgnoreCaseAndReleaseYear("Dune", 2021)).isTrue();
    }

    @Test
    void testCreateWithInvalidData() throws Exception {
        var dto = buildCreateDto("", 1500, true, 15);

        mockMvc.perform(post("/api/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.releaseYear").exists())
                .andExpect(jsonPath("$.rating").exists());

        assertThat(filmRepository.count()).isEqualTo(1);
    }

    @Test
    void testCreateDuplicate() throws Exception {
        var dto = buildCreateDto("interstellar", 2014, false, null);

        mockMvc.perform(post("/api/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isConflict());

        assertThat(filmRepository.count()).isEqualTo(1);
    }

    @Test
    void testCreateRatingForUnwatchedFilm() throws Exception {
        var dto = buildCreateDto("Arrival", 2016, false, 9);

        mockMvc.perform(post("/api/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdate() throws Exception {
        var data = Map.of("rating", 9, "review", "Пересмотрел - всё ещё шедевр");

        mockMvc.perform(put("/api/films/" + testFilm.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(data)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(9));

        Film updated = filmRepository.findById(testFilm.getId()).orElseThrow();
        assertThat(updated.getRating()).isEqualTo(9);
        assertThat(updated.getReview()).isEqualTo("Пересмотрел - всё ещё шедевр");
        // Поля, которые не передавали, не изменились
        assertThat(updated.getTitle()).isEqualTo("Interstellar");
        assertThat(updated.getDirector()).isEqualTo("Christopher Nolan");
    }

    @Test
    void testUpdateMarkUnwatchedResetsRating() throws Exception {
        var data = Map.of("watched", false);

        mockMvc.perform(put("/api/films/" + testFilm.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(data)))
                .andExpect(status().isOk());

        Film updated = filmRepository.findById(testFilm.getId()).orElseThrow();
        assertThat(updated.isWatched()).isFalse();
        assertThat(updated.getRating()).isNull();
    }

    @Test
    void testDestroy() throws Exception {
        mockMvc.perform(delete("/api/films/" + testFilm.getId()))
                .andExpect(status().isNoContent());

        assertThat(filmRepository.existsById(testFilm.getId())).isFalse();
    }

    @Test
    void testDestroyNotFound() throws Exception {
        mockMvc.perform(delete("/api/films/999999"))
                .andExpect(status().isNotFound());
    }
}
