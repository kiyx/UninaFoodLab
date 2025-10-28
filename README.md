# UninaFoodLab 🍲

<p align="center">
  <img src="Object Orientation/uninafoodlab/src/main/resources/logo_finestra.png" alt="UninaFoodLab Logo" width="200"/>
</p>

<p align="center">
  <strong>Sistema di Gestione di Corsi di Cucina Online</strong>
  <br>
  Progetto universitario per i corsi di Object Orientation e Basi di Dati
  <br>
  Università degli Studi di Napoli Federico II
</p>

## 📝 Descrizione

**UninaFoodLab** è un'applicazione desktop per la gestione di una piattaforma di corsi di cucina. È stata sviluppata come progetto per i corsi di "Object Orientation" e "Basi di Dati" (Gruppo OOBD7).

Il sistema permette a due tipi principali di utenti, **Chef** e **Partecipanti**, di interagire con la piattaforma. Gli Chef possono creare e gestire corsi, ricette e sessioni (sia pratiche che online), mentre i Partecipanti possono iscriversi ai corsi, consultare le ricette e partecipare alle sessioni.

## ✨ Funzionalità Principali

* **Autenticazione Utente**: Registrazione e Login sicuri per Chef e Partecipanti.
* **Gestione Corsi**: Creazione, modifica e visualizzazione dei corsi con filtri di ricerca.
* **Gestione Ricette**: Gli Chef possono creare e gestire le proprie ricette, specificando ingredienti e passaggi.
* **Gestione Sessioni**: Creazione di sessioni pratiche e online associate ai corsi.
* **Profilo Utente**: Visualizzazione e modifica delle informazioni del proprio profilo.
* **Reporting**: Funzionalità di reportistica per gli amministratori.

## 🏗️ Architettura

Il progetto segue un'architettura multi-strato per separare le responsabilità:

* **Boundary** (Package `UninaFoodLab.Boundary`): Contiene tutte le classi dell'interfaccia utente (GUI) realizzate in Java Swing.
* **Controller** (Package `UninaFoodLab.Controller`): Gestisce la logica applicativa e fa da mediatore tra l'interfaccia utente e l'accesso ai dati.
* **DTO (Data Transfer Object)** (Package `UninaFoodLab.DTO`): Classi POJO (Plain Old Java Object) che modellano le entità del dominio.
* **DAO (Data Access Object)** (Package `UninaFoodLab.DAO`): Interfacce e implementazioni concrete per l'accesso e la persistenza dei dati sul database PostgreSQL.

## 🛠️ Tecnologie Utilizzate

* **Linguaggio**: Java
* **Interfaccia Grafica**: Java Swing, SwingX, Ikonli (per le icone)
* **Database**: PostgreSQL
* **Gestione Dipendenze**: Apache Maven
* **Layout Manager**: MigLayout

## 🚀 Setup e Avvio

Per eseguire il progetto in locale, segui questi passaggi:

### 1. Database

1.  Assicurati di avere un server PostgreSQL in esecuzione.
2.  Crea un nuovo database (es. `uninafoodlab`).
3.  Esegui gli script SQL forniti nella cartella `Database/sql/` nell'ordine corretto:
    1.  `00_Create.sql` (per creare tabelle e tipi)
    2.  `01_Trigger_Funzioni_Procedure.sql` (per la logica di business del DB)
    3.  `02_Popolamento.sql` (per inserire dati di esempio)

### 2. Applicazione Java

1.  Apri il progetto (`Object Orientation/uninafoodlab/`) in un IDE che supporta Maven (es. Eclipse, IntelliJ IDEA).
2.  Compila il progetto e risolvi le dipendenze Maven.
3.  Esegui la classe main ( `Controller.java`).

*Oppure, tramite terminale nella cartella `Object Orientation/uninafoodlab/`:*

```bash
# Compila e installa le dipendenze
mvn clean install

# Esegui l'applicazione (assicurati che il mainClass sia configurato nel pom.xml)
mvn exec:java
