package by.bsuir.filmlibrary.dto;

import by.bsuir.filmlibrary.model.Genre;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO для добавления фильма.
 */
@Getter
@Setter
public class FilmCreateDTO {

    @NotBlank(message = "Название обязательно")
    private String title;

    @NotBlank(message = "Режиссёр обязателен")
    private String director;

    @NotNull(message = "Год выпуска обязателен")
    @Min(value = 1888, message = "Год выпуска не может быть раньше 1888")
    @Max(value = 2100, message = "Год выпуска указан некорректно")
    private Integer releaseYear;

    @NotNull(message = "Жанр обязателен")
    private Genre genre;

    @Min(value = 1, message = "Оценка от 1 до 10")
    @Max(value = 10, message = "Оценка от 1 до 10")
    private Integer rating;

    private String review;

    private boolean watched;
}
