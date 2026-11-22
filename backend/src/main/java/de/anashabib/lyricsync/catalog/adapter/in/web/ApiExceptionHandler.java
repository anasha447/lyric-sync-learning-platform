package de.anashabib.lyricsync.catalog.adapter.in.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.anashabib.lyricsync.catalog.application.port.out.AudioStorageException;
import de.anashabib.lyricsync.catalog.domain.validation.ValidationError;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

/**
 * Turns every error into one shape, RFC 9457 problem details:
 * {@code {"type", "title", "status", "detail", "errors": [{"field" | "line", "message"}]}}.
 * Spring's base class already covers its own exceptions (405, 415, …) the same way.
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    /** One problem in a request: a form field, a line of the .lrc file, or the whole file (neither). */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Problem(String field, Integer line, String message) {
    }

    /** 400: fields missing, too long or the wrong type. Every field is listed, not just the first. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail problem = ex.getBody();
        problem.setTitle("Invalid request");
        problem.setDetail("Some fields are missing or invalid");
        problem.setProperty("errors", ex.getFieldErrors().stream()
                .map(e -> new Problem(e.getField(), null,
                        // a failed conversion ("three minutes" for a number) has a long technical message
                        e.isBindingFailure() ? "has the wrong format" : e.getDefaultMessage()))
                .toList());
        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    /** 413: the multipart limit in application.yaml was hit before our code even ran. */
    @Override
    protected ResponseEntity<Object> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail problem = ex.getBody();
        problem.setDetail("The upload is larger than 20 MB");
        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    /** 500, but with a useful title. The message may contain file paths, so it goes to the log only. */
    @ExceptionHandler(AudioStorageException.class)
    ProblemDetail audioStorageFailed(AudioStorageException ex) {
        logger.error("Storing audio failed", ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "The audio could not be stored, so nothing was saved. Please try again.");
        problem.setTitle("Audio storage failed");
        return problem;
    }

    /** Anything else is our bug: log everything, show nothing internal. */
    @ExceptionHandler(Exception.class)
    ProblemDetail unexpected(Exception ex) {
        logger.error("Unexpected error", ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Something went wrong on our side. It has been logged.");
        problem.setTitle("Internal error");
        return problem;
    }

    /** 422: the request was well-formed, but its content breaks the rules for a song. */
    static ProblemDetail songRejected(List<ValidationError> errors) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT,
                "The song can't be added: " + errors.size() + (errors.size() == 1 ? " problem" : " problems"));
        problem.setTitle("Song rejected");
        problem.setProperty("errors", errors.stream()
                .map(e -> new Problem(null, e.lineIndex(), e.message()))
                .toList());
        return problem;
    }
}
