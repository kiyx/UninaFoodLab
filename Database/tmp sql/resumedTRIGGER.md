&nbsp;Riepilogo dei Trigger per Tabella



1\. Partecipante



trg\_normalizza\_partecipante: Normalizza Username (minuscolo), CodiceFiscale (maiuscolo), Email (minuscolo), Nome e Cognome (iniziale maiuscola) prima dell'inserimento/aggiornamento.

trg\_unico\_username\_partecipante: Impedisce che un Username di un partecipante sia già usato da uno chef.

trg\_blocca\_aggiorna\_partecipante: Impedisce la modifica del campo CodiceFiscale dopo la creazione.

`NumeroCorsi` viene aggiornato dai trigger sulla tabella `Iscrizioni`.



2\. Chef



trg\_normalizza\_chef: Normalizza Username (minuscolo), CodiceFiscale (maiuscolo), Email (minuscolo), Nome e Cognome (iniziale maiuscola) prima dell'inserimento/aggiornamento.

trg\_unico\_username\_chef: Impedisce che un Username di uno chef sia già usato da un partecipante.

trg\_blocca\_aggiorna\_chef: Impedisce la modifica del campo CodiceFiscale dopo la creazione.



3\. Ingrediente



trg\_normalizza\_ingrediente: Normalizza il campo Nome (iniziale maiuscola) prima dell'inserimento/aggiornamento.

trg\_blocca\_aggiorna\_ingrediente: Impedisce la modifica o la cancellazione di IdIngrediente, Nome e Origine.



4\. Ricetta



trg\_normalizza\_ricetta: Normalizza il campo Nome (iniziale maiuscola) prima dell'inserimento/aggiornamento.

trg\_blocca\_aggiorna\_ricetta: Impedisce la modifica di IdRicetta e IdChef.



5\. Corso



trg\_blocca\_aggiorna\_corso: Impedisce la modifica di IdCorso, DataInizio, NumeroSessioni, FrequenzaSessioni, Costo o IdChef.



6\. SessionePratica



trg\_unicita\_sessione\_pratica\_giorno: Impedisce più sessioni (pratiche o online) per lo stesso corso nello stesso giorno.

trg\_sovrapposizione\_orario\_sessione\_pratica: Controlla che le sessioni dello stesso chef non si sovrappongano orariamente nello stesso giorno.

trg\_sessione\_pratica\_dopo\_inizio\_corso: Garantisce che la data della sessione non sia precedente alla data di inizio corso e che la prima sessione coincida con la data di inizio corso.

trg\_max\_sessioni\_pratica\*\*: Impedisce l'inserimento di nuove sessioni se è già raggiunto il numero massimo consentito.

trg\_verifica\_frequenza\_sessioni\_pratiche: Assicura che la data della sessione rispetti la frequenza definita per il corso.

trg\_ispratico\_insert\*\*: Impedisce l'inserimento di sessioni pratiche in corsi non contrassegnati come pratici.

trg\_blocca\_aggiorna\_sessioni\_pratiche: Impedisce la modifica di IdSessionePratica e IdCorso.

Il campo `NumeroPartecipanti` è aggiornato dai trigger su `Adesioni`.



7\. SessioneOnline



trg\_unicita\_sessione\_online\_giorno: Impedisce più sessioni (pratiche o online) per lo stesso corso nello stesso giorno.

trg\_sovrapposizione\_orario\_sessione\_online: Controlla che le sessioni dello stesso chef non si sovrappongano orariamente nello stesso giorno.

trg\_sessione\_online\_dopo\_inizio\_corso: Garantisce che la data della sessione non sia precedente alla data di inizio corso e che la prima sessione coincida con la data di inizio corso.

trg\_max\_sessioni\_online: Impedisce l'inserimento di nuove sessioni se è già raggiunto il numero massimo consentito.

trg\_verifica\_frequenza\_sessioni\_online: Assicura che la data della sessione rispetti la frequenza definita per il corso.

trg\_blocca\_aggiorna\_sessioni\_online: Impedisce la modifica di IdSessioneOnline e IdCorso.



&nbsp;8. Adesioni



trg\_incremento\_numutenti: Incrementa `NumeroPartecipanti` in `SessionePratica` all'inserimento di una nuova adesione.

trg\_decrementa\_num\_utenti: Decrementa `NumeroPartecipanti` in `SessionePratica` all'eliminazione di un'adesione.

trg\_data\_adesione: Assicura che la data dell'adesione sia antecedente alla data della sessione pratica.

trg\_iscrizione\_before\_adesione: Impedisce l'adesione a una sessione pratica se il partecipante non è iscritto al corso.

trg\_blocca\_aggiorna\_adesioni: Impedisce la modifica di IdPartecipante, IdSessionePratica, DataAdesione.

trg\_blocca\_cancella\_adesioni: Impedisce la cancellazione di un'adesione a meno di 3 giorni dalla sessione pratica.



&nbsp;9. Iscrizioni



trg\_incrementa\_num\_corsi: Incrementa `NumeroCorsi` del partecipante all'inserimento di una nuova iscrizione.

trg\_decrementa\_num\_corsi: Decrementa `NumeroCorsi` del partecipante all'eliminazione di un'iscrizione.

trg\_limite\_iscrizioni: Impedisce nuove iscrizioni se il limite di partecipanti per il corso è già raggiunto (solo per corsi pratici).

trg\_blocca\_aggiorna\_iscrizioni: Impedisce la modifica di IdPartecipante e IdCorso.



10\. Argomento



trg\_blocca\_aggiorna\_argomento: Impedisce la modifica o la cancellazione del campo Nome.



11\. Argomenti\_Corso



trg\_limit\_argomenti Limita a 5 il numero di argomenti per corso.

trg\_blocca\_aggiorna\_argomenticorso: Impedisce la modifica di IdCorso e IdArgomento.

trg\_blocca\_cancellazione\_argomento\_corso: Impedisce la cancellazione dell'ultimo argomento di un corso.



12\. Preparazioni



trg\_ricette\_chef\_sessione: Impedisce che uno chef usi ricette non proprie nelle sessioni pratiche.

trg\_blocca\_aggiorna\_preparazioni: Impedisce la modifica o la cancellazione di IdSessionePratica e IdRicetta.



13\. Utilizzi

trg\_blocca\_aggiorna\_utilizzi: Impedisce la modifica di IdRicetta e IdIngrediente.

