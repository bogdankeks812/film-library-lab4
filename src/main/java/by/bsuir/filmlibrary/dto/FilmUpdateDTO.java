package by.bsuir.filmlibrary.dto;

import by.bsuir.filmlibrary.model.Genre;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO для частичного обновления фильма.
 * Поля, которые не переданы (null), остаются без изменений.
 */
@Getter
@Setter
public class FilmUpdateDTO {

    @Size(min = 1, message = "Название не может быть пустым")
    private String title;

    @Size(min = 1, message = "Режиссёр не может быть пустым")
    private String director;

    @Min(value = 1888, message = "Год выпуска не может быть раньше 1888")
    @Max(value = 2100, message = "Год выпуска указан некорректно")
    private Integer releaseYear;

    private Genre genre;

    @Min(value = 1, message = "Оценка от 1 до 10")
    @Max(value = 10, message = "Оценка от 1 до 10")
    private Integer rating;

    private String review;

    private Boolean watched;
}
