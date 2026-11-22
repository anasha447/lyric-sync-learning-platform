package de.anashabib.lyricsync.catalog.adapter.in.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.anashabib.lyricsync.catalog.domain.validation.ValidationError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.RestControllerAdvice;
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
