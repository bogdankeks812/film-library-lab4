package by.bsuir.filmlibrary.exception;

/** Такой фильм уже есть в фильмотеке -> HTTP 409. */
public class DuplicateFilmException extends RuntimeException {
    public DuplicateFilmException(String message) {
        super(message);
    }
}
