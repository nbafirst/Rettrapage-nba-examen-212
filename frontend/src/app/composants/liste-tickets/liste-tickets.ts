import { Component, input, output } from '@angular/core';
import { Ticket } from '../../services/ticket-api.service';

/**
 * Composant liste : colonnes titre, statut, date de création, action.
 * Le bouton Fermer n'apparaît que si le statut est OUVERT ou EN_COURS.
 * Confirmation demandée avant chaque fermeture.
 */
@Component({
  selector: 'app-liste-tickets',
  imports: [],
  templateUrl: './liste-tickets.html',
  styleUrl: './liste-tickets.css'
})
export class ListeTickets {
  readonly tickets = input.required<Ticket[]>();
  readonly idFermetureEnCours = input<number | null>(null);
  readonly erreurFermeture = input<string | null>(null);

  readonly fermerTicket = output<number>();
  readonly marquerEnCours = output<number>();

  protected statuts = ['OUVERT', 'EN_COURS', 'FERME'] as const;

  protected peutFermer(statut: Ticket['statut']): boolean {
    return statut === 'OUVERT' || statut === 'EN_COURS';
  }

  protected peutDemarrer(statut: Ticket['statut']): boolean {
    return statut === 'OUVERT';
  }

  protected cliquerFermer(ticket: Ticket): void {
    if (window.confirm(`Fermer définitivement le ticket « ${ticket.titre} » ?`)) {
      this.fermerTicket.emit(ticket.id);
    }
  }

  protected formaterDate(iso: string): string {
    return new Date(iso).toLocaleString('fr-FR');
  }
}
