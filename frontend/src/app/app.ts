import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { TicketApiService, Ticket, FiltreStatut } from './services/ticket-api.service';
import { ListeTickets } from './composants/liste-tickets/liste-tickets';
import { FormulaireTicket } from './composants/formulaire-ticket/formulaire-ticket';

/**
 * Conteneur : gère l'état de chargement, les tickets, le filtre,
 * les erreurs et le rafraîchissement. Les composants n'appellent pas l'API HTTP.
 */
@Component({
  selector: 'app-root',
  imports: [ListeTickets, FormulaireTicket],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  private readonly api = inject(TicketApiService);

  protected readonly filtres: FiltreStatut[] = ['Tous', 'OUVERT', 'EN_COURS', 'FERME'];

  protected readonly filtre = signal<FiltreStatut>('Tous');
  protected readonly tickets = signal<Ticket[]>([]);
  protected readonly chargement = signal(true);
  protected readonly erreur = signal<string | null>(null);

  protected readonly idFermetureEnCours = signal<number | null>(null);
  protected readonly erreurFermeture = signal<string | null>(null);

  protected readonly compteurTicket = signal(0);

  ngOnInit(): void {
    this.charger();
  }

  /** Les branches succès et erreur terminent toutes deux l'état de chargement. */
  protected charger(): void {
    this.chargement.set(true);
    this.erreur.set(null);

    this.api.lister(this.filtre()).subscribe({
      next: (tickets) => {
        this.tickets.set(tickets);
        this.chargement.set(false);
      },
      error: (ex: HttpErrorResponse) => {
        const err = this.api.extraireErreur(ex);
        this.erreur.set(err.message);
        this.chargement.set(false);
      }
    });
  }

  protected changerFiltre(nouveau: FiltreStatut): void {
    if (nouveau === this.filtre()) return;
    this.filtre.set(nouveau);
    this.charger();
  }

  /** Après une création réussie : rafraîchir la liste sans recharger la page. */
  protected rafraichir(): void {
    this.compteurTicket.update(n => n + 1);
    this.charger();
  }

  protected fermer(id: number): void {
    this.erreurFermeture.set(null);
    this.idFermetureEnCours.set(id);

    this.api.fermer(id).subscribe({
      next: () => {
        this.idFermetureEnCours.set(null);
        this.compteurTicket.update(n => n + 1);
        this.charger();
      },
      error: (ex: HttpErrorResponse) => {
        const err = this.api.extraireErreur(ex);
        this.idFermetureEnCours.set(null);
        this.erreurFermeture.set(err.message);
      }
    });
  }
}
