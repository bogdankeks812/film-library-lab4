package by.bsuir.filmlibrary.controller;

import by.bsuir.filmlibrary.dto.FilmCreateDTO;
import by.bsuir.filmlibrary.dto.FilmDTO;
import by.bsuir.filmlibrary.dto.FilmUpdateDTO;
import by.bsuir.filmlibrary.model.Genre;
import by.bsuir.filmlibrary.service.FilmService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API фильмотеки. Вся логика вынесена в FilmService.
 */
@RestController
@RequestMapping("/api/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;

    /** GET /api/films или GET /api/films?genre=SCI_FI */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<FilmDTO> index(@RequestParam(name = "genre", required = false) Genre genre) {
        return filmService.getAll(genre);
    }

    /** GET /api/films/{id} */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public FilmDTO show(@PathVariable("id") Long id) {
        return filmService.getById(id);
    }

    /** POST /api/films */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FilmDTO create(@Valid @RequestBody FilmCreateDTO filmData) {
        return filmService.create(filmData);
    }

    /** PUT /api/films/{id} */
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public FilmDTO update(@PathVariable("id") Long id, @Valid @RequestBody FilmUpdateDTO filmData) {
        return filmService.update(id, filmData);
    }

    /** DELETE /api/films/{id} */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        filmService.delete(id);
    }
}
