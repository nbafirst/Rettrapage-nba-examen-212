# 🎫 Gestion des Tickets — Épreuve rattrapage (Module Ticket)

Application full stack de gestion de tickets : **Spring Boot + PostgreSQL (Docker) + Angular**.
Épreuve de rattrapage KFOKAM48 — Batch 2, Phase 3.

---

## 🧩 Stack technique

| Couche     | Technologie                                        |
|------------|----------------------------------------------------|
| Frontend   | Angular (standalone, signaux, `provideHttpClient`)  |
| Backend    | Spring Boot — Web, Data JPA, Validation, OpenAPI    |
| Base       | PostgreSQL 16 dans Docker (volume persistant)       |
| API docs   | Swagger UI (springdoc-openapi)                      |
| Build      | Maven (wrapper `./mvnw`), Java 21                   |

---

## 🚀 Démarrage rapide

### 1. Base de données (Docker)

```bash
docker compose up -d
```

PostgreSQL écoute sur le port hôte **5433** (base `ticketdb`, utilisateur `ticket`).

### 2. Backend (port 8081)

```bash
cd backend
./mvnw spring-boot:run
```

> **Note Java :** le projet cible **Java 21**. Si votre `java -version` est plus ancien,
> précisez le JDK 21 :
> ```bash
> JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./mvnw spring-boot:run
> ```

Swagger : [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)

### 3. Frontend (port 4200)

```bash
cd frontend
npm install
npm start
```

Application : [http://localhost:4200](http://localhost:4200)

L'URL du backend est centralisée dans `frontend/src/environments/environment.ts`.

---

## 🔌 API

Base URL : `http://localhost:8081/api/tickets`

| Verbe  | Chemin                    | Succès | Erreurs      | Rôle                                             |
|--------|---------------------------|--------|--------------|--------------------------------------------------|
| POST   | `/api/tickets`            | 201    | 400, 409     | Créer (titre + description)                      |
| GET    | `/api/tickets`            | 200    | 400          | Lister, filtre `?statut=` facultatif             |
| PATCH  | `/api/tickets/{id}/en-cours` | 200 | 404, 409     | Marquer **EN_COURS** (idempotent)                |
| PATCH  | `/api/tickets/{id}/fermer`| 200    | 404, 409     | Fermer                                            |

### Exemples

```bash
# Créer
curl -X POST http://localhost:8081/api/tickets \
  -H 'Content-Type: application/json' \
  -d '{"titre":"Serveur en panne","description":"connexions refusées"}'

# Lister / filtrer
curl http://localhost:8081/api/tickets
curl 'http://localhost:8081/api/tickets?statut=EN_COURS'

# Marquer en cours
curl -X PATCH http://localhost:8081/api/tickets/1/en-cours

# Fermer
curl -X PATCH http://localhost:8081/api/tickets/1/fermer
```

---

## 📏 Règles de gestion

- **RG-01** — Le titre est **obligatoire**, **100 caractères max**. À la création, le statut est
  **OUVERT** et `dateCreation` est **générée par le serveur** (jamais fournie par le client).
- **RG-02** — Deux tickets ne peuvent pas avoir le **même titre** → **HTTP 409** avec message
  nommant `RG-02`.
- **RG-03** — Un ticket ne peut être fermé que s'il est **OUVERT** ou **EN_COURS**. Un ticket
  **FERME ne change plus d'état** → **HTTP 409** avec message nommant `RG-03`.

L'endpoint `en-cours` applique la même protection : un ticket **FERME** ne repasse jamais en
arrière (**409 RG-03**) ; l'appel sur un ticket déjà **EN_COURS** est **idempotent** (200).

### Codes d'erreur

| Cas                                   | HTTP | Message                          |
|---------------------------------------|------|----------------------------------|
| Titre absent / vide / > 100 caractères | 400  | « Le titre est obligatoire » / « …100 caractères » |
| Titre déjà utilisé                     | 409  | « RG-02 : … déjà utilisé … »     |
| Statut `?statut=` invalide             | 400  | « Valeurs attendues : OUVERT, EN_COURS ou FERME » |
| Identifiant inconnu                    | 404  | « Le ticket {id} est introuvable » |
| Fermeture / repassement d'un FERME     | 409  | « RG-03 : … déjà FERME … »       |

Réponse d'erreur stable (format `ApiErreur`) :
```json
{
  "horodatage": "2026-10-16T15:01:29.784Z",
  "statut": 409,
  "erreur": "Conflit",
  "message": "RG-02 : le titre « … » est déjà utilisé par un autre ticket.",
  "details": null
}
```
Aucune stack trace n'est exposée au client.

---

## 🗂 Architecture

### Backend (`backend/src/main/java/cm/kfokam48/ticket/`)

```
entity/       Ticket (JPA), StatutTicket (enum : OUVERT, EN_COURS, FERME)
repository/   TicketRepository (Spring Data JPA)
service/      TicketService — règles métier, transactions (aucune logique HTTP)
web/          TicketController (routes uniquement), ApiErreur, GestionGlobaleErreurs
dto/          TicketCreateRequest (entrée), TicketResponse (sortie)
exception/    TicketIntrouvableException, TitreDejaUtiliseException, TicketDejaFermeException
```

- **Aucune logique métier dans le contrôleur.**
- **L'entité JPA n'est jamais renvoyée directement** : le DTO de sortie (`TicketResponse`)
  et le DTO d'entrée (`TicketCreateRequest` : titre + description uniquement) sont distincts.
- CORS autorise explicitement l'origine du frontend (`http://localhost:4200`).

### Frontend (`frontend/src/app/`)

```
services/ticket-api.service.ts   Service API dédié — tous les appels HTTP sont ici
services/erreur-api.ts           Format d'erreur structuré
composants/formulaire-ticket/    Saisie + création, affiche les refus 400/409
composants/liste-tickets/        Tableau + badges + actions (Commencer / Fermer)
app.ts / app.html / app.css      Conteneur : état, filtre, erreurs, rafraîchissement
environments/environment.ts      URL du backend
```

- **Aucun `HttpClient` dans un composant** : tout passe par `TicketApiService`.
- **Les 4 états obligatoires** sont gérés : chargement, données, **liste vide**
  (« Aucun ticket »), **erreur** (message + bouton « Réessayer »).

---

## 🧪 Tests & vérifications

### Backend

```bash
cd backend && ./mvnw test
```

### Frontend

```bash
cd frontend && npx ng build
```

### Scénarios vérifiés (curl + Chrome headless)

| Scénario                                                     | Résultat  |
|--------------------------------------------------------------|-----------|
| POST valide → 201, statut OUVERT, dateCreation serveur       | ✅        |
| POST titre absent / espaces → 400                            | ✅        |
| POST titre > 100 caractères → 400                            | ✅        |
| POST doublon → 409 (RG-02)                                   | ✅        |
| GET sans filtre / avec `?statut=` → 200                      | ✅        |
| GET `?statut=INVALIDE` → 400                                 | ✅        |
| PATCH `/{id}/en-cours` OUVERT → 200 (EN_COURS)               | ✅        |
| PATCH `/{id}/en-cours` déjà EN_COURS → 200 (idempotent)      | ✅        |
| PATCH `/{id}/en-cours` id inconnu → 404                      | ✅        |
| PATCH `/{id}/en-cours` sur FERME → 409 (RG-03)               | ✅        |
| PATCH `/{id}/fermer` OUVERT → 200                            | ✅        |
| PATCH `/{id}/fermer` déjà FERME → 409 (RG-03)                | ✅        |
| CORS préflight OPTIONS → 200                                 | ✅        |
| Frontend : bouton « Commencer » → ticket EN_COURS            | ✅        |
| Frontend : backend arrêté → « serveur injoignable » + Réessayer | ✅     |

### Vérification SQL

Un ticket créé par HTTP est retrouvé dans la table réelle :

```bash
docker compose exec db psql -U ticket -d ticketdb \
  -c "SELECT id, titre, description, statut, date_creation FROM ticket ORDER BY date_creation DESC;"
```

---

## 🎨 Interface

Le frontend propose :
- Un **formulaire** de création avec désactivation du bouton tant que le titre est vide,
  état de progression pendant l'envoi, et affichage des refus serveur **à côté du formulaire**
  (jamais de `alert()`).
- Un **filtre par statut** (Tous / OUVERT / EN_COURS / FERME) avec compteurs, où « Tous »
  appelle la liste sans filtre.
- Des **actions conditionnelles** : « Commencer » uniquement sur un ticket **OUVERT**,
  « Fermer » sur **OUVERT** ou **EN_COURS**, avec **confirmation** avant l'appel.
- Quatre états explicites : chargement (spinner), données, **liste vide**, **erreur** avec
  possibilité de **réessayer**.
- Un thème soigné (variables CSS, dégradés, badges à pastille, animations respectant
  `prefers-reduced-motion`).

---

## 📸 Captures

Les captures d'écran exigées sont dans [`docs/captures/`](docs/captures/) :
- `capture-1-swagger-3-endpoints.png` — Swagger avec les endpoints documentés
- `capture-2-doublon-409-rg02.png` — refus HTTP 409 (RG-02) affiché dans l'interface

---

## 📁 Déploiement local — résumé

| Service    | URL / port                           |
|------------|--------------------------------------|
| PostgreSQL | `localhost:5433` (Docker)            |
| Backend    | `http://localhost:8081`              |
| Swagger    | `http://localhost:8081/swagger-ui.html` |
| Frontend   | `http://localhost:4200`              |
