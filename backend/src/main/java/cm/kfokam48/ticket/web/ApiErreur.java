package cm.kfokam48.ticket.web;

import java.time.Instant;
import java.util.List;

/**
 * Format d'erreur stable consommé par le frontend.
 */
public record ApiErreur(
        Instant horodatage,
        int statut,
        String erreur,
        String message,
        List<String> details
) {
}
