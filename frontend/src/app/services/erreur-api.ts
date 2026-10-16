/** Format d'erreur stable renvoyé par le backend (GestionGlobaleErreurs). */
export interface ApiErreur {
  horodatage?: string;
  statut: number;
  erreur: string;
  message: string;
  details?: string[] | null;
}
