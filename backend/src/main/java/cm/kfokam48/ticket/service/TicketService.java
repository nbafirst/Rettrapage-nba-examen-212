package cm.kfokam48.ticket.service;

import cm.kfokam48.ticket.entity.StatutTicket;
import cm.kfokam48.ticket.entity.Ticket;
import cm.kfokam48.ticket.exception.TicketDejaFermeException;
import cm.kfokam48.ticket.exception.TicketIntrouvableException;
import cm.kfokam48.ticket.exception.TitreDejaUtiliseException;
import cm.kfokam48.ticket.repository.TicketRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Règles métier (RG-01, RG-02, RG-03) et transactions. Aucune logique HTTP ici.
 */
@Service
public class TicketService {

    private final TicketRepository repository;

    public TicketService(TicketRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Ticket creer(String titre, String description) {
        if (repository.existsByTitreIgnoreCase(titre.trim())) {
            throw new TitreDejaUtiliseException(titre.trim());
        }
        Ticket ticket = new Ticket();
        ticket.setTitre(titre.trim());
        ticket.setDescription(description != null && !description.isBlank() ? description.trim() : null);
        // RG-01 : statut initial OUVERT, dateCreation générée côté serveur.
        ticket.setStatut(StatutTicket.OUVERT);
        ticket.setDateCreation(LocalDateTime.now());
        return repository.save(ticket);
    }

    @Transactional(readOnly = true)
    public List<Ticket> lister(StatutTicket statut) {
        if (statut == null) {
            return repository.findAllByOrderByDateCreationDesc();
        }
        return repository.findByStatutOrderByDateCreationDesc(statut);
    }

    @Transactional
    public Ticket fermer(Long id) {
        Ticket ticket = repository.findById(id)
                .orElseThrow(() -> new TicketIntrouvableException(id));
        // RG-03 : un ticket FERME ne change plus d'état.
        if (ticket.getStatut() == StatutTicket.FERME) {
            throw new TicketDejaFermeException(id);
        }
        ticket.setStatut(StatutTicket.FERME);
        return repository.save(ticket);
    }

    /**
     * Passe un ticket OUVERT en EN_COURS. Idempotent sur un ticket déjà EN_COURS.
     * Un ticket FERME ne revient jamais en arrière (RG-03).
     */
    @Transactional
    public Ticket marquerEnCours(Long id) {
        Ticket ticket = repository.findById(id)
                .orElseThrow(() -> new TicketIntrouvableException(id));
        if (ticket.getStatut() == StatutTicket.FERME) {
            throw new TicketDejaFermeException(id);
        }
        ticket.setStatut(StatutTicket.EN_COURS);
        return repository.save(ticket);
    }
}
