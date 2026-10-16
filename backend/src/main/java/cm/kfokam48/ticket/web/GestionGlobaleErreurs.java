package cm.kfokam48.ticket.web;

import cm.kfokam48.ticket.exception.TicketDejaFermeException;
import cm.kfokam48.ticket.exception.TicketIntrouvableException;
import cm.kfokam48.ticket.exception.TitreDejaUtiliseException;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Gestion globale des erreurs : conversion validation/métier -> HTTP cohérent.
 * Aucune stack trace n'est exposée au client.
 */
@RestControllerAdvice
public class GestionGlobaleErreurs {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErreur> validation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getDefaultMessage() == null ? err.getField() + " invalide" : err.getDefaultMessage())
                .toList();
        ApiErreur erreur = new ApiErreur(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Requête invalide",
                details.isEmpty() ? "Requête invalide" : details.get(0),
                details
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erreur);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiErreur> validationDirecte(HandlerMethodValidationException ex) {
        ApiErreur erreur = new ApiErreur(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Requête invalide",
                "Paramètre de requête invalide",
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erreur);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErreur> parametreInvalide(MethodArgumentTypeMismatchException ex) {
        String nom = ex.getName() == null ? "paramètre" : ex.getName();
        ApiErreur erreur = new ApiErreur(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Requête invalide",
                "Valeur invalide pour le paramètre « " + nom + " ». Valeurs attendues : OUVERT, EN_COURS ou FERME.",
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erreur);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErreur> corpsIllisible(HttpMessageNotReadableException ex) {
        ApiErreur erreur = new ApiErreur(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Requête invalide",
                "Le corps de la requête est absent ou illisible (JSON attendu).",
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erreur);
    }

    @ExceptionHandler(TicketIntrouvableException.class)
    public ResponseEntity<ApiErreur> introuvable(TicketIntrouvableException ex) {
        ApiErreur erreur = new ApiErreur(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                "Ticket introuvable",
                ex.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erreur);
    }

    @ExceptionHandler({TitreDejaUtiliseException.class, TicketDejaFermeException.class})
    public ResponseEntity<ApiErreur> conflit(RuntimeException ex) {
        ApiErreur erreur = new ApiErreur(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                "Conflit",
                ex.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(erreur);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErreur> inattendue(Exception ex) {
        ApiErreur erreur = new ApiErreur(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erreur interne",
                "Une erreur interne est survenue côté serveur.",
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erreur);
    }
}
