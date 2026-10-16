package cm.kfokam48.ticket.repository;

import cm.kfokam48.ticket.entity.StatutTicket;
import cm.kfokam48.ticket.entity.Ticket;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    boolean existsByTitreIgnoreCase(String titre);

    List<Ticket> findByStatutOrderByDateCreationDesc(StatutTicket statut);

    List<Ticket> findAllByOrderByDateCreationDesc();
}
