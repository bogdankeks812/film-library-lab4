package by.bsuir.filmlibrary.exception;

/** Данные нарушают бизнес-правило -> HTTP 400. */
public class InvalidFilmDataException extends RuntimeException {
    public InvalidFilmDataException(String message) {
        super(message);
    }
}
