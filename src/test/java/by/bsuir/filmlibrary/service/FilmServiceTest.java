package by.bsuir.filmlibrary.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import by.bsuir.filmlibrary.dto.FilmCreateDTO;
import by.bsuir.filmlibrary.dto.FilmDTO;
import by.bsuir.filmlibrary.dto.FilmUpdateDTO;
import by.bsuir.filmlibrary.exception.DuplicateFilmException;
import by.bsuir.filmlibrary.exception.InvalidFilmDataException;
import by.bsuir.filmlibrary.exception.ResourceNotFoundException;
import by.bsuir.filmlibrary.mapper.FilmMapper;
import by.bsuir.filmlibrary.model.Film;
import by.bsuir.filmlibrary.model.Genre;
import by.bsuir.filmlibrary.repository.FilmRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Тесты сервисного слоя.
 * Spring не запускается: репозиторий - mock-объект Mockito,
 * маппер - настоящая реализация MapStruct, обёрнутая в @Spy.
 * Проверяется только бизнес-логика FilmService.
 */
@ExtendWith(MockitoExtension.class)
class FilmServiceTest {

    @Mock
    private FilmRepository filmRepository;

    @Spy
    private FilmMapper filmMapper = Mappers.getMapper(FilmMapper.class);

    @InjectMocks
    private FilmService filmService;

    private Film film;

    @BeforeEach
    void setUp() {
        film = new Film();
        film.setId(1L);
        film.setTitle("Interstellar");
        film.setDirector("Christopher Nolan");
        film.setReleaseYear(2014);
        film.setGenre(Genre.SCI_FI);
        film.setWatched(true);
        film.setRating(10);
    }

    private FilmCreateDTO buildCreateDto(boolean watched, Integer rating) {
        FilmCreateDTO dto = new FilmCreateDTO();
        dto.setTitle("Dune");
        dto.setDirector("Denis Villeneuve");
        dto.setReleaseYear(2021);
        dto.setGenre(Genre.SCI_FI);
        dto.setWatched(watched);
        dto.setRating(rating);
        return dto;
    }

    @Test
    @DisplayName("getAll без жанра: вызывается findAll")
    void getAllFilmsTest() {
        given(filmRepository.findAll()).willReturn(List.of(film));

        List<FilmDTO> result = filmService.getAll(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Interstellar");
        verify(filmRepository, never()).findByGenre(any());
    }

    @Test
    @DisplayName("getAll с жанром: вызывается findByGenre")
    void getFilmsByGenreTest() {
        given(filmRepository.findByGenre(Genre.SCI_FI)).willReturn(List.of(film));

        List<FilmDTO> result = filmService.getAll(Genre.SCI_FI);

        assertThat(result).hasSize(1);
        verify(filmRepository, never()).findAll();
    }

    @Test
    @DisplayName("getById: фильм найден и преобразован в DTO")
    void getFilmByIdTest() {
        given(filmRepository.findById(1L)).willReturn(Optional.of(film));

        FilmDTO result = filmService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Interstellar");
        assertThat(result.getRating()).isEqualTo(10);
    }

    @Test
    @DisplayName("getById: фильм не найден -> ResourceNotFoundException")
    void getFilmByIdNotFoundTest() {
        given(filmRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> filmService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("create: новый фильм сохраняется")
    void createFilmTest() {
        given(filmRepository.save(any(Film.class))).willAnswer(invocation -> {
            Film saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        FilmDTO result = filmService.create(buildCreateDto(true, 8));

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getTitle()).isEqualTo("Dune");
        assertThat(result.getRating()).isEqualTo(8);
        verify(filmRepository, times(1)).save(any(Film.class));
    }

    @Test
    @DisplayName("create: дубликат -> DuplicateFilmException, сохранения нет")
    void createDuplicateFilmTest() {
        given(filmRepository.existsByTitleIgnoreCaseAndReleaseYear("Dune", 2021)).willReturn(true);

        assertThatThrownBy(() -> filmService.create(buildCreateDto(false, null)))
                .isInstanceOf(DuplicateFilmException.class);

        verify(filmRepository, never()).save(any());
    }

    @Test
    @DisplayName("create: оценка непросмотренному фильму -> InvalidFilmDataException")
    void createRatingForUnwatchedFilmTest() {
        assertThatThrownBy(() -> filmService.create(buildCreateDto(false, 9)))
                .isInstanceOf(InvalidFilmDataException.class);

        verify(filmRepository, never()).save(any());
    }

    @Test
    @DisplayName("update: меняются только переданные поля")
    void updateFilmTest() {
        given(filmRepository.findById(1L)).willReturn(Optional.of(film));
        given(filmRepository.saveAndFlush(any(Film.class))).willAnswer(returnsFirstArg());

        FilmUpdateDTO dto = new FilmUpdateDTO();
        dto.setRating(9);
        dto.setReview("Пересмотрел");

        FilmDTO result = filmService.update(1L, dto);

        assertThat(result.getRating()).isEqualTo(9);
        assertThat(result.getReview()).isEqualTo("Пересмотрел");
        assertThat(result.getTitle()).isEqualTo("Interstellar");
        assertThat(result.getDirector()).isEqualTo("Christopher Nolan");
    }

    @Test
    @DisplayName("update: отметка «не просмотрен» сбрасывает оценку")
    void updateMarkUnwatchedResetsRatingTest() {
        given(filmRepository.findById(1L)).willReturn(Optional.of(film));
        given(filmRepository.saveAndFlush(any(Film.class))).willAnswer(returnsFirstArg());

        FilmUpdateDTO dto = new FilmUpdateDTO();
        dto.setWatched(false);

        FilmDTO result = filmService.update(1L, dto);

        assertThat(result.isWatched()).isFalse();
        assertThat(result.getRating()).isNull();
    }

    @Test
    @DisplayName("update: переименование в существующий фильм -> DuplicateFilmException")
    void updateToDuplicateTest() {
        given(filmRepository.findById(1L)).willReturn(Optional.of(film));
        given(filmRepository.existsByTitleIgnoreCaseAndReleaseYearAndIdNot("Dune", 2014, 1L)).willReturn(true);

        FilmUpdateDTO dto = new FilmUpdateDTO();
        dto.setTitle("Dune");

        assertThatThrownBy(() -> filmService.update(1L, dto))
                .isInstanceOf(DuplicateFilmException.class);

        verify(filmRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("delete: существующий фильм удаляется")
    void deleteFilmTest() {
        given(filmRepository.existsById(1L)).willReturn(true);

        filmService.delete(1L);

        verify(filmRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("delete: фильм не найден -> ResourceNotFoundException")
    void deleteFilmNotFoundTest() {
        given(filmRepository.existsById(99L)).willReturn(false);

        assertThatThrownBy(() -> filmService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(filmRepository, never()).deleteById(any());
    }
}
