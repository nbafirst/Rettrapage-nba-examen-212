package cm.kfokam48.ticket.exception;

/**
 * Ticket introuvable en base.
 */
public class TicketIntrouvableException extends RuntimeException {

    public TicketIntrouvableException(Long id) {
        super("Le ticket " + id + " est introuvable.");
    }
}
