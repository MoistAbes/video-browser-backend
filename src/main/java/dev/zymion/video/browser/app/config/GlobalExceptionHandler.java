package dev.zymion.video.browser.app.config;

import dev.zymion.video.browser.app.exceptions.*;
import dev.zymion.video.browser.app.models.dto.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDto> handleAuthException(AuthenticationException ex) {
        ErrorResponseDto error = new ErrorResponseDto("Niepoprawne dane uwierzytelniające", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUserNotFound(UsernameNotFoundException ex) {
        ErrorResponseDto error = new ErrorResponseDto("Użytkownik nie istnieje", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        ErrorResponseDto error = new ErrorResponseDto("User already exists", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleRoleNotFoundException(RoleNotFoundException ex) {
        ErrorResponseDto error = new ErrorResponseDto("Role not found", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ShowNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleShowNotFound(ShowNotFoundException ex) {
        ErrorResponseDto error = new ErrorResponseDto("Show not found", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(GenreNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleGenreNotFound(GenreNotFoundException ex) {
        ErrorResponseDto error = new ErrorResponseDto("Genre not found" ,ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(UserDisabledException.class)
    public ResponseEntity<ErrorResponseDto> handleUserDisabled(UserDisabledException ex) {
        ErrorResponseDto error = new ErrorResponseDto("Konto użytkownika jest nieaktywne", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(WrongUsernameOrPasswordException.class)
    public ResponseEntity<ErrorResponseDto> handleWrongUsernameOrPasswordException(WrongUsernameOrPasswordException ex) {
        ErrorResponseDto error = new ErrorResponseDto(
                "Niepoprawna nazwa użytkownika lub hasło",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

}

