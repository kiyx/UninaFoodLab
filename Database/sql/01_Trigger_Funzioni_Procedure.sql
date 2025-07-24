-- 01_Triggers_Functions_Procedures

-- Normalizzazione basica dei dati di Partecipante/Chef/Ricetta/Ingrediente

-- Normalizza Partecipante/Chef con Username minuscolo, CodiceFiscale Maiuscolo, Email minuscolo, Nome, Cognome e Luogo con l'iniziale Maiuscola

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

CREATE TRIGGER trg_normalizza_partecipante
BEFORE INSERT OR UPDATE ON Partecipante
FOR EACH ROW
EXECUTE FUNCTION fun_normalizza_utente();

CREATE TRIGGER trg_normalizza_chef
BEFORE INSERT OR UPDATE ON Chef
FOR EACH ROW
EXECUTE FUNCTION fun_normalizza_utente();


-- Normalizzo Ingrediente e Ricetta con l'iniziale del nome maiuscola

CREATE OR REPLACE FUNCTION fun_normalizza_ricetta_ingr_chef()
RETURNS TRIGGER AS
$$
BEGIN
    NEW.Nome := INITCAP(NEW.Nome);
    RETURN NEW;

END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_normalizza_ingrediente
BEFORE INSERT OR UPDATE ON Ingrediente
FOR EACH ROW
EXECUTE FUNCTION fun_normalizza_ricetta_ingr_chef();

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
	IF TG_OP = 'UPDATE' AND NEW.Username = OLD.Username THEN			-- Ottimizzazione se è una update e lo username non cambia
    		RETURN NEW;
	END IF;

	IF TG_TABLE_NAME = 'partecipante' THEN
    		IF EXISTS (SELECT 1 FROM Chef WHERE Username = NEW.Username) THEN
    			RAISE EXCEPTION 'Username già usato in Chef';
		END IF;
	END IF;
	
	IF TG_TABLE_NAME = 'chef' THEN
		IF EXISTS (SELECT 1 FROM Partecipante WHERE Username = NEW.Username) THEN
    			RAISE EXCEPTION 'Username già usato in Partecipante';
		END IF;
	END IF;
	RETURN NEW;

END;
$$ LANGUAGE plpgsql;

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

-- Se viene inserita un corso con un numero sessioni diverso da 0, lanciamo una eccezione perchè è gestita automaticamente dal db

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


-- Aggiornamento numero sessioni (increment)

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

CREATE TRIGGER trg_incremento_numsessioni_online
AFTER INSERT ON SessioneOnline
FOR EACH ROW
EXECUTE FUNCTION fun_incrementa_num_sessioni();

CREATE TRIGGER trg_incremento_numsessioni_pratiche
AFTER INSERT ON SessionePratica
FOR EACH ROW
EXECUTE FUNCTION fun_incrementa_num_sessioni();


-- Aggiornamento numero sessioni (decrement)

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

-- Non posso inserire una sessione pratica se ispratico = false (poteva essere aggiornato automaticamente 
-- all'inserimento di una sessione pratica ma non sarebbe stato possibile inserire il limite)

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


-- Se viene inserita o aggiornata una sessione pratica con un numero partecipanti diverso da 0, lanciamo una eccezione perchè è gestito automaticamente dal db

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


-- Se viene inserita una adesione, bisogna settarla alla data di oggi 

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


-- Trigger aggiornamento numero utenti (increment)

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


-- Trigger aggiornamento numero utenti (decrement)

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

-- Interrelazionale: Non ci possono essere più sessioni per lo stesso corso nello stesso giorno

CREATE OR REPLACE FUNCTION fun_unicita_sessione_giorno()
RETURNS TRIGGER AS
$$
BEGIN
		IF TG_TABLE_NAME = 'sessionepratica' THEN
			IF EXISTS 
			(
        		SELECT 1 FROM SessionePratica
        		WHERE Data = NEW.Data
          		AND IdCorso = NEW.IdCorso
          		AND NOT (TG_OP = 'UPDATE' AND IdSessionePratica = NEW.IdSessionePratica)
    		) 
			THEN RAISE EXCEPTION 'Esiste già una sessione pratica per questo corso in data %.', NEW.Data;
			END IF;
			IF EXISTS 
			(
        		SELECT 1 FROM SessioneOnline
        		WHERE Data = NEW.Data
          		AND IdCorso = NEW.IdCorso
    		) 
			THEN RAISE EXCEPTION 'Esiste già una sessione online per questo corso in data %.', NEW.Data;
			END IF;
		ELSE
			 IF EXISTS 
			 (
        		SELECT 1 FROM SessioneOnline
        		WHERE Data = NEW.Data
          		AND IdCorso = NEW.IdCorso
          		AND NOT (TG_OP = 'UPDATE' AND IdSessioneOnline = NEW.IdSessioneOnline)
    		) 
			THEN RAISE EXCEPTION 'Esiste già una sessione online per questo corso in data %.', NEW.Data;
			END IF;
			IF EXISTS 
			(
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


-- La data della sessione non puo essere prima della data inizio corso e se non ci sono sessioni allora deve essere inserita il giorno di inizio corso DA TESTARE

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


-- Interrelazionale: Quando inserisco o modifico una sessione deve rispettare l'intervallo della frequenza 

CREATE OR REPLACE FUNCTION fun_verifica_frequenza_sessioni()
RETURNS TRIGGER AS
$$
DECLARE
    frequenza Corso.FrequenzaSessioni%TYPE;  -- Memorizza la frequenza del corso (es. 'Settimanale').
    giorniFrequenza INTEGER;                 -- Numero di giorni corrispondente alla frequenza.
    v_dataInizio DATE;                       -- Data di inizio del corso di riferimento.
    finestra_nuova INTEGER;                  -- Indice numerico della finestra temporale per la nuova data.
    finestra_vecchia INTEGER;                -- Indice numerico della finestra temporale per la vecchia data (solo in UPDATE).
    id_sessione_corrente INTEGER;            -- ID della sessione in corso di modifica (per auto-esclusione).
BEGIN
    -- Recupera i dati fondamentali del corso per i calcoli successivi.
    SELECT FrequenzaSessioni, DataInizio
    INTO frequenza, v_dataInizio
    FROM Corso
    WHERE IdCorso = NEW.IdCorso;

    -- VALIDAZIONE 1: La data della sessione non può essere antecedente alla data di inizio del corso.
    IF NEW.Data < v_dataInizio THEN
        RAISE EXCEPTION 'La data della sessione (%) non può essere precedente alla data di inizio del corso (%).', NEW.Data, v_dataInizio;
    END IF;

    -- USCITA RAPIDA: Se la frequenza è 'Libera', non sono necessari ulteriori controlli.
    IF frequenza = 'Libera' THEN
        RETURN NEW;
    END IF;

    -- Converte la frequenza testuale in un numero di giorni per il calcolo delle finestre temporali.
    CASE frequenza
        WHEN 'Giornaliera' THEN giorniFrequenza := 1;
        WHEN 'Settimanale' THEN giorniFrequenza := 7;
        WHEN 'Bisettimanale' THEN giorniFrequenza := 14;
        WHEN 'Mensile' THEN giorniFrequenza := 30;
        ELSE
            RAISE EXCEPTION 'Frequenza non riconosciuta: %', frequenza;
    END CASE;

    -- Calcola l'indice della "finestra temporale" per la nuova data della sessione.
    -- Esempio: (DataSessione - DataInizioCorso) / 7 giorni -> 0 per la prima settimana, 1 per la seconda, etc.
    finestra_nuova := FLOOR((NEW.Data - v_dataInizio) / giorniFrequenza);

    -- OTTIMIZZAZIONE PER UPDATE: Se la sessione viene modificata ma rimane nella stessa finestra temporale,
    -- l'operazione è valida e non sono necessari altri controlli.
    IF TG_OP = 'UPDATE' THEN
        finestra_vecchia := FLOOR((OLD.Data - v_dataInizio) / giorniFrequenza);
        IF finestra_nuova = finestra_vecchia THEN
            RETURN NEW;
        END IF;
    END IF;

    -- Identifica la sessione corrente per escluderla dal controllo di conflitto durante un UPDATE.
    IF TG_TABLE_NAME = 'sessionepratica' THEN
        id_sessione_corrente := NEW.IdSessionePratica;
    ELSE
        id_sessione_corrente := NEW.IdSessioneOnline;
    END IF;

    -- Verifica se esiste già un'altra sessione nella finestra temporale di destinazione.
    IF EXISTS 
    (
        -- Unifico tutte le sessioni (pratiche e online).
        SELECT 1
        FROM (
                SELECT Data, IdSessionePratica AS id, 'SessionePratica' as tipo FROM SessionePratica WHERE IdCorso = NEW.IdCorso
                UNION ALL
                SELECT Data, IdSessioneOnline AS id, 'SessioneOnline' as tipo FROM SessioneOnline WHERE IdCorso = NEW.IdCorso
             ) AS tutte_le_sessioni
        -- Filtra per trovare sessioni che cadono nella stessa finestra temporale calcolata.
        WHERE FLOOR((tutte_le_sessioni.Data - v_dataInizio) / giorniFrequenza) = finestra_nuova
        -- Esclude la sessione stessa dal controllo in caso di UPDATE, per evitare un falso positivo.
        AND NOT (TG_OP = 'UPDATE' AND tutte_le_sessioni.id = id_sessione_corrente AND tutte_le_sessioni.tipo = TG_TABLE_NAME)
    ) 
    THEN
        -- Se viene trovata una sessione, l'operazione viene bloccata per evitare sovrapposizioni.
        RAISE EXCEPTION 'La finestra temporale per la data % è già occupata da un''altra sessione.', NEW.Data;
    END IF;

    -- Se nessun controllo ha fallito, l'operazione di INSERT o UPDATE è permessa.
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


-- Trigger per aggiornare la FrequenzaSessioni del Corso a 'Libera' quando una sessione associata viene cancellata.

CREATE OR REPLACE FUNCTION fun_gestisci_frequenza_dopo_eliminazione()
RETURNS TRIGGER AS
$$
BEGIN
    -- Controlla se la frequenza del corso non è già 'Libera'
    -- per evitare aggiornamenti ridondanti
    IF (	  SELECT FrequenzaSessioni FROM Corso WHERE IdCorso = OLD.IdCorso) <> 'Libera' THEN
       		  UPDATE Corso
        	  SET FrequenzaSessioni = 'Libera'
      		  WHERE IdCorso = OLD.IdCorso;
       		  RAISE NOTICE 'FrequenzaSessioni del corso % è stata impostata a "Libera" a causa della cancellazione di una sessione.', OLD.IdCorso;
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


--- Stesso chef -> piu corsi -> controllare che le sessioni non siano nella stessa fascia oraria

CREATE OR REPLACE FUNCTION fun_controlla_sovrapposizione_orario_sessione()
RETURNS TRIGGER AS
$$
DECLARE
    chef Chef.IdChef%TYPE;
    inizio_nuova TIME := NEW.Orario;
    fine_nuova TIME := (NEW.Orario + (NEW.Durata || ' minutes')::interval)::time;
    id_sessione_corrente INTEGER;
	sessione_corrente TEXT;
BEGIN

    IF TG_TABLE_NAME = 'sessionepratica' THEN
        id_sessione_corrente := NEW.IdSessionePratica;
		sessione_corrente := 'sessionepratica';
    ELSIF TG_TABLE_NAME = 'sessioneonline' THEN
        id_sessione_corrente := NEW.IdSessioneOnline;
		sessione_corrente := 'sessioneonline';
    END IF;

    SELECT IdChef INTO chef FROM Corso WHERE IdCorso = NEW.IdCorso;

    IF EXISTS 
       (
            SELECT 1 FROM 
            (
                SELECT SP.Data, SP.Orario, SP.Durata, C.IdChef, SP.IdSessionePratica AS IdSessione, 'sessionepratica' AS TipoSessione
                FROM SessionePratica SP JOIN Corso C ON SP.IdCorso = C.IdCorso
                WHERE C.IdChef = chef AND SP.Data = NEW.Data

                UNION ALL

                SELECT SO.Data, SO.Orario, SO.Durata, C.IdChef, SO.IdSessioneOnline AS IdSessione, 'sessioneonline' AS TipoSessione
                FROM SessioneOnline SO JOIN Corso C ON SO.IdCorso = C.IdCorso
                WHERE C.IdChef = chef AND SO.Data = NEW.Data
            ) AS S
            WHERE NOT (TG_OP = 'UPDATE' AND S.TipoSessione = sessione_corrente AND S.IdSessione = id_sessione_corrente)
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

-- BUSINESS IntraRelazionale: Gli argomenti del corso non possono essere più di 5

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

-- Interrelazionale: Se viene inserita una iscrizione ma il limite di iscrizioni è già raggiunto, essa non viene inserita

CREATE OR REPLACE FUNCTION fun_limite_iscrizioni()
RETURNS TRIGGER AS
$$
DECLARE
    	numero_iscritti INTEGER;
    	limite_corso Corso.Limite%TYPE;
		pratico Corso.isPratico%TYPE;
BEGIN
	
	SELECT Limite, isPratico INTO limite_corso, pratico
    FROM Corso
    WHERE IdCorso = NEW.IdCorso;

	IF pratico THEN
    		SELECT COUNT(*) INTO numero_iscritti
    		FROM Iscrizioni
    		WHERE IdCorso = NEW.IdCorso;
    	
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

-- Interrelazionale: Un utente non puo' partecipare a una sessione pratica se non iscritto al corso che la organizza

CREATE OR REPLACE FUNCTION fun_iscrizione_before_adesione()
RETURNS TRIGGER AS
$$
DECLARE
    	idcorso_sessione SessionePratica.IdCorso%TYPE;
BEGIN

    	SELECT IdCorso INTO idcorso_sessione
    	FROM SessionePratica
    	WHERE IdSessionePratica = NEW.IdSessionePratica;

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


-- Interrelazionale: La data dell'adesione alla sessione pratica deve essere antecedente (3 giorni) alla data della sessione pratica/ 
-- Se la sessione pratica è già avvenuta, l'utente non puo' più aderire

CREATE OR REPLACE FUNCTION fun_data_adesione()
RETURNS TRIGGER AS
$$
DECLARE
    	Data_Sessione SessionePratica.Data%TYPE;
BEGIN
    	SELECT Data INTO Data_Sessione
    	FROM SessionePratica
    	WHERE IdSessionePratica = NEW.IdSessionePratica;

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

-- Controllare che lo chef della ricetta aggiunta alla sessione pratica sia lo stesso dello chef che organizza il corso 

CREATE OR REPLACE FUNCTION fun_check_corso_chef()
RETURNS TRIGGER AS
$$
DECLARE 
    ChefRicetta Chef.IdChef%TYPE;
    ChefCorso Chef.IdChef%TYPE;
BEGIN
	
    SELECT IdChef 
    INTO ChefRicetta
    FROM Ricetta R
    WHERE R.IdRicetta = NEW.IdRicetta;

    SELECT IdChef
    INTO ChefCorso
    FROM SessionePratica SP JOIN Corso C ON SP.IdCorso = C.IdCorso
    WHERE SP.IdSessionePratica = NEW.IdSessionePratica;

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

-- Il Codice Fiscale non è modificabile una volta creato l'utente

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

--Non posso cancellare il partecipante se ha aderito a una sessione e siamo oltre i 3 giorni prima

CREATE OR REPLACE FUNCTION fun_delete_partecipante_con_adesione()
RETURNS TRIGGER AS
$$
BEGIN
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

-- Utility per checkare se un corso è ancora attivo

CREATE OR REPLACE FUNCTION corso_attivo(id_corso_in INTEGER)
RETURNS BOOLEAN AS
$$
DECLARE
	data_inizio_corso Corso.DataInizio%TYPE;
	data_fine_corso Corso.DataInizio%TYPE;
BEGIN
	SELECT DataInizio INTO data_inizio_corso FROM Corso WHERE IdCorso = id_corso_in;

	SELECT MAX(Data) INTO data_fine_corso
     	FROM 
        (
            SELECT Data FROM SessionePratica WHERE IdCorso = id_corso_in
            UNION
            SELECT Data FROM SessioneOnline WHERE IdCorso = id_corso_in
        );

	IF data_fine_corso >= CURRENT_DATE AND data_inizio_corso <= CURRENT_DATE THEN
		RETURN TRUE;
	ELSE
		RETURN FALSE;
	END IF;
END;
$$ LANGUAGE plpgsql;

-----------------------------------------------------------------------------------------------------------------------

--Non posso cancellare lo chef se ha corsi attivi

CREATE OR REPLACE FUNCTION fun_delete_chef_con_corsi()
RETURNS TRIGGER AS
$$
BEGIN
	IF EXISTS (SELECT 1 
               FROM Corso 
		       WHERE IdChef = OLD.IdChef AND corso_attivo(IdCorso)) 

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

-- Del corso non è possibile aggiornare IdCorso, Costo, IdChef

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


-- Non puoi modifcare data inizio corso se ci sono sessioni

CREATE OR REPLACE FUNCTION fun_blocca_aggiornamento_data_se_iniziato()
RETURNS TRIGGER AS 
$$
BEGIN 
	IF EXISTS
				(
				   SELECT 1 FROM SessionePratica WHERE IdCorso = NEW.IdCorso
				   UNION
				   SELECT 1 FROM SessioneOnline WHERE IdCorso = NEW.IdCorso
				)
	THEN
		RAISE EXCEPTION 'Il corso e'' già iniziato!! non puoi spostare la data di inizio corso';
	END IF;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_blocca_aggiornamento_data_se_iniziato
BEFORE UPDATE OF DataInizio ON Corso
FOR EACH ROW
EXECUTE FUNCTION fun_blocca_aggiornamento_data_se_iniziato();


-- Non si puo cancellare un corso che è attivo o che ha iscritti

CREATE OR REPLACE FUNCTION fun_blocca_delete_corso()
RETURNS TRIGGER AS 
$$
DECLARE 
	num_iscritti INT;
BEGIN

	IF corso_attivo(OLD.IdCorso) THEN
		RAISE EXCEPTION 'Non puoi cancellare un corso ancora in svolgimento!!';
    END IF;

    SELECT COUNT(*) 
    INTO num_iscritti 
    FROM Iscrizioni 
    WHERE IdCorso = OLD.IdCorso;

    IF num_iscritti > 0 THEN
        RAISE EXCEPTION 'Non puoi cancellare un corso che ha già iscritti!';
	END IF;

	RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_blocca_delete_corso
BEFORE DELETE ON Corso
FOR EACH ROW
EXECUTE FUNCTION fun_blocca_delete_corso();

-----------------------------------------------------------------------------------------------------------------------

-- Delle sessioni non è possibile aggiornare IdSessione(pratica,online) e IdCorso

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


-- Non puoi cancellare una sessione passata se il suo corso è ancora attivo

CREATE OR REPLACE FUNCTION fun_blocca_delete_sessioni_passate()
RETURNS TRIGGER 
AS 
$$
BEGIN
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

-- Non si può modificare o eliminare un argomento

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

-- Non deve essere possibile aggiornare gli argomenti_corso

CREATE OR REPLACE FUNCTION fun_blocca_aggiorna_argomenticorso()
RETURNS TRIGGER AS
$$
BEGIN
 	RAISE EXCEPTION 'Non puoi modificare i campi IdCorso, IdArgomento.';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_blocca_aggiorna_argomenticorso
BEFORE UPDATE  OF IdCorso, IdArgomento ON Argomenti_Corso
FOR EACH ROW
EXECUTE FUNCTION fun_blocca_aggiorna_argomenticorso();


-- Bloccare la cancellazione dell'ultimo argomento di un corso

CREATE OR REPLACE FUNCTION fun_blocca_cancellazione_argomento_corso()
RETURNS TRIGGER AS
$$
DECLARE
    num_argomenti INT;
BEGIN
    	SELECT COUNT(*) INTO num_argomenti
    	FROM Argomenti_Corso
   	 	WHERE IdCorso = OLD.IdCorso;

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

-- Della ricetta non è possibile aggiornare IdRicetta e IdChef

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


-- Non puoi cancellare una ricetta se è in uso in una sessione pratica

CREATE OR REPLACE FUNCTION fun_blocca_delete_ricetta_utilizzata()
RETURNS TRIGGER AS
$$
BEGIN
	IF EXISTS (
				SELECT 1
				FROM Preparazioni P JOIN SessionePratica SP ON P.IdSessionePratica = SP.IdSessionePratica
				WHERE P.IdRicetta = OLD.IdRicetta AND SP.Data > CURRENT_DATE
			  )
	THEN RAISE EXCEPTION 'Non puoi cancellare una ricetta che è in uso in una sessione pratica non ancora iniziata';
	END IF;

	RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_blocca_delete_ricetta_utilizzata
BEFORE DELETE ON Ricetta
FOR EACH ROW
EXECUTE FUNCTION fun_blocca_delete_ricetta_utilizzata();


-----------------------------------------------------------------------------------------------------------------------

--Degli Ingredienti non deve essere possibile aggiornare IdIngrediente, Nome, Origine

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

-- Non deve essere possibile aggiornare gli utilizzi

CREATE OR REPLACE FUNCTION fun_blocca_aggiorna_utilizzi()
RETURNS TRIGGER AS
$$
BEGIN
 	RAISE EXCEPTION 'Non puoi modificare i campi IdRicetta, IdIngrediente.';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_blocca_aggiorna_utilizzi
BEFORE UPDATE OF IdRicetta, IdIngrediente ON Utilizzi
FOR EACH ROW
EXECUTE FUNCTION fun_blocca_aggiorna_utilizzi();

-----------------------------------------------------------------------------------------------------------------------

-- Non deve essere possibile aggiornare le preparazioni

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


-- Non si puo eliminare una ricetta dalla sessione pratica 


--Non si può aggiungere una ricetta alla sessione pratica se essa è già avvenuta

CREATE OR REPLACE FUNCTION fun_preparazione_per_sessione_futura()
RETURNS TRIGGER AS
$$
DECLARE
    	Data_Sessione SessionePratica.Data%TYPE;
BEGIN

	SELECT Data INTO Data_Sessione
    	FROM SessionePratica
    	WHERE IdSessionePratica = NEW.IdSessionePratica;

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

-- Non deve essere possibile aggiornare le iscrizioni

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


-- Non ci si può disiscrivere dal corso se si è aderito a una sessione pratica e mancano meno di 3 giorni dal suo avvenimento

CREATE OR REPLACE FUNCTION fun_impedisci_disiscrizione()
RETURNS TRIGGER AS
$$
BEGIN
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


-- Se è possibile disiscriversi dal corso vengono tolte le adesioni effettuate da oggi in poi alle sessioni pratiche di quel corso potrei averne altre i giorni subito successivi

CREATE OR REPLACE FUNCTION fun_gestisci_disiscrizione()
RETURNS TRIGGER AS
$$
BEGIN	
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
	SELECT DataInizio INTO Data_inizio_corso
	FROM Corso
	WHERE IdCorso = NEW.IdCorso;

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

-- Se ci sono sessioni pratiche, non puoi modificare il campo ispratico di corso

CREATE OR REPLACE FUNCTION fun_blocca_update_ispratico()
RETURNS TRIGGER AS
$$
BEGIN
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

-- Non deve essere possibile aggiornare le adesioni

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


-- Si può eliminare una adesione a una sessione pratica fino a 3 giorni prima in cui essa avviene

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