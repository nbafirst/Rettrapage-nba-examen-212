package cm.kfokam48.ticket.dto;

import cm.kfokam48.ticket.entity.StatutTicket;
import cm.kfokam48.ticket.entity.Ticket;
import java.time.LocalDateTime;

public record TicketResponse(
        Long id,
        String titre,
        String description,
        StatutTicket statut,
        LocalDateTime dateCreation
) {
    public static TicketResponse from(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getTitre(),
                ticket.getDescription(),
                ticket.getStatut(),
                ticket.getDateCreation()
        );
    }
}
