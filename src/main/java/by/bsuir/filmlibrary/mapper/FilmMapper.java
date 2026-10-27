package by.bsuir.filmlibrary.mapper;

import by.bsuir.filmlibrary.dto.FilmCreateDTO;
import by.bsuir.filmlibrary.dto.FilmDTO;
import by.bsuir.filmlibrary.dto.FilmUpdateDTO;
import by.bsuir.filmlibrary.model.Film;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

/**
 * Преобразование между сущностью Film и DTO.
 * Реализацию генерирует MapStruct при компиляции.
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface FilmMapper {

    /** DTO для создания -> сущность. */
    Film map(FilmCreateDTO dto);

    /** Сущность -> DTO для просмотра. */
    FilmDTO map(Film film);

    /** Обновление существующей сущности данными из DTO (null-поля пропускаются). */
    void update(FilmUpdateDTO dto, @MappingTarget Film film);
}
