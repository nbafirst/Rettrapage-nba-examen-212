import { Component, signal, output, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { TicketApiService } from '../../services/ticket-api.service';

/**
 * Formulaire de création : titre obligatoire, description facultative.
 * Les refus 400/409 du serveur sont affichés à côté du formulaire.
 * Le titre n'est limité côté client : le serveur doit refuser au-delà de 100 caractères.
 */
@Component({
  selector: 'app-formulaire-ticket',
  imports: [FormsModule],
  templateUrl: './formulaire-ticket.html',
  styleUrl: './formulaire-ticket.css'
})
export class FormulaireTicket {
  private readonly api = inject(TicketApiService);

  /** Émis après chaque création réussie : le conteneur rafraîchit la liste. */
  readonly ticketCree = output<void>();

  protected readonly titre = signal('');
  protected readonly description = signal('');
  protected readonly soumissionEnCours = signal(false);
  protected readonly erreurCreation = signal<string | null>(null);

  /** Bouton désactivé tant que le titre est vide ou composé d'espaces. */
  protected get peutSoumettre(): boolean {
    return this.titre().trim().length > 0 && !this.soumissionEnCours();
  }

  protected soumettre(): void {
    const titre = this.titre().trim();
    this.erreurCreation.set(null);
    this.soumissionEnCours.set(true);

    this.api.creer(titre, this.description().trim()).subscribe({
      next: () => {
        this.soumissionEnCours.set(false);
        this.titre.set('');
        this.description.set('');
        this.ticketCree.emit();
      },
      error: (ex: HttpErrorResponse) => {
        this.soumissionEnCours.set(false);
        this.erreurCreation.set(this.api.extraireErreur(ex).message);
      }
    });
  }
}
