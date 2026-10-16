package cm.kfokam48.ticket.web;

import cm.kfokam48.ticket.dto.TicketCreateRequest;
import cm.kfokam48.ticket.dto.TicketResponse;
import cm.kfokam48.ticket.entity.StatutTicket;
import cm.kfokam48.ticket.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Routes HTTP uniquement : délégation au service, aucune logique métier.
 */
@RestController
@RequestMapping("/api/tickets")
@Tag(name = "Tickets", description = "Gestion des tickets (épreuve rattrapage)")
public class TicketController {

    private final TicketService service;

    public TicketController(TicketService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Créer un ticket", description = "RG-01 : statut OUVERT et dateCreation générés par le serveur. RG-02 : titre unique.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ticket créé"),
            @ApiResponse(responseCode = "400", description = "Titre absent, vide ou > 100 caractères"),
            @ApiResponse(responseCode = "409", description = "RG-02 : titre déjà utilisé")
    })
    public ResponseEntity<TicketResponse> creer(@Valid @RequestBody TicketCreateRequest requete) {
        TicketResponse cree = TicketResponse.from(service.creer(requete.titre(), requete.description()));
        return ResponseEntity.status(HttpStatus.CREATED).body(cree);
    }

    @GetMapping
    @Operation(summary = "Lister les tickets", description = "Paramètre statut facultatif : OUVERT, EN_COURS ou FERME.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des tickets (éventuellement vide)")
    })
    public List<TicketResponse> lister(
            @Parameter(description = "Filtrer par statut (facultatif)")
            @RequestParam(required = false) StatutTicket statut) {
        return service.lister(statut).stream().map(TicketResponse::from).toList();
    }

    @PatchMapping("/{id}/fermer")
    @Operation(summary = "Fermer un ticket", description = "RG-03 : un ticket FERME ne change plus d'état.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket fermé"),
            @ApiResponse(responseCode = "404", description = "Ticket introuvable"),
            @ApiResponse(responseCode = "409", description = "RG-03 : ticket déjà FERME")
    })
    public ResponseEntity<TicketResponse> fermer(
            @Parameter(description = "Identifiant du ticket")
            @PathVariable Long id) {
        return ResponseEntity.ok(TicketResponse.from(service.fermer(id)));
    }
}
