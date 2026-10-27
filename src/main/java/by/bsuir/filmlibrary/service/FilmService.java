package by.bsuir.filmlibrary.service;

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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Бизнес-логика фильмотеки.
 * Правила:
 *  1) нельзя добавить два фильма с одинаковым названием и годом выпуска;
 *  2) оценку можно поставить только просмотренному фильму;
 *  3) если фильм отмечен как непросмотренный, его оценка сбрасывается.
 */
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmRepository filmRepository;
    private final FilmMapper filmMapper;

    @Transactional(readOnly = true)
    public List<FilmDTO> getAll(Genre genre) {
        List<Film> films = (genre == null)
                ? filmRepository.findAll()
                : filmRepository.findByGenre(genre);
        return films.stream()
                .map(filmMapper::map)
                .toList();
    }

    @Transactional(readOnly = true)
    public FilmDTO getById(Long id) {
        return filmMapper.map(findFilm(id));
    }

    @Transactional
    public FilmDTO create(FilmCreateDTO dto) {
        if (filmRepository.existsByTitleIgnoreCaseAndReleaseYear(dto.getTitle(), dto.getReleaseYear())) {
            throw new DuplicateFilmException(
                    "Фильм '" + dto.getTitle() + "' (" + dto.getReleaseYear() + ") уже есть в фильмотеке");
        }
        if (!dto.isWatched() && dto.getRating() != null) {
            throw new InvalidFilmDataException("Оценку можно поставить только просмотренному фильму");
        }
        Film film = filmMapper.map(dto);
        Film saved = filmRepository.save(film);
        return filmMapper.map(saved);
    }

    @Transactional
    public FilmDTO update(Long id, FilmUpdateDTO dto) {
        Film film = findFilm(id);

        String newTitle = dto.getTitle() != null ? dto.getTitle() : film.getTitle();
        Integer newYear = dto.getReleaseYear() != null ? dto.getReleaseYear() : film.getReleaseYear();
        if (filmRepository.existsByTitleIgnoreCaseAndReleaseYearAndIdNot(newTitle, newYear, id)) {
            throw new DuplicateFilmException(
                    "Фильм '" + newTitle + "' (" + newYear + ") уже есть в фильмотеке");
        }

        filmMapper.update(dto, film);

        if (!film.isWatched()) {
            if (dto.getRating() != null) {
                throw new InvalidFilmDataException("Оценку можно поставить только просмотренному фильму");
            }
            film.setRating(null);
        }

        Film saved = filmRepository.saveAndFlush(film);
        return filmMapper.map(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!filmRepository.existsById(id)) {
            throw new ResourceNotFoundException("Фильм с id=" + id + " не найден");
        }
        filmRepository.deleteById(id);
    }

    private Film findFilm(Long id) {
        return filmRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Фильм с id=" + id + " не найден"));
    }
}
