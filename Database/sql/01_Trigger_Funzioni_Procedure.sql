-- 01_Triggers_Functions_Procedures

-- Normalizzazione basica dei dati di Partecipante/Chef/Ricetta/Ingrediente

-- Funzione che normalizza i campi testuale dell'utente:
-- Username, Email -> Minuscolo
-- CodiceFiscale -> Maiuscolo
-- Nome, Cognome, Luogo -> iniziale maiuscola

CREATE OR REPLACE FUNCTION fun_normalizza_utente()
RETURNS TRIGGER AS
$$
BEGIN
    NEW.Username := LOWER(NEW.Username);
    NEW.Nome := INITCAP(NEW.Nome);
    NEW.Cognome := INITCAP(NEW.Cognome);
    NEW.CodiceFiscale := UPPER(NEW.CodiceFiscale);
    NEW.LuogoDiNascita := INITCAP(NEW.LuogoDiNascita);
    NEW.Email := LOWER(NEW.Email);

    RETURN NEW;

END;
$$ LANGUAGE plpgsql;

-- Applica la normalizzazione prima di INSERT o UPDATE su Partecipante
CREATE TRIGGER trg_normalizza_partecipante
BEFORE INSERT OR UPDATE ON Partecipante
FOR EACH ROW
EXECUTE FUNCTION fun_normalizza_utente();

-- Applica la normalizzazione prima di INSERT o UPDATE su Chef
CREATE TRIGGER trg_normalizza_chef
BEFORE INSERT OR UPDATE ON Chef
FOR EACH ROW
EXECUTE FUNCTION fun_normalizza_utente();


-- Normalizzo Ingrediente e Ricetta con la prima lettera maiuscola

CREATE OR REPLACE FUNCTION fun_normalizza_ricetta_ingr_chef()
RETURNS TRIGGER AS
$$
BEGIN
    NEW.Nome := INITCAP(NEW.Nome);
    RETURN NEW;

END;
$$ LANGUAGE plpgsql;

-- Applica INITCAP al Nome di Ingrediente
CREATE TRIGGER trg_normalizza_ingrediente
BEFORE INSERT OR UPDATE ON Ingrediente
FOR EACH ROW
EXECUTE FUNCTION fun_normalizza_ricetta_ingr_chef();

-- Applica INITCAP al Nome di Ricetta
CREATE TRIGGER trg_normalizza_ricetta
BEFORE INSERT OR UPDATE ON Ricetta
FOR EACH ROW
EXECUTE FUNCTION fun_normalizza_ricetta_ingr_chef();

-----------------------------------------------------------------------------------------------------------------------

-- Interrelazionale: Impedire che due utenti – uno Chef e uno Partecipante – 
-- abbiano lo stesso Username, anche se sono in due tabelle diverse.

CREATE OR REPLACE FUNCTION fun_username_unique()
RETURNS TRIGGER AS
$$
BEGIN
	-- Se è un UPDATE e l' username non cambia, salto il controllo
	IF TG_OP = 'UPDATE' AND NEW.Username = OLD.Username THEN			
    		RETURN NEW;
	END IF;

	-- Se stiamo inserendo o modificando un Partecipante, controlla che l'username non sia già usato da uno CHEF
	IF TG_TABLE_NAME = 'partecipante' THEN
    		IF EXISTS (SELECT 1 FROM Chef WHERE Username = NEW.Username) THEN
    			RAISE EXCEPTION 'Username già usato in Chef';
		END IF;
	END IF;
	
	-- Se stiamo inserendo o modificando uno Chef, controlla che l'username non sia già usato da un Partecipante
	IF TG_TABLE_NAME = 'chef' THEN
		IF EXISTS (SELECT 1 FROM Partecipante WHERE Username = NEW.Username) THEN
    			RAISE EXCEPTION 'Username già usato in Partecipante';
		END IF;
	END IF;
	RETURN NEW;

END;
$$ LANGUAGE plpgsql;

-- Trigger che garantiscono unicità dello username tra le due tabelle (vincolo nato dalla ristrutturazione)
CREATE TRIGGER trg_unico_username_partecipante
BEFORE INSERT OR UPDATE ON Partecipante 
FOR EACH ROW
EXECUTE FUNCTION fun_username_unique();

CREATE TRIGGER trg_unico_username_chef
BEFORE INSERT OR UPDATE ON Chef 
FOR EACH ROW
EXECUTE FUNCTION fun_username_unique();

-----------------------------------------------------------------------------------------------------------------------

-- Gestione numero sessioni del corso automatica

-- Blocca l'inserimento di un corso con NumeroSessioni diverso da 0, il numero è gestito dal db automaticamente

CREATE OR REPLACE FUNCTION fun_setta_numero_sessioni_corsi_iniziali()
RETURNS TRIGGER AS 
$$ 
BEGIN 
		IF NEW.NumeroSessioni <> 0 THEN
			RAISE EXCEPTION 'Il corso deve partire con numero di sessioni 0! Vengono aggiornate automaticamente';
		END IF;
        RETURN NEW;
END; 
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_setta_numero_sessioni_corsi_iniziali
BEFORE INSERT ON Corso
FOR EACH ROW 
EXECUTE FUNCTION fun_setta_numero_sessioni_corsi_iniziali();


-- Aggiornamento numero sessioni (incremento quando viene creata una nuova sessione di qualsiasi tipo)

CREATE OR REPLACE FUNCTION fun_incrementa_num_sessioni()
RETURNS TRIGGER AS
$$
BEGIN
        UPDATE Corso
        SET NumeroSessioni = NumeroSessioni + 1
        WHERE IdCorso = NEW.IdCorso;
        RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Incremento automatico all'inserimento di una sessione (Online/Pratica)
CREATE TRIGGER trg_incremento_numsessioni_online
AFTER INSERT ON SessioneOnline
FOR EACH ROW
EXECUTE FUNCTION fun_incrementa_num_sessioni();

CREATE TRIGGER trg_incremento_numsessioni_pratiche
AFTER INSERT ON SessionePratica
FOR EACH ROW
EXECUTE FUNCTION fun_incrementa_num_sessioni();


-- Aggiornamento numero sessioni (decremento quando viene eliminata una nuova sessione di qualsiasi tipo)
-- Protezione con GREATEST per evitare valori negativi

CREATE OR REPLACE FUNCTION fun_decrementa_num_sessioni()
RETURNS TRIGGER AS
$$
BEGIN
    IF EXISTS (SELECT 1 FROM Corso WHERE IdCorso = OLD.IdCorso) THEN
        UPDATE Corso
        SET NumeroSessioni = GREATEST(NumeroSessioni - 1, 0)
        WHERE IdCorso = OLD.IdCorso;
    END IF;
	RETURN OLD;
END;
$$ LANGUAGE plpgsql;


-- Decremento automatico alla rimozione di una sessione (Online/Pratica)
CREATE TRIGGER trg_decrementa_num_sessioni_pratica
AFTER DELETE ON SessionePratica
FOR EACH ROW
EXECUTE FUNCTION fun_decrementa_num_sessioni();

CREATE TRIGGER trg_decrementa_num_sessioni_online
AFTER DELETE ON SessioneOnline
FOR EACH ROW
EXECUTE FUNCTION fun_decrementa_num_sessioni();

-----------------------------------------------------------------------------------------------------------------------

-- Gestione sessioni pratiche del corso automatica

-- Blocca l'inserimento di una sessione pratica se il corso non è pratico
--(poteva essere impostato automaticamente a true all'inserimento di una sessione pratica, ma non sarebbe stato possibile inserire il limite corrispondente)

CREATE OR REPLACE FUNCTION fun_ispratico_insert()
RETURNS TRIGGER AS
$$
DECLARE
	pratico Corso.isPratico%TYPE;
BEGIN
	SELECT isPratico INTO pratico FROM Corso WHERE IdCorso = NEW.IdCorso;

	IF NOT pratico THEN
		RAISE EXCEPTION 'Non puoi inserire una sessione pratica in un corso non pratico!!!';
	END IF;
	RETURN NEW;
	
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_ispratico_insert
BEFORE INSERT ON SessionePratica
FOR EACH ROW
EXECUTE FUNCTION fun_ispratico_insert();


-- Blocca l'inserimento di una sessione con NumeroPartecipanti diverso da 0, il numero è gestito dal db automaticamente

CREATE OR REPLACE FUNCTION fun_setta_numero_partecipanti_iniziali()
RETURNS TRIGGER AS 
$$ 
BEGIN 
		IF NEW.NumeroPartecipanti <> 0 THEN
			RAISE EXCEPTION 'La Sessione pratica deve partire con numero di partecipanti 0!  Vengono aggiornati automaticamente';
		END IF;
        RETURN NEW;
END; 
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_setta_numero_partecipanti_iniziali
BEFORE INSERT ON SessionePratica
FOR EACH ROW 
EXECUTE FUNCTION fun_setta_numero_partecipanti_iniziali();


-- Verifica che l'adesione sia registrata alla data corrente e previene inserimenti retrodatati o futuri

CREATE OR REPLACE FUNCTION fun_setta_data_adesione()
RETURNS TRIGGER AS
$$
BEGIN
        IF NEW.DataAdesione <> CURRENT_DATE THEN
            RAISE EXCEPTION 'Se inserisci una adesione alla sessione pratica allora la sua data deve essere quella di oggi';
        END IF;
        RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_setta_data_adesione
BEFORE INSERT ON Adesioni
FOR EACH ROW
EXECUTE FUNCTION fun_setta_data_adesione();


-- Incrementa il numero partecipanti di una sessione pratica al momento dell'adesione di un partecipante

CREATE OR REPLACE FUNCTION fun_incrementa_num_utenti()
RETURNS TRIGGER AS
$$    
BEGIN
    UPDATE SessionePratica
    SET NumeroPartecipanti = NumeroPartecipanti + 1
    WHERE IdSessionePratica = NEW.IdSessionePratica;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_incremento_numutenti
AFTER INSERT ON Adesioni
FOR EACH ROW
EXECUTE FUNCTION fun_incrementa_num_utenti();


-- Decrementa il numero partecipanti di una sessione pratica al momento dell'adesione di un partecipante con protezione per non andare in negativo

CREATE OR REPLACE FUNCTION fun_decrementa_num_utenti()
RETURNS TRIGGER AS
$$
BEGIN
	UPDATE SessionePratica
	SET NumeroPartecipanti = GREATEST(NumeroPartecipanti - 1, 0)
	WHERE IdSessionePratica = OLD.IdSessionePratica;
	RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_decrementa_num_utenti
AFTER DELETE ON Adesioni
FOR EACH ROW
EXECUTE FUNCTION fun_decrementa_num_utenti();


-----------------------------------------------------------------------------------------------------------------------
-- Gestione sessioni (frequenza e orari)

-- Impedisce più sessioni (pratiche o online) dello stesso corso nello stesso giorno
-- Controlla entrambe le tabelle SessionePratica e SessioneOnline per evitare duplicati o sovrapposizioni
-- Escludiamo i conflitti (Potremmo considerare la sessione stessa in update e EXISTS ci restituirebbe true)

CREATE OR REPLACE FUNCTION fun_unicita_sessione_giorno()
RETURNS TRIGGER AS
$$
BEGIN
		
		IF TG_TABLE_NAME = 'sessionepratica' THEN
			IF EXISTS 
			(   -- Verifica se esiste già una sessione pratica con stessa data e corso, diversa dalla riga in update
        		SELECT 1 FROM SessionePratica
        		WHERE Data = NEW.Data
          		AND IdCorso = NEW.IdCorso
          		AND NOT (TG_OP = 'UPDATE' AND IdSessionePratica = NEW.IdSessionePratica)
    		) 
			THEN RAISE EXCEPTION 'Esiste già una sessione pratica per questo corso in data %.', NEW.Data;
			END IF;		
			IF EXISTS 
			(   -- Verifica se esiste già una sessione online con stessa data e corso
        		SELECT 1 FROM SessioneOnline
        		WHERE Data = NEW.Data
          		AND IdCorso = NEW.IdCorso
    		) 
			THEN RAISE EXCEPTION 'Esiste già una sessione online per questo corso in data %.', NEW.Data;
			END IF;
		ELSE
			 IF EXISTS 
			 (	-- Verifica se esiste già una sessione online con stessa data e corso, diversa dalla riga in update
        		SELECT 1 FROM SessioneOnline
        		WHERE Data = NEW.Data
          		AND IdCorso = NEW.IdCorso
          		AND NOT (TG_OP = 'UPDATE' AND IdSessioneOnline = NEW.IdSessioneOnline)
    		) 
			THEN RAISE EXCEPTION 'Esiste già una sessione online per questo corso in data %.', NEW.Data;
			END IF;
			IF EXISTS 
			(	-- Verifica se esiste già una sessione pratica con stessa data e corso
        		SELECT 1 FROM SessionePratica
        		WHERE Data = NEW.Data
          		AND IdCorso = NEW.IdCorso
    		) 
			THEN RAISE EXCEPTION 'Esiste già una sessione pratica per questo corso in data %.', NEW.Data;
			END IF;
		END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_unicita_sessione_online_giorno
BEFORE INSERT OR UPDATE OF Data ON SessioneOnline
FOR EACH ROW
EXECUTE FUNCTION fun_unicita_sessione_giorno();

CREATE TRIGGER trg_unicita_sessione_pratica_giorno
BEFORE INSERT OR UPDATE OF Data ON SessionePratica
FOR EACH ROW
EXECUTE FUNCTION fun_unicita_sessione_giorno();


-- Controlla che la data della sessione:
-- 1- Non sia precedente alla data di inizio del corso
-- 2- Se è la prima sessione di quel corso, deve coincidere con la data di inizio corso 

CREATE OR REPLACE FUNCTION fun_sessione_dopo_inizio_corso()
RETURNS TRIGGER AS
$$
DECLARE
	data_inizio Corso.DataInizio%TYPE;
BEGIN
	SELECT DataInizio INTO data_inizio FROM Corso WHERE IdCorso = NEW.IdCorso;
    
	IF NEW.Data < data_inizio THEN
		RAISE EXCEPTION 'La sessione non può essere precedente alla data di inizio del corso';
	END IF;

	 -- Se non esistono sessioni per il corso, la prima deve essere esattamente la data di inizio corso
	IF NOT EXISTS
				(
				   SELECT 1 FROM SessionePratica WHERE IdCorso = NEW.IdCorso
				   UNION
				   SELECT 1 FROM SessioneOnline WHERE IdCorso = NEW.IdCorso
				)
	THEN 
		IF NEW.Data <> data_inizio THEN
			RAISE EXCEPTION 'La prima sessione del corso deve essere il giorno della Data di inizio del corso';
		END IF;
	END IF;

	RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_sessione_pratica_dopo_inizio_corso
BEFORE INSERT ON SessionePratica
FOR EACH ROW
EXECUTE FUNCTION fun_sessione_dopo_inizio_corso();

CREATE TRIGGER trg_sessione_online_dopo_inizio_corso
BEFORE INSERT ON SessioneOnline
FOR EACH ROW
EXECUTE FUNCTION fun_sessione_dopo_inizio_corso();


-- Verifica della frequenza all'inserimento o aggiornamento della data di una sessione

CREATE OR REPLACE FUNCTION fun_verifica_frequenza_sessioni()
RETURNS TRIGGER AS
$$
DECLARE
    frequenza Corso.FrequenzaSessioni%TYPE;  -- Frequenza del corso
    giorniFrequenza INTEGER;                 -- Numero di giorni che corrisponde alla frequenza.
    v_dataInizio DATE;                       -- Data di inizio del corso
    finestra_nuova INTEGER;                  -- Indice della finestra temporale per la nuova data.
    finestra_vecchia INTEGER;                -- Indice della finestra temporale per la vecchia data (UPDATE).
    id_sessione_corrente INTEGER;            -- ID della sessione in corso di modifica (per auto-esclusione).
BEGIN
    -- Recupera i dati del corso
    SELECT FrequenzaSessioni, DataInizio
    INTO frequenza, v_dataInizio
    FROM Corso
    WHERE IdCorso = NEW.IdCorso;

    -- La data della sessione non può essere antecedente alla data di inizio del corso
    IF NEW.Data < v_dataInizio THEN
        RAISE EXCEPTION 'La data della sessione (%) non può essere precedente alla data di inizio del corso (%).', NEW.Data, v_dataInizio;
    END IF;

    -- Se la frequenza è 'Libera', non sono necessari ulteriori controlli
    IF frequenza = 'Libera' THEN
        RETURN NEW;
    END IF;

    -- Converte la frequenza testuale in un numero di giorni per il calcolo delle finestre temporali
    CASE frequenza
        WHEN 'Giornaliera' THEN giorniFrequenza := 1;
        WHEN 'Settimanale' THEN giorniFrequenza := 7;
        WHEN 'Bisettimanale' THEN giorniFrequenza := 14;
        WHEN 'Mensile' THEN giorniFrequenza := 30;
        ELSE
            RAISE EXCEPTION 'Frequenza non riconosciuta: %', frequenza;
    END CASE;

    -- Calcola l'indice della finestra per la nuova data della sessione.
    -- (DataSessione - DataInizioCorso) / 7 giorni -> 0 per la prima settimana, 1 per la seconda, ...

    finestra_nuova := FLOOR((NEW.Data - v_dataInizio) / giorniFrequenza);

    -- Se la sessione viene modificata ma rimane nella stessa finestra temporale,
    -- l'operazione è valida e non sono necessari altri controlli
    IF TG_OP = 'UPDATE' THEN
        finestra_vecchia := FLOOR((OLD.Data - v_dataInizio) / giorniFrequenza);
        IF finestra_nuova = finestra_vecchia THEN
            RETURN NEW;
        END IF;
    END IF;

    -- Identifica la sessione corrente per escluderla dal conflitto durante un UPDATE
    IF TG_TABLE_NAME = 'sessionepratica' THEN
        id_sessione_corrente := NEW.IdSessionePratica;
    ELSE
        id_sessione_corrente := NEW.IdSessioneOnline;
    END IF;

    -- Verifica se esiste già un'altra sessione nella finestra temporale di destinazione
    IF EXISTS 
    (
        -- Unifico le sessioni
        SELECT 1
        FROM (
                SELECT Data, IdSessionePratica AS id, 'SessionePratica' as tipo FROM SessionePratica WHERE IdCorso = NEW.IdCorso
                UNION ALL
                SELECT Data, IdSessioneOnline AS id, 'SessioneOnline' as tipo FROM SessioneOnline WHERE IdCorso = NEW.IdCorso
             ) AS tutte_le_sessioni

        -- Trovo le sessioni che cadono nella stessa finestra temporale calcolata
        WHERE FLOOR((tutte_le_sessioni.Data - v_dataInizio) / giorniFrequenza) = finestra_nuova

        -- Esclude la sessione stessa dal controllo in caso di UPDATE
        AND NOT (TG_OP = 'UPDATE' AND tutte_le_sessioni.id = id_sessione_corrente AND tutte_le_sessioni.tipo = TG_TABLE_NAME)
    ) 
    THEN
        RAISE EXCEPTION 'La finestra temporale per la data % è già occupata da un''altra sessione.', NEW.Data;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_verifica_frequenza_sessioni_pratiche
BEFORE INSERT OR UPDATE OF Data ON SessionePratica
FOR EACH ROW
EXECUTE FUNCTION fun_verifica_frequenza_sessioni();

CREATE TRIGGER trg_verifica_frequenza_sessioni_online
BEFORE INSERT OR UPDATE OF Data ON SessioneOnline
FOR EACH ROW
EXECUTE FUNCTION fun_verifica_frequenza_sessioni();


-- Se una sessione associata al corso viene cancellata, impostiamo la frequenza del corso a libera 
--(questo perchè vogliamo mantenere un controllo della frequenza senza violazioni ed evitare buchi)

CREATE OR REPLACE FUNCTION fun_gestisci_frequenza_dopo_eliminazione()
RETURNS TRIGGER AS
$$
BEGIN
    -- Controlla se la frequenza del corso non è già 'Libera'
    -- per evitare update ridondante
    IF (SELECT FrequenzaSessioni FROM Corso WHERE IdCorso = OLD.IdCorso) <> 'Libera' THEN
       		UPDATE Corso
        	SET FrequenzaSessioni = 'Libera'
      		WHERE IdCorso = OLD.IdCorso;
    END IF;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;


CREATE TRIGGER trg_gestisci_frequenza_dopo_eliminazione_pratica
AFTER DELETE ON SessionePratica
FOR EACH ROW
EXECUTE FUNCTION fun_gestisci_frequenza_dopo_eliminazione();


CREATE TRIGGER trg_gestisci_frequenza_dopo_eliminazione_online
AFTER DELETE ON SessioneOnline
FOR EACH ROW
EXECUTE FUNCTION fun_gestisci_frequenza_dopo_eliminazione();


-- Controlliamo, all'inserimento o modifica di una sessione, che non ci siano altre sessioni (di altri corsi) che siano in sovrapposizione temporale

CREATE OR REPLACE FUNCTION fun_controlla_sovrapposizione_orario_sessione()
RETURNS TRIGGER AS
$$
DECLARE
	-- Recupera l'id dello chef del corso
    chef Chef.IdChef%TYPE;

	-- Calcola orario di inizio e fine della nuova sessione
    inizio_nuova TIME := NEW.Orario;
    fine_nuova TIME := (NEW.Orario + (NEW.Durata || ' minutes')::interval)::time;

	 -- Memorizza id e tipo della sessione corrente (pratica o online)
    id_sessione_corrente INTEGER;
	sessione_corrente TEXT;
BEGIN

	-- Determina il tipo di sessione in base alla tabella
    IF TG_TABLE_NAME = 'sessionepratica' THEN
        id_sessione_corrente := NEW.IdSessionePratica;
		sessione_corrente := 'sessionepratica';
    ELSIF TG_TABLE_NAME = 'sessioneonline' THEN
        id_sessione_corrente := NEW.IdSessioneOnline;
		sessione_corrente := 'sessioneonline';
    END IF;

	-- Recupera lo chef del corso
    SELECT IdChef INTO chef FROM Corso WHERE IdCorso = NEW.IdCorso;

	-- Controlla se esiste una sessione dello stesso chef, nello stesso giorno, con orario sovrapposto
    IF EXISTS 
       (
            SELECT 1 FROM 
            (
				-- Sessioni pratiche
                SELECT SP.Data, SP.Orario, SP.Durata, C.IdChef, SP.IdSessionePratica AS IdSessione, 'sessionepratica' AS TipoSessione
                FROM SessionePratica SP JOIN Corso C ON SP.IdCorso = C.IdCorso
                WHERE C.IdChef = chef AND SP.Data = NEW.Data

                UNION ALL

				-- Sessioni online
                SELECT SO.Data, SO.Orario, SO.Durata, C.IdChef, SO.IdSessioneOnline AS IdSessione, 'sessioneonline' AS TipoSessione
                FROM SessioneOnline SO JOIN Corso C ON SO.IdCorso = C.IdCorso
                WHERE C.IdChef = chef AND SO.Data = NEW.Data
            ) AS S

			-- Esclude la sessione stessa nel caso di UPDATE
            WHERE NOT (TG_OP = 'UPDATE' AND S.TipoSessione = sessione_corrente AND S.IdSessione = id_sessione_corrente)

			-- Verifica sovrapposizione oraria: inizio < fine esistente e fine > inizio esistente
			AND NOT ((S.Orario + (S.Durata || ' minutes')::interval)::time <= inizio_nuova OR S.Orario >= fine_nuova)
    ) THEN
        RAISE EXCEPTION 'Sovrapposizione oraria con altra sessione dello stesso chef';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_sovrapposizione_orario_sessione_pratica
BEFORE INSERT OR UPDATE OF Data, Orario, Durata ON SessionePratica
FOR EACH ROW
EXECUTE FUNCTION fun_controlla_sovrapposizione_orario_sessione();

CREATE TRIGGER trg_sovrapposizione_orario_sessione_online
BEFORE INSERT OR UPDATE OF Data, Orario, Durata ON SessioneOnline
FOR EACH ROW
EXECUTE FUNCTION fun_controlla_sovrapposizione_orario_sessione();

-----------------------------------------------------------------------------------------------------------------------

-- Controlla all'insert sulla tabella ponte che gli argomenti del corso non siano essere più di 5

CREATE OR REPLACE FUNCTION fun_limit_argomenti()
RETURNS TRIGGER AS
$$
DECLARE
    	num_argomenti INTEGER;
BEGIN
	
	SELECT COUNT(*) INTO num_argomenti FROM Argomenti_Corso WHERE IdCorso = NEW.IdCorso;
	IF num_argomenti >= 5 THEN
		RAISE EXCEPTION 'E'' gia'' stato scelto il numero massimo di argomenti per questo corso'; 
    	END IF;
    	RETURN NEW;

END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_limit_argomenti
BEFORE INSERT OR UPDATE ON Argomenti_Corso
FOR EACH ROW
EXECUTE FUNCTION fun_limit_argomenti();

-----------------------------------------------------------------------------------------------------------------------

-- Controlla che se viene inserita una iscrizione ma il limite di iscrizioni è già raggiunto, essa non viene inserita

CREATE OR REPLACE FUNCTION fun_limite_iscrizioni()
RETURNS TRIGGER AS
$$
DECLARE
    	numero_iscritti INTEGER;
    	limite_corso Corso.Limite%TYPE;
		pratico Corso.isPratico%TYPE;
BEGIN
	
	-- Ottiene il limite e il tipo del corso associato all'iscrizione
	SELECT Limite, isPratico INTO limite_corso, pratico
    FROM Corso
    WHERE IdCorso = NEW.IdCorso;

	-- Il controllo viene fatto solo per i corsi pratici (gli unici che hanno il limite)
	IF pratico THEN
			-- Conta quante iscrizioni esistono già per quel corso
    		SELECT COUNT(*) INTO numero_iscritti
    		FROM Iscrizioni
    		WHERE IdCorso = NEW.IdCorso;
    	
			-- Se è stato raggiunto il limite massimo, blocca l'inserimento
    		IF numero_iscritti >= limite_corso THEN
        		RAISE EXCEPTION 'Il limite delle iscrizioni per il corso è stato già raggiunto';
    		END IF;
	END IF;
    	RETURN NEW;

END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_limite_iscrizioni 
BEFORE INSERT ON Iscrizioni
FOR EACH ROW
EXECUTE FUNCTION fun_limite_iscrizioni();

-----------------------------------------------------------------------------------------------------------------------

-- Un utente non puo' partecipare a una sessione pratica se non iscritto al corso che la organizza

CREATE OR REPLACE FUNCTION fun_iscrizione_before_adesione()
RETURNS TRIGGER AS
$$
DECLARE
    	idcorso_sessione SessionePratica.IdCorso%TYPE;
BEGIN

		-- Recupero l'id del corso dalla sessionepratica associata
    	SELECT IdCorso INTO idcorso_sessione
    	FROM SessionePratica
    	WHERE IdSessionePratica = NEW.IdSessionePratica;

		-- Se non esiste l'iscrizione del partecipante per quel corso, blocca l'iscrizione
		IF NOT EXISTS ( SELECT 1 FROM Iscrizioni WHERE IdPartecipante = NEW.IdPartecipante AND IdCorso = idcorso_sessione ) THEN
    		RAISE EXCEPTION 'Partecipante non iscritto al corso, impossibile aderire alla sessione pratica';
		END IF;
    	RETURN NEW;

END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_iscrizione_before_adesione
BEFORE INSERT ON Adesioni
FOR EACH ROW
EXECUTE FUNCTION fun_iscrizione_before_adesione();


-- Interrelazionale: La data dell'adesione alla sessione pratica deve essere antecedente (di almeno 3 giorni) alla data della sessione pratica.
-- Se la sessione è troppo vicina o già avvenuta, l'adesione viene bloccata.

CREATE OR REPLACE FUNCTION fun_data_adesione()
RETURNS TRIGGER AS
$$
DECLARE
    	Data_Sessione SessionePratica.Data%TYPE;
BEGIN
		-- Recupero la data della sessione pratica
    	SELECT Data INTO Data_Sessione
    	FROM SessionePratica
    	WHERE IdSessionePratica = NEW.IdSessionePratica;

		-- Se mancano meno di 3 giorni tra adesione e la data della sessione, blocca l'adesione
    	IF (Data_Sessione - NEW.DataAdesione < 3) THEN
        	RAISE EXCEPTION 'L'' adesione deve essere antecedente (3 giorni) alla data della sessione pratica';
    	END IF;

    	RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_data_adesione
BEFORE INSERT ON Adesioni
FOR EACH ROW
EXECUTE FUNCTION fun_data_adesione();

-----------------------------------------------------------------------------------------------------------------------

-- Verifica che lo chef della ricetta sia lo stesso chef che organizza il corso della sessione pratica.
-- Impedisce che ricette di altri chef vengano assegnate a sessioni non loro.

CREATE OR REPLACE FUNCTION fun_check_corso_chef()
RETURNS TRIGGER AS
$$
DECLARE 
    ChefRicetta Chef.IdChef%TYPE;
    ChefCorso Chef.IdChef%TYPE;
BEGIN
	
	-- Ottengo lo chef associato alla ricetta
    SELECT IdChef 
    INTO ChefRicetta
    FROM Ricetta R
    WHERE R.IdRicetta = NEW.IdRicetta;

	-- Ottengo lo chef associato al corso della sessione pratica
    SELECT IdChef
    INTO ChefCorso
    FROM SessionePratica SP JOIN Corso C ON SP.IdCorso = C.IdCorso
    WHERE SP.IdSessionePratica = NEW.IdSessionePratica;

	-- Confronta i due chef e se sono diversi blocca l'inserimento della ricetta nella sessione
    IF ChefRicetta <> ChefCorso
        THEN RAISE EXCEPTION 'Lo chef della ricetta e lo chef che organizza il corso non sono gli stessi!';
	END IF;
    
	RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_check_corso_chef
BEFORE INSERT ON Preparazioni
FOR EACH ROW
EXECUTE FUNCTION fun_check_corso_chef();

-----------------------------------------------------------------------------------------------------------------------

-- Blocco di update di attributi e delete su tabelle 

-----------------------------------------------------------------------------------------------------------------------

-- Blocca la modifica del codice fiscale una volta che è stato inserito

CREATE OR REPLACE FUNCTION fun_blocca_aggiorna_utente()
RETURNS TRIGGER AS
$$
BEGIN
	RAISE EXCEPTION 'Non puoi modificare il tuo codice fiscale!!!';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_blocca_aggiorna_chef
BEFORE UPDATE OF CodiceFiscale ON Chef 
FOR EACH ROW
EXECUTE FUNCTION fun_blocca_aggiorna_utente();

CREATE TRIGGER trg_blocca_aggiorna_partecipante
BEFORE UPDATE OF CodiceFiscale ON Partecipante
FOR EACH ROW
EXECUTE FUNCTION fun_blocca_aggiorna_utente();


-----------------------------------------------------------------------------------------------------------------------

-- Impedisce l'eliminazione di un partecipante se ha aderito a una sessione pratica imminente (entro 3 giorni).
-- Protegge la coerenza delle adesioni poco prima della sessione e impedisce gli sprechi.

CREATE OR REPLACE FUNCTION fun_delete_partecipante_con_adesione()
RETURNS TRIGGER AS
$$
BEGIN
	-- Controlla se esiste una adesione del partecipante che dista a meno di 3 giorni dalla data di oggi
	IF EXISTS(SELECT 1
		      FROM Adesioni NATURAL JOIN SessionePratica
		      WHERE IdPartecipante = OLD.IdPartecipante AND (Data - CURRENT_DATE) < 3)
	THEN RAISE EXCEPTION 'Non puoi eliminare un partecipante che ha effettuato un''adesione per una sessione pratica a meno di 3 giorni prima della data';
	END IF;

	RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_delete_partecipante_con_adesione
BEFORE DELETE ON Partecipante
FOR EACH ROW
EXECUTE FUNCTION fun_delete_partecipante_con_adesione();

-----------------------------------------------------------------------------------------------------------------------

-- Restituisce TRUE se un corso è considerato "attivo":
--   1. Ha almeno un iscritto
--   2. La data odierna è tra DataInizio e la data dell'ultima sessione (o prima della DataInizio)

CREATE OR REPLACE FUNCTION corso_attivo(id_corso_in INTEGER)
RETURNS BOOLEAN AS
$$
DECLARE
	data_inizio_corso Corso.DataInizio%TYPE;
	data_fine_corso Corso.DataInizio%TYPE;
	num_iscritti INT;
BEGIN
	-- Recupera la data di inizio del corso
	SELECT DataInizio INTO data_inizio_corso FROM Corso WHERE IdCorso = id_corso_in;

	-- Calcola la data dell'ultima sessione
	SELECT MAX(Data) INTO data_fine_corso
     	FROM 
        (
            SELECT Data FROM SessionePratica WHERE IdCorso = id_corso_in
            UNION
            SELECT Data FROM SessioneOnline WHERE IdCorso = id_corso_in
        );

	-- Conta gli iscritti al corso
	SELECT COUNT(*) 
    INTO num_iscritti 
    FROM Iscrizioni 
    WHERE IdCorso = id_corso_in;

	-- Verifica se il corso ha iscritti e se oggi è tra inizio e fine corso oppure prima dell'inizio
	IF(num_iscritti > 0) THEN
		IF (data_fine_corso >= CURRENT_DATE AND data_inizio_corso <= CURRENT_DATE) OR (CURRENT_DATE < data_inizio_corso)
			THEN RETURN TRUE;
		END IF;
	ELSE
		RETURN FALSE;
	END IF;
END;
$$ LANGUAGE plpgsql;

-----------------------------------------------------------------------------------------------------------------------

-- Blocca la eliminazione di uno chef con corsi attivi 

CREATE OR REPLACE FUNCTION fun_delete_chef_con_corsi()
RETURNS TRIGGER AS
$$
BEGIN
	-- Se esiste un corso attivo per quello chef
	IF EXISTS (SELECT 1 
               FROM Corso 
		       WHERE IdChef = OLD.IdChef AND corso_attivo(IdCorso)) 
		-- Blocca cancellazione dello chef
    	THEN RAISE EXCEPTION 'Non puoi Eliminare uno Chef che ha corsi attivi.';
    	END IF;

    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_delete_chef_con_corsi
BEFORE DELETE ON Chef
FOR EACH ROW
EXECUTE FUNCTION fun_delete_chef_con_corsi();

-----------------------------------------------------------------------------------------------------------------------

-- Impedisce la modifica dei campi: IdCorso, Costo e IdChef

CREATE OR REPLACE FUNCTION fun_blocca_aggiorna_corso()
RETURNS TRIGGER AS
$$
BEGIN
    RAISE EXCEPTION 'Non puoi modificare i campi IdCorso, Costo o IdChef.';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_blocca_aggiorna_corso
BEFORE UPDATE OF IdCorso, Costo, IdChef ON Corso
FOR EACH ROW
EXECUTE FUNCTION fun_blocca_aggiorna_corso();


-- Impedisce la modifica della DataInizio di un corso se sono già presenti sessioni collegate (pratiche o online)

CREATE OR REPLACE FUNCTION fun_blocca_aggiornamento_data_se_iniziato()
RETURNS TRIGGER AS 
$$
BEGIN 
	-- Se esiste almeno una sessione associata al corso
	IF EXISTS
				(
				   SELECT 1 FROM SessionePratica WHERE IdCorso = NEW.IdCorso
				   UNION
				   SELECT 1 FROM SessioneOnline WHERE IdCorso = NEW.IdCorso
				)
	THEN
		-- Blocca l'update della data di inizio
		RAISE EXCEPTION 'Il corso e'' già iniziato!! non puoi spostare la data di inizio corso';
	END IF;
	RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_blocca_aggiornamento_data_se_iniziato
BEFORE UPDATE OF DataInizio ON Corso
FOR EACH ROW
EXECUTE FUNCTION fun_blocca_aggiornamento_data_se_iniziato();


-- Non è possibile cancellare un corso attivo

CREATE OR REPLACE FUNCTION fun_blocca_delete_corso()
RETURNS TRIGGER AS 
$$
BEGIN
	IF corso_attivo(OLD.IdCorso) THEN
		RAISE EXCEPTION 'Non puoi cancellare un corso attivo!!';
    END IF;

	RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_blocca_delete_corso
BEFORE DELETE ON Corso
FOR EACH ROW
EXECUTE FUNCTION fun_blocca_delete_corso();

-----------------------------------------------------------------------------------------------------------------------

-- Impedisce la modifica delle sessioni dei campi: IdSessione(pratica,online) e IdCorso

CREATE OR REPLACE FUNCTION fun_blocca_aggiorna_sessioni()
RETURNS TRIGGER AS
$$
BEGIN
 	RAISE EXCEPTION 'Non puoi modificare i campi IdSessione e IdCorso.';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_blocca_aggiorna_sessioni_online
BEFORE UPDATE OF IdSessioneOnline, IdCorso On SessioneOnline
FOR EACH ROW
EXECUTE FUNCTION fun_blocca_aggiorna_sessioni();

CREATE TRIGGER trg_blocca_aggiorna_sessioni_pratiche
BEFORE UPDATE  OF IdSessionePratica, IdCorso ON SessionePratica
FOR EACH ROW
EXECUTE FUNCTION fun_blocca_aggiorna_sessioni();


-- Non è possibile cancellare una sessione già avvenuta se il corso è ancora attivo

CREATE OR REPLACE FUNCTION fun_blocca_delete_sessioni_passate()
RETURNS TRIGGER 
AS 
$$
BEGIN
	-- Se il corso è attivo e la sessione è avvenuta
    IF corso_attivo(OLD.IdCorso) AND OLD.Data <= CURRENT_DATE THEN
        RAISE EXCEPTION 'Non puoi cancellare una sessione già avvenuta in un corso non ancora terminato';
    END IF;

    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_blocca_delete_sessioni_pratiche_passate
BEFORE DELETE ON SessionePratica
FOR EACH ROW
EXECUTE FUNCTION fun_blocca_delete_sessioni_passate();

CREATE TRIGGER trg_blocca_delete_sessioni_online_passate
BEFORE DELETE ON SessioneOnline
FOR EACH ROW
EXECUTE FUNCTION fun_blocca_delete_sessioni_passate();

-----------------------------------------------------------------------------------------------------------------------

-- Non è possibile cancellare o modificare un argomento ( è una tabella molti a molti globale e rappresenta keywords comuni per rappresentare le tematiche del corso)

CREATE OR REPLACE FUNCTION fun_blocca_aggiorna_argomento()
RETURNS TRIGGER AS
$$
BEGIN
	RAISE EXCEPTION 'Non puoi modificare o cancellare un argomento!!!';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_blocca_aggiorna_argomento
BEFORE DELETE OR UPDATE OF Nome ON Argomento
FOR EACH ROW
EXECUTE  FUNCTION fun_blocca_aggiorna_argomento();

-----------------------------------------------------------------------------------------------------------------------

-- Impedisce di modificare gli argomenti di un corso se è attivo o ha iscrizioni
-- Una volta che un corso è attivo non è più possibile alterarne la lista di argomenti, per coerenza

CREATE OR REPLACE FUNCTION fun_blocca_aggiorna_argomenticorso()
RETURNS TRIGGER AS
$$
BEGIN
	IF corso_attivo(OLD.IdCorso) THEN
 		RAISE EXCEPTION 'Non puoi modificare i campi IdCorso, IdArgomento.';
	END IF;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_blocca_aggiorna_argomenticorso
BEFORE UPDATE OF IdCorso, IdArgomento ON Argomenti_Corso
FOR EACH ROW
EXECUTE FUNCTION fun_blocca_aggiorna_argomenticorso();


-- Impedisce la cancellazione dell’ultimo argomento di un corso

CREATE OR REPLACE FUNCTION fun_blocca_cancellazione_argomento_corso()
RETURNS TRIGGER AS
$$
DECLARE
    num_argomenti INT;
BEGIN
		-- Conto gli argomenti del corso
    	SELECT COUNT(*) INTO num_argomenti
    	FROM Argomenti_Corso
   	 	WHERE IdCorso = OLD.IdCorso;

		-- Controllo il numero 
   	 	IF num_argomenti <= 1 THEN
       	 		RAISE EXCEPTION 'Non puoi rimuovere l''ultimo argomento di un corso.';
    	END IF;

    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_blocca_cancellazione_argomento_corso
BEFORE DELETE ON Argomenti_Corso
FOR EACH ROW
EXECUTE FUNCTION fun_blocca_cancellazione_argomento_corso();

-----------------------------------------------------------------------------------------------------------------------

-- Impedisce la modifica di IdRicetta e IdChef nella tabella Ricetta

CREATE OR REPLACE FUNCTION fun_blocca_aggiorna_ricetta()
RETURNS TRIGGER AS 
$$
BEGIN
 	RAISE EXCEPTION 'Non puoi modificare i campi IdRicetta e IdChef.';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_blocca_aggiorna_ricetta
BEFORE UPDATE OF IdRicetta, IdChef ON Ricetta
FOR EACH ROW
EXECUTE FUNCTION fun_blocca_aggiorna_ricetta();


-- Una ricetta non può essere eliminata se è utilizzata in una sessione pratica associata a un corso attivo.
CREATE OR REPLACE FUNCTION fun_blocca_delete_ricetta_utilizzata()
RETURNS TRIGGER AS
$$
BEGIN
	-- Se esiste una ricetta che è in uso in un corso attivo...
	IF EXISTS (
				SELECT 1
				FROM Preparazioni P JOIN SessionePratica SP ON P.IdSessionePratica = SP.IdSessionePratica
				WHERE P.IdRicetta = OLD.IdRicetta AND corso_attivo(SP.IdCorso)
			  )
	-- Blocca la delete
	THEN RAISE EXCEPTION 'Non puoi cancellare una ricetta che è in uso in una sessione pratica di un corso considerato attivo';
	END IF;

	RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_blocca_delete_ricetta_utilizzata
BEFORE DELETE ON Ricetta
FOR EACH ROW
EXECUTE FUNCTION fun_blocca_delete_ricetta_utilizzata();

-----------------------------------------------------------------------------------------------------------------------

-- Gli ingredienti rappresentano entità di base del sistema e non devono essere modificati
-- o cancellati per evitare effetti a catena su ricette e preparazioni esistenti.

CREATE OR REPLACE FUNCTION fun_blocca_aggiorna_ingrediente()
RETURNS TRIGGER AS
$$
BEGIN
	RAISE EXCEPTION 'Non puoi modificare o cancellare un ingrediente!!!';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_blocca_aggiorna_ingrediente
BEFORE DELETE OR UPDATE OF IdIngrediente, Nome, Origine ON Ingrediente
FOR EACH ROW
EXECUTE  FUNCTION fun_blocca_aggiorna_ingrediente();

-----------------------------------------------------------------------------------------------------------------------

--  Impedisce la modifica dei riferimenti nelle preparazioni pratiche

CREATE OR REPLACE FUNCTION fun_blocca_aggiorna_preparazioni()
RETURNS TRIGGER AS
$$
BEGIN
 	RAISE EXCEPTION 'Non puoi modificare i campi IdSessionePratica, IdRicetta.';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_blocca_aggiorna_preparazioni
BEFORE UPDATE OF IdSessionePratica, IdRicetta ON Preparazioni
FOR EACH ROW
EXECUTE FUNCTION fun_blocca_aggiorna_preparazioni();


-- Impedisce l’aggiunta di preparazioni a sessioni già avvenute per rispettare la cronologia degli eventi

CREATE OR REPLACE FUNCTION fun_preparazione_per_sessione_futura()
RETURNS TRIGGER AS
$$
DECLARE
    	Data_Sessione SessionePratica.Data%TYPE;
BEGIN
		-- Prendo la data della sessione pratica
		SELECT Data INTO Data_Sessione
    	FROM SessionePratica
    	WHERE IdSessionePratica = NEW.IdSessionePratica;

		-- Se è passata blocco l'insert su preparazioni
    	IF Data_Sessione <= CURRENT_DATE  THEN
        	RAISE EXCEPTION 'Non si può aggiungere una ricetta ad una sessione già avvenuta';
    	END IF;
    	RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_preparazione_per_sessione_futura
BEFORE INSERT ON Preparazioni
FOR EACH ROW
EXECUTE FUNCTION fun_preparazione_per_sessione_futura();

-----------------------------------------------------------------------------------------------------------------------

-- Impedisce la modifica dei riferimenti nelle iscrizioni a un corso

CREATE OR REPLACE FUNCTION fun_blocca_aggiorna_iscrizioni()
RETURNS TRIGGER AS
$$
BEGIN
 	RAISE EXCEPTION 'Non puoi modificare i campi IdPartecipante, IdCorso.';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_blocca_aggiorna_iscrizioni
BEFORE UPDATE OF IdPartecipante, IdCorso ON Iscrizioni
FOR EACH ROW
EXECUTE FUNCTION fun_blocca_aggiorna_iscrizioni();


-- Impedisce la disiscrizione da un corso se si è aderito a una sessione pratica imminente

CREATE OR REPLACE FUNCTION fun_impedisci_disiscrizione()
RETURNS TRIGGER AS
$$
BEGIN
	-- Se esiste una adesione imminente da parte del partecipante a una sessione pratica blocca la disiscrizione
 	IF EXISTS (SELECT 1 
               FROM Adesioni A JOIN SessionePratica SP ON A.IdSessionePratica = SP.IdSessionePratica
               WHERE IdPartecipante = OLD.IdPartecipante AND (Data - CURRENT_DATE) < 3) 
    THEN RAISE EXCEPTION 'Non puoi disiscriverti dal corso perchè hai aderito a una sessione che dista meno di 3 giorni';
    END IF;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_impedisci_disiscrizione
BEFORE DELETE ON Iscrizioni
FOR EACH ROW
EXECUTE FUNCTION fun_impedisci_disiscrizione();


-- Dopo una disiscrizione valida, elimina automaticamente adesioni future alle sessioni

CREATE OR REPLACE FUNCTION fun_gestisci_disiscrizione()
RETURNS TRIGGER AS
$$
BEGIN	
		-- Elimina tutte le adesioni del partecipante alle sessioni pratiche future del corso da cui si è disiscritto
    	-- Utilizza la clausola USING per fare riferimento a SessionePratica senza dover fare una join esplicita non permessa nella clausola WHERE di Adesioni,
		DELETE FROM Adesioni
		USING SessionePratica
		WHERE Adesioni.IdSessionePratica = SessionePratica.IdSessionePratica
		AND Adesioni.IdPartecipante = OLD.IdPartecipante
		AND SessionePratica.IdCorso = OLD.IdCorso
		AND SessionePratica.Data > CURRENT_DATE;
		RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_gestisci_disiscrizione
AFTER DELETE ON Iscrizioni
FOR EACH ROW
EXECUTE FUNCTION fun_gestisci_disiscrizione();


--Non ci si può iscrivere a un corso se è già iniziato o già finito

CREATE OR REPLACE FUNCTION fun_iscrizione_corso_iniziato()
RETURNS TRIGGER AS
$$
DECLARE
	Data_inizio_corso Corso.DataInizio%TYPE;
BEGIN
	-- Prendo la data di inizio del corso associato all'iscrizione
	SELECT DataInizio INTO Data_inizio_corso
	FROM Corso
	WHERE IdCorso = NEW.IdCorso;
		-- Se è già iniziato o finito allora non blocco l'iscrizione
    	IF Data_inizio_corso <= CURRENT_DATE  THEN
        	RAISE EXCEPTION 'Non ci si può iscrivere ad un corso già iniziato';
    	END IF;
    	RETURN NEW;

END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_iscrizione_corso_iniziato
BEFORE INSERT ON Iscrizioni
FOR EACH ROW
EXECUTE FUNCTION fun_iscrizione_corso_iniziato();

-- Se sono presenti sessioni pratiche allora non è possibile cambiare il tipo del corso a non pratico tramite update

CREATE OR REPLACE FUNCTION fun_blocca_update_ispratico()
RETURNS TRIGGER AS
$$
BEGIN
	-- Checka se è stato impostato IsPratico a false nell'update e ci sono sessioni pratiche nel corso
	IF NEW.IsPratico = FALSE AND EXISTS (SELECT 1 FROM SessionePratica WHERE IdCorso = OLD.IdCorso) THEN
		RAISE EXCEPTION 'Non puoi rendere un corso non pratico se ci sono sessioni pratiche nel corso';
	END IF;
	RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_blocca_update_ispratico
BEFORE UPDATE OF IsPratico ON Corso
FOR EACH ROW
WHEN (OLD.IsPratico IS DISTINCT FROM NEW.IsPratico)
EXECUTE FUNCTION fun_blocca_update_ispratico();

-----------------------------------------------------------------------------------------------------------------------

-- Blocca l'aggiornamento dei campi IdPartecipante, IdSessionePratica e DataAdesione nella tabella Adesioni.

CREATE OR REPLACE FUNCTION fun_blocca_aggiorna_adesioni()
RETURNS TRIGGER AS
$$
BEGIN
 	RAISE EXCEPTION 'Non puoi modificare i campi IdPartecipante, IdSessionePratica, DataAdesione.';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_blocca_aggiorna_adesioni
BEFORE UPDATE  OF IdPartecipante, IdSessionePratica, DataAdesione ON Adesioni
FOR EACH ROW
EXECUTE FUNCTION fun_blocca_aggiorna_adesioni();


-- Impedisce la cancellazione di un'adesione a una sessione pratica se mancano meno di 3 giorni dalla data della sessione.
-- Garantisce che non si possano rimuovere adesioni all'ultimo momento, preservando l' organizzazione.

CREATE OR REPLACE FUNCTION fun_blocca_cancella_adesioni()
RETURNS TRIGGER AS
$$
DECLARE 
	data_sessione SessionePratica.Data%TYPE;
BEGIN
 	SELECT Data INTO data_sessione FROM SessionePratica WHERE IdSessionePratica = OLD.IdSessionePratica;
	
	IF data_sessione - CURRENT_DATE <= 2 THEN
		 RAISE EXCEPTION ' Non puoi cancellare una adesione a meno di 3 giorni dalla sessione pratica!';
	END IF;
	RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_blocca_cancella_adesioni
BEFORE DELETE ON Adesioni
FOR EACH ROW
EXECUTE FUNCTION fun_blocca_cancella_adesioni();

-----------------------------------------------------------------------------------------------------------------------