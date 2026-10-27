package by.bsuir.filmlibrary.dto;

import by.bsuir.filmlibrary.model.Genre;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO для отображения фильма (то, что API возвращает клиенту).
 */
@Getter
@Setter
public class FilmDTO {

    private Long id;
    private String title;
    private String director;
    private Integer releaseYear;
    private Genre genre;
    private Integer rating;
    private String review;
    private boolean watched;
    private LocalDate createdAt;
    private LocalDate updatedAt;
}
