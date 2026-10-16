package cm.kfokam48.ticket.exception;

/**
 * RG-03 : un ticket FERME ne change plus d'état.
 */
public class TicketDejaFermeException extends RuntimeException {

    public TicketDejaFermeException(Long id) {
        super("RG-03 : le ticket " + id + " est déjà FERME. Un ticket fermé ne change plus d'état.");
    }
}
