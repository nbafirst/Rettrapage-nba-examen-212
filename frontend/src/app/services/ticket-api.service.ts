import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, firstValueFrom } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiErreur } from './erreur-api';

/** Réflexions du DTO de sortie du backend. */
export interface Ticket {
  id: number;
  titre: string;
  description: string | null;
  statut: 'OUVERT' | 'EN_COURS' | 'FERME';
  dateCreation: string;
}

export type FiltreStatut = 'Tous' | 'OUVERT' | 'EN_COURS' | 'FERME';

/**
 * Service API isolé : les composants n'appellent jamais HttpClient directement.
 * Préserve le statut HTTP et extrait le message structuré du serveur.
 */
@Injectable({ providedIn: 'root' })
export class TicketApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/tickets`;

  lister(statut: FiltreStatut): Observable<Ticket[]> {
    const url = statut === 'Tous' ? this.baseUrl : `${this.baseUrl}?statut=${statut}`;
    return this.http.get<Ticket[]>(url);
  }

  creer(titre: string, description: string): Observable<Ticket> {
    return this.http.post<Ticket>(this.baseUrl, { titre, description });
  }

  fermer(id: number): Observable<Ticket> {
    return this.http.patch<Ticket>(`${this.baseUrl}/${id}/fermer`, {});
  }

  /** Extrait le message structuré du serveur en préservant le statut HTTP. */
  extraireErreur(ex: HttpErrorResponse): ApiErreur {
    const corps = ex.error as ApiErreur | null;
    if (corps && typeof corps.message === 'string' && corps.message.length > 0) {
      return { statut: ex.status, erreur: corps.erreur ?? 'Erreur', message: corps.message, details: corps.details ?? null };
    }
    if (ex.status === 0) {
      return { statut: 0, erreur: 'Serveur injoignable', message: 'Le serveur est injoignable. Vérifiez que le backend est démarré, puis réessayez.' };
    }
    return { statut: ex.status, erreur: 'Erreur', message: `Erreur ${ex.status} : ${ex.statusText}` };
  }
}
