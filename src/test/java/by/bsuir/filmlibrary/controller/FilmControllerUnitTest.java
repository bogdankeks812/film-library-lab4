package by.bsuir.filmlibrary.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import by.bsuir.filmlibrary.dto.FilmCreateDTO;
import by.bsuir.filmlibrary.dto.FilmDTO;
import by.bsuir.filmlibrary.dto.FilmUpdateDTO;
import by.bsuir.filmlibrary.exception.DuplicateFilmException;
import by.bsuir.filmlibrary.exception.ResourceNotFoundException;
import by.bsuir.filmlibrary.model.Genre;
import by.bsuir.filmlibrary.service.FilmService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Тесты слоя контроллеров.
 * @WebMvcTest поднимает только веб-слой (контроллер + обработчик исключений),
 * а сервис заменяется mock-объектом Mockito - база данных не используется.
 */
@WebMvcTest(FilmController.class)
class FilmControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FilmService filmService;

    private FilmDTO filmDto;

    @BeforeEach
    void setUp() {
        filmDto = new FilmDTO();
        filmDto.setId(1L);
        filmDto.setTitle("Interstellar");
        filmDto.setDirector("Christopher Nolan");
        filmDto.setReleaseYear(2014);
        filmDto.setGenre(Genre.SCI_FI);
        filmDto.setWatched(true);
        filmDto.setRating(10);
        filmDto.setCreatedAt(LocalDate.now());
        filmDto.setUpdatedAt(LocalDate.now());
    }

    private FilmCreateDTO buildCreateDto() {
        FilmCreateDTO dto = new FilmCreateDTO();
        dto.setTitle("Interstellar");
        dto.setDirector("Christopher Nolan");
        dto.setReleaseYear(2014);
        dto.setGenre(Genre.SCI_FI);
        dto.setWatched(true);
        dto.setRating(10);
        return dto;
    }

    @Test
    @DisplayName("POST /api/films: фильм создаётся, ответ 201")
    void createFilmTest() throws Exception {
        given(filmService.create(any(FilmCreateDTO.class))).willReturn(filmDto);

        mockMvc.perform(post("/api/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCreateDto())))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Interstellar"))
                .andExpect(jsonPath("$.genre").value("SCI_FI"));

        verify(filmService, times(1)).create(any(FilmCreateDTO.class));
    }

    @Test
    @DisplayName("POST /api/films: некорректные данные -> 400, сервис не вызывается")
    void createInvalidFilmTest() throws Exception {
        FilmCreateDTO dto = buildCreateDto();
        dto.setTitle("");
        dto.setReleaseYear(1500);

        mockMvc.perform(post("/api/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.releaseYear").exists());

        verify(filmService, never()).create(any());
    }

    @Test
    @DisplayName("POST /api/films: дубликат -> 409")
    void createDuplicateFilmTest() throws Exception {
        given(filmService.create(any(FilmCreateDTO.class)))
                .willThrow(new DuplicateFilmException("Фильм уже есть в фильмотеке"));

        mockMvc.perform(post("/api/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCreateDto())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("GET /api/films: возвращается список фильмов")
    void getAllFilmsTest() throws Exception {
        FilmDTO second = new FilmDTO();
        second.setId(2L);
        second.setTitle("Dune");
        second.setGenre(Genre.SCI_FI);
        given(filmService.getAll(isNull())).willReturn(List.of(filmDto, second));

        mockMvc.perform(get("/api/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[1].title").value("Dune"));
    }

    @Test
    @DisplayName("GET /api/films?genre=HORROR: жанр передаётся в сервис")
    void getFilmsByGenreTest() throws Exception {
        given(filmService.getAll(Genre.HORROR)).willReturn(List.of());

        mockMvc.perform(get("/api/films").param("genre", "HORROR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(filmService).getAll(Genre.HORROR);
    }

    @Test
    @DisplayName("GET /api/films/{id}: фильм найден, ответ 200")
    void getFilmByIdTest() throws Exception {
        given(filmService.getById(1L)).willReturn(filmDto);

        mockMvc.perform(get("/api/films/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Interstellar"))
                .andExpect(jsonPath("$.director").value("Christopher Nolan"))
                .andExpect(jsonPath("$.rating").value(10));
    }

    @Test
    @DisplayName("GET /api/films/{id}: фильм не найден -> 404")
    void getFilmByIdNotFoundTest() throws Exception {
        given(filmService.getById(99L)).willThrow(new ResourceNotFoundException("Фильм с id=99 не найден"));

        mockMvc.perform(get("/api/films/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("PUT /api/films/{id}: фильм обновляется, ответ 200")
    void updateFilmTest() throws Exception {
        filmDto.setRating(9);
        given(filmService.update(eq(1L), any(FilmUpdateDTO.class))).willReturn(filmDto);

        mockMvc.perform(put("/api/films/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("rating", 9))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(9));
    }

    @Test
    @DisplayName("DELETE /api/films/{id}: фильм удаляется, ответ 204")
    void deleteFilmTest() throws Exception {
        willDoNothing().given(filmService).delete(1L);

        mockMvc.perform(delete("/api/films/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(filmService, times(1)).delete(1L);
    }

    @Test
    @DisplayName("DELETE /api/films/{id}: фильм не найден -> 404")
    void deleteFilmNotFoundTest() throws Exception {
        willThrow(new ResourceNotFoundException("Фильм с id=99 не найден"))
                .given(filmService).delete(99L);

        mockMvc.perform(delete("/api/films/{id}", 99L))
                .andExpect(status().isNotFound());
    }
}
