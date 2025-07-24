-- Setto lo schema come primo nella ricerca
SET search_path TO UninaFoodLab, public;

-- Creo lo schema per UninaFoodLab
CREATE SCHEMA UninaFoodLab;

-- 00_Create: Definizione Tabelle

-- Partecipante
CREATE TABLE Partecipante
(
	IdPartecipante SERIAL PRIMARY KEY,
	Username TEXT UNIQUE NOT NULL,
	Nome VARCHAR(100) NOT NULL,
	Cognome VARCHAR(100) NOT NULL,
	CodiceFiscale CHAR(17) UNIQUE NOT NULL, 
	DataDiNascita DATE NOT NULL,
	LuogoDiNascita VARCHAR(100) NOT NULL,
	Email TEXT UNIQUE NOT NULL, 
	Password VARCHAR(60) NOT NULL,
	CONSTRAINT check_empty_part_nome CHECK (LENGTH(Nome) > 0),
	CONSTRAINT check_empty_part_cognome CHECK (LENGTH(Cognome) > 0),
	CONSTRAINT check_empty_part_cf CHECK (LENGTH(CodiceFiscale) > 0),
	CONSTRAINT check_empty_part_luogonascita CHECK (LENGTH(LuogoDiNascita) > 0),
	CONSTRAINT check_empty_part_email CHECK (LENGTH(Email) > 0),
	CONSTRAINT check_empty_part_pass CHECK (LENGTH(Password) > 0),
	CONSTRAINT check_part_length CHECK (LENGTH(Username) BETWEEN 4 AND 20),
	CONSTRAINT check_part_maggiorenne CHECK (DataDiNascita <= CURRENT_DATE - INTERVAL '18 years')
);

-- Chef
CREATE TABLE Chef
(
	IdChef SERIAL PRIMARY KEY,
	Username TEXT UNIQUE NOT NULL,
	Nome VARCHAR(100) NOT NULL,
	Cognome VARCHAR(100) NOT NULL,
	CodiceFiscale CHAR(17) UNIQUE NOT NULL, 
	DataDiNascita DATE NOT NULL,
	LuogoDiNascita VARCHAR(100) NOT NULL,
	Email TEXT UNIQUE NOT NULL, 
	Password VARCHAR(60) NOT NULL,
	Curriculum TEXT NOT NULL,
	CONSTRAINT check_empty_chef_nome CHECK (LENGTH(Nome) > 0),
	CONSTRAINT check_empty_chef_cognome CHECK (LENGTH(Cognome) > 0),
	CONSTRAINT check_empty_chef_cf CHECK (LENGTH(CodiceFiscale) > 0),
	CONSTRAINT check_empty_chef_luogonascita CHECK (LENGTH(LuogoDiNascita) > 0),
	CONSTRAINT check_empty_chef_email CHECK (LENGTH(Email) > 0),
	CONSTRAINT check_empty_chef_pass CHECK (LENGTH(Password) > 0),
	CONSTRAINT check_empty_chef_curriculum CHECK (LENGTH(Curriculum) > 0),
	CONSTRAINT check_chef_length CHECK (LENGTH(Username) BETWEEN 4 AND 20),
	CONSTRAINT check_chef_maggiorenne CHECK (DataDiNascita <= CURRENT_DATE - INTERVAL '18 years')
);

-- Argomento
CREATE TABLE Argomento
(
	IdArgomento SERIAL PRIMARY KEY,
	Nome VARCHAR(100) UNIQUE NOT NULL
);

-- Enum per la frequenza delle sessioni
CREATE TYPE Frequenza AS ENUM ('Giornaliera', 'Settimanale', 'Bisettimanale', 'Mensile', 'Libera' );

-- Corso
CREATE TABLE Corso 
(
	IdCorso SERIAL PRIMARY KEY,
	Nome VARCHAR(100) NOT NULL,
	DataInizio DATE NOT NULL,																					
	NumeroSessioni INTEGER DEFAULT 0,							
	FrequenzaSessioni Frequenza NOT NULL,
	Limite INTEGER DEFAULT NULL,
	Descrizione TEXT NOT NULL,
	Costo NUMERIC(10,2) NOT NULL,
	isPratico BOOLEAN NOT NULL DEFAULT false,
	IdChef INTEGER NOT NULL,
	CONSTRAINT check_empty_corso_nome CHECK (LENGTH(Nome) > 0),
    CONSTRAINT check_data_non_passata_corso CHECK (DataInizio > CURRENT_DATE),		
	CONSTRAINT check_empty_corso_descr CHECK (LENGTH(Descrizione) > 0),														
	CONSTRAINT fk_chef_corso FOREIGN KEY(IdChef) REFERENCES Chef(IdChef) ON DELETE CASCADE,
	CONSTRAINT check_costo_non_negativo CHECK (Costo >= 0.0),
	CONSTRAINT check_limite_pratico CHECK ((NOT isPratico AND LIMITE IS NULL) OR (isPratico AND Limite IS NOT NULL))	 -- Il corso ha limite solo se è pratico		
);

-- SessionePratica
CREATE TABLE SessionePratica
(
	IdSessionePratica SERIAL PRIMARY KEY, 
	Durata INTEGER NOT NULL,													  -- La durata è in minuti
	Orario TIME NOT NULL,
	Data DATE NOT NULL, 
	NumeroPartecipanti INTEGER DEFAULT 0,
	Luogo VARCHAR(100) NOT NULL,
	IdCorso  INTEGER NOT NULL,
	CONSTRAINT check_data_non_passata_sessioneprat CHECK (Data > CURRENT_DATE),
	CONSTRAINT check_durata_positiva_sessioneprat CHECK (Durata > 0),
	CONSTRAINT check_empty_sessioneprat_luogo CHECK (LENGTH(Luogo) > 0),	
	CONSTRAINT fk_corso_pratica FOREIGN KEY(IdCorso) REFERENCES Corso(IdCorso) ON DELETE CASCADE 	--Se eliminiamo il corso vengono eliminate anche tutte le sessioni pratiche associate
);

-- SessioneOnline
CREATE TABLE SessioneOnline
(
	IdSessioneOnline SERIAL PRIMARY KEY, 
	Durata INTEGER NOT NULL,			                -- La durata è in minuti
	Orario TIME NOT NULL,
	Data DATE NOT NULL, 
	LinkRiunione TEXT NOT NULL,
	IdCorso  INTEGER NOT NULL,
	CONSTRAINT check_data_non_passata_sessioneonl CHECK (Data > CURRENT_DATE),
	CONSTRAINT check_durata_positiva_sessioneonl CHECK (Durata > 0),
	CONSTRAINT check_empty_sessioneonl_link CHECK (LENGTH(LinkRiunione) > 0),
	CONSTRAINT fk_corso_online FOREIGN KEY(IdCorso) REFERENCES Corso(IdCorso) ON DELETE CASCADE		--Se eliminiamo il corso vengono eliminate anche tutte le sessioni online associate
);

-- Enum per il tipo di difficoltà della ricetta
CREATE TYPE LivelloDifficolta AS ENUM ('Principiante', 'Facile', 'Medio', 'Difficile', 'Esperto');

-- Ricetta
CREATE TABLE Ricetta
(
	IdRicetta SERIAL PRIMARY KEY,
	Nome VARCHAR(100) NOT NULL,
	Provenienza VARCHAR(100) NOT NULL,
	Tempo INTEGER NOT NULL,				                -- La durata è in minuti
	Calorie INTEGER NOT NULL,				            -- in KCAL
	Difficolta LivelloDifficolta NOT NULL,
	Allergeni VARCHAR(100),
	IdChef INTEGER NOT NULL,
	CONSTRAINT unique_nome_chef UNIQUE (Nome, IdChef),
	CONSTRAINT check_empty_ricetta_nome CHECK (LENGTH(Nome) > 0),
	CONSTRAINT check_empty_ricetta_provenienza CHECK (LENGTH(Provenienza) > 0),				
	CONSTRAINT check_tempo_positivo CHECK (Tempo > 0),
	CONSTRAINT check_calorie_positive CHECK (Calorie > 0),
	CONSTRAINT fk_chef_ricetta FOREIGN KEY(IdChef) REFERENCES Chef(IdChef) ON DELETE CASCADE
);

-- Enum per l'origine dell'ingrediente
CREATE TYPE NaturaIngrediente AS ENUM ('Vegetale', 'Animale', 'Minerale', 'Fungino', 'Sintetico', 'Microbico', 'Altro');

-- Ingrediente
CREATE TABLE Ingrediente
(
	IdIngrediente SERIAL PRIMARY KEY,
	Nome VARCHAR(100) UNIQUE NOT NULL,
	Origine NaturaIngrediente NOT NULL,
	CONSTRAINT check_empty_ingr_nome CHECK (LENGTH(Nome) > 0)
);

-- Iscrizioni
CREATE TABLE Iscrizioni
(
	IdPartecipante INTEGER NOT NULL,
	IdCorso INTEGER NOT NULL,
	CONSTRAINT pk_iscrizioni_corso PRIMARY KEY(IdPartecipante, IdCorso),					      	 -- Lo stesso utente non puo' partecipare due volte allo stesso corso allo stesso momento
	CONSTRAINT fk_partecipante_iscrizioni FOREIGN KEY(IdPartecipante) REFERENCES Partecipante(IdPartecipante) ON DELETE CASCADE,
	CONSTRAINT fk_corso_iscrizioni FOREIGN KEY(IdCorso) REFERENCES Corso(IdCorso) ON DELETE CASCADE
);

-- Argomenti_Corso		
CREATE TABLE Argomenti_Corso
(
	IdCorso INTEGER NOT NULL,
	IdArgomento INTEGER NOT NULL,
	CONSTRAINT pk_argomenti_corso PRIMARY KEY(IdCorso, IdArgomento),						        -- Minimo un argomento e niente argomenti ripetuti per corso
	CONSTRAINT fk_corso_argomenticorso FOREIGN KEY(IdCorso) REFERENCES Corso(IdCorso) ON DELETE CASCADE,
	CONSTRAINT fk_argomenti_argomenticorso FOREIGN KEY(IdArgomento) REFERENCES Argomento(IdArgomento) ON DELETE RESTRICT
);

-- Adesioni
CREATE TABLE Adesioni
(
	IdPartecipante INTEGER NOT NULL,
	IdSessionePratica INTEGER NOT NULL,
	DataAdesione DATE NOT NULL,
    CONSTRAINT pk_adesioni PRIMARY KEY(IdPartecipante, IdSessionePratica),
	CONSTRAINT fk_partecipante_adesioni FOREIGN KEY(IdPartecipante) REFERENCES Partecipante(IdPartecipante) ON DELETE CASCADE,
	CONSTRAINT fk_sessionepratica_adesioni FOREIGN KEY(IdSessionePratica) REFERENCES SessionePratica(IdSessionePratica) ON DELETE CASCADE
);

-- Preparazioni
CREATE TABLE Preparazioni
(
	IdSessionePratica INTEGER NOT NULL,
	IdRicetta INTEGER NOT NULL,
	CONSTRAINT pk_sessionepratica_ricetta PRIMARY KEY (IdSessionePratica, IdRicetta),						-- Non si può ripetere la stessa ricetta nella stessa sessione
	CONSTRAINT fk_sessionepratica_preparazioni FOREIGN KEY(IdSessionePratica) REFERENCES SessionePratica(IdSessionePratica) ON DELETE CASCADE,
	CONSTRAINT fk_ricetta_preparazioni FOREIGN KEY(IdRicetta) REFERENCES Ricetta(IdRicetta) ON DELETE CASCADE
);

-- Enumerazione per l'unità di misura degli utilizzi degli ingredienti
CREATE TYPE UnitaDiMisura AS ENUM ('Unita', 'Grammi', 'Kilogrammi', 'Litri', 'Millilitri', 'Bicchiere', 'Tazza', 'Tazzina', 'Cucchiaio', 'Cucchiaino');

-- Utilizzi
CREATE TABLE Utilizzi
(
	IdRicetta INTEGER NOT NULL,
	IdIngrediente INTEGER NOT NULL,
	Quantita FLOAT8 NOT NULL,										            -- Per una porzione!
	UDM	UnitaDiMisura NOT NULL,																	
	CONSTRAINT pk_ricetta_ingrediente PRIMARY KEY(IdRicetta, IdIngrediente),	-- Gli ingredienti non devono essere ripetuti per la stessa ricetta e ce ne deve essere almeno 1
	CONSTRAINT fk_ricetta_utilizzi FOREIGN KEY(IdRicetta) REFERENCES Ricetta(IdRicetta) ON DELETE CASCADE,
	CONSTRAINT fk_ingrediente_utilizzi FOREIGN KEY(IdIngrediente) REFERENCES Ingrediente(IdIngrediente) ON DELETE RESTRICT,
	CONSTRAINT check_quantita CHECK (Quantita > 0)
);


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
    UPDATE Corso
    SET NumeroSessioni = GREATEST(NumeroSessioni - 1, 0)
    WHERE IdCorso = OLD.IdCorso;
	RAISE NOTICE 'POORCDDIDIDIDI';
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


-- La data della sessione non puo essere prima della data inizio corso e se non ci sono sessioni allora deve essere inserita il giorno di inizio corso

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
	RAISE NOTICE 'IF NON PARTITO DIOCANE';
    IF (SELECT FrequenzaSessioni FROM Corso WHERE IdCorso = OLD.IdCorso) <> 'Libera' THEN
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
BEGIN

    IF TG_TABLE_NAME = 'sessionepratica' THEN
        id_sessione_corrente := NEW.IdSessionePratica;
    ELSIF TG_TABLE_NAME = 'sessioneonline' THEN
        id_sessione_corrente := NEW.IdSessioneOnline;
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
            
            WHERE (S.IdSessione <> id_sessione_corrente) AND NOT ((S.Orario + (S.Durata || ' minutes')::interval)::time <= inizio_nuova OR S.Orario >= fine_nuova)
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
BEFORE DELETE OR UPDATE  OF Nome ON Argomento
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



-- 01_Insert: Popolamento Tabelle

-- Partecipante
INSERT INTO Partecipante (Username, Nome, Cognome, CodiceFiscale, DataDiNascita, LuogoDiNascita, Email, Password) VALUES
('kiyo', 'Giuseppe Paolo', 'Esposito', 'SPSGPP03H17F839U', '2003-06-17', 'Napoli', 'giuseppep.esposito@studenti.unina.it', '$2a$11$YDepwIlVMHeru34z47crB.QsrpEc5CppCIV97XiIGPPHCBrCarjIm'),
('virgi', 'Virginia Antonia', 'Esposito', 'SPSVGN04M61G964D', '2004-08-21', 'Pozzuoli', 'virginiaa.esposito@studenti.unina.it', '$2a$11$V6d/Gymya1BzSmrPP/cZdepkOhmjf9uLgE.dyF6TQNFaOXlqskyPm'),
('gennarino', 'Gennaro', 'Iacuaniello', 'CNLGNR04B09F839J', '2004-02-09', 'Napoli', 'gennaro.iacuaniello@studenti.unina.it', '$2a$11$n4s8ouFWZBdDMa3YEmZpHO4ayn4tjIZ51u7Ih2u8gVcrReNDipQNa'),
('chicolococon', 'Luca', 'Fiorentino Heredia', 'FRTLCA03H31F839C', '2003-05-31', 'Lima', 'luca.fiorentinoh@studenti.unina.it', '$2a$11$YfEJqbB/fS54uOyMjQUkme1e.nOWY2DlmDRwLpF3.oJLzHbWrcT/G'),
('mariorossi', 'Mario', 'Rossi', 'RSSMRA75B01L219D', '1975-05-15', 'Milano', 'mario.rossi@example.com', '$2a$11$aZLcelUJOmXHchJrtP.vh.ncwE9jVTDpqZOdvkpbjEwzBoOpGJlvy'),
('laurabianchi', 'Laura', 'Bianchi', 'BNCLRA80C01H501E', '1980-11-20', 'Torino', 'laura.bianchi@example.com', '$2a$11$N5Jdvrmk2SICJQ7quO0yh.d4wmRi5RMTiF35H7toSZGGx.k9X0dCu'),
('francescoverdi', 'Francesco', 'Verdi', 'VRDFRC92A01F839F', '1992-03-08', 'Firenze', 'francesco.verdi@example.com', '$2a$11$Cbci8Yov.rBousvTBhTTUOFO90c2OxIwNy/0.XT3JR4DTSQpYQJvS'),
('elenagallo', 'Elena', 'Gallo', 'GLLLEN88D01A123G', '1988-07-25', 'Bologna', 'elena.gallo@example.com', '$2a$11$JZN2itw4SFjxiqsJTBTg3.gUO.ZtV99X1ThEcG/BAVhIH9Y6ckqEe'),
('andrearizzo', 'Andrea', 'Rizzo', 'RZZNDR70E01L789H', '1970-09-10', 'Palermo', 'andrea.rizzo@example.com', '$2a$11$HTJU4J9RLOQ4F4dzt/sOr.N9ezjSQLS.4l4W47mP.8O9R6M/9utSi'),
('sofianeri', 'Sofia', 'Neri', 'NRESFO95F01F839I', '1995-01-30', 'Genova', 'sofia.neri@example.com', '$2a$11$zNMrIG.VpPVywSBwaUxinONsAMb0oNUU671NOalkoDgHThHxsm7Ba'),
('giacomobruno', 'Giacomo', 'Bruno', 'BRNGCM85G01C345J', '1985-04-03', 'Bari', 'giacomo.bruno@example.com', '$2a$11$a45u.8KLUr1CkbzHSEJTBObOr97iXDwUNh5XdizPDzP/epXKvgRKm'),
('chiaraconti', 'Chiara', 'Conti', 'CNTCHR90H01B678K', '1990-08-12', 'Venezia', 'chiara.conti@example.com', '$2a$11$uvF/bqsy8N2YoT/1vVvOvuV7s7t61v4UYSTlYNnuMuf8k3DcfwlOG');

-- Chef
INSERT INTO Chef (Username, Nome, Cognome, CodiceFiscale, DataDiNascita, LuogoDiNascita, Email, Password, Curriculum) VALUES
('nonnaada', 'Adelaide', 'Salemme', 'SLMADL35M61G964D', '1935-05-06', 'Bacoli', 'nonnaada@libero.it', '$2a$11$Fmjn15a9L868IBwL9ueeoesySF8.FCYV5FZRpJpSPOVooYuVi35v6', 'resources\nonnaada\Curriculum.pdf'),
('massimobottura', 'Massimo', 'Bottura', 'BTTMSS62A01F839D', '1962-09-30', 'Modena', 'massimo.bottura@osteriaconcesco.com', '$2a$11$cO0F5REHON21drgTmtSJWOEQSfKONJXAXSqF2DnfdbtpzVtUyhb9K', 'resources\massimobottura\Curriculum.pdf'),
('alainducasse', 'Alain', 'Ducasse', 'DCSALN56F01I839I', '1956-09-13', 'Orthez', 'alain.ducasse@ducasse-paris.com', '$2a$11$fxBEchDG12Q16TAHRvgiaeQiAEH5gKtmBkefGSKbQi2h/zJPUD/B.', 'resources\alainducasse\Curriculum.pdf'),
('gordonramsay', 'Gordon', 'Ramsay', 'RMSGRD66D01G839G', '1966-11-08', 'Johnstone', 'gordon.ramsay@hellskitchen.com', '$2a$11$EH4uO2WR82IW7/Fp42FwdeItsjt.u6Y/V/HvFGcKyS.c2RQxpLfhW', 'resources\gordonramsay\Curriculum.pdf'),
('thomasmkeller', 'Thomas', 'Keller', 'KLLTHM55H01K123L', '1955-10-14', 'Oceanside', 'thomas.keller@frenchlaundry.com', '$2a$11$o4aFhQnOuAaPWwfKUqYDluyCgWM/Ib.oza2UD9cgDOHBSbvUZtkJW', 'resources\thomasmkeller\Curriculum.pdf'),
('reneredzepi', 'René', 'Redzepi', 'RDZRNE77I01M456N', '1977-12-16', 'Copenhagen', 'rene.redzepi@noma.dk', '$2a$11$yggwVjNq5l1Rm19dNbusfurYWyUvLBMjRwTBY1COy4atNnlriNZQa', 'resources\reneredzepi\Curriculum.pdf'),
('hestonblumen', 'Heston', 'Blumenthal', 'BLMHST66M01C567P', '1966-05-17', 'Londra', 'heston.blumenthal@thefatduck.com', '$2a$11$7TsXx1hQYoR.k2SHc71Ede38EEZruGBX6NOte/sAwlGxE6hdBQYry', 'resources\hestonblumen\Curriculum.pdf'),
('ennicocrippa', 'Enrico', 'Crippa', 'CRPECC69N01D789Q', '1969-10-09', 'Carate Brianza', 'enrico.crippa@piazzaduomoalba.com', '$2a$11$Qt/s5FdvdiFWO/kdYCLblev72J.zsBCEKq4wLQYjibeLngHHn/M/u', 'resources\ennicocrippa\Curriculum.pdf'),
('wolfgangpuck', 'Wolfgang', 'Puck', 'PUCKWLF49O01E901R', '1949-07-08', 'Sankt Veit an der Glan', 'wolfgang.puck@wolfgangpuck.com', '$2a$11$5CswY2gPr/R6m1kyp0XO7.7W44g46o5scNuzD8WmQwD6ERVvCgNsa', 'resources\wolfgangpuck\Curriculum.pdf'),
('danielhumm', 'Daniel', 'Humm', 'HUMMDNL76P01F012S', '1976-09-21', 'Strengelbach', 'daniel.humm@elevenmadisonpark.com', '$2a$11$Jn/d/KENObtiS5pKIBgOmehr8/A9LyX9QSp2sVuwM681G4VYBOzCC', 'resources\danielhumm\Curriculum.pdf'),
('carlo.cracco', 'Carlo', 'Cracco', 'CRCCRL65B01F839E', '1965-10-08', 'Vicenza', 'carlo.cracco@craccoristorante.com', '$2a$11$nP50i1baxnfbpkHj28Aq3ujXFE9jtoD5NSyTZbik.pFm3lZnmgnXu', 'resources\carlo.cracco\Curriculum.pdf'),
('antoninochef', 'Antonino', 'Cannavacciuolo', 'CNNANT75C01F839F', '1975-04-16', 'Vico Equense', 'antonino.cannavacciuolo@villacrespi.com', '$2a$11$EXV80HVOBGJveVixz1yHvOl14/KUWW738n55XYhlWWrFp1qSZ.ZhG', 'resources\antoninochef\Curriculum.pdf'),
('brunobarbieri', 'Bruno', 'Barbieri', 'BRNBRN62P01E839X', '1962-01-12', 'Medicina', 'bruno.barbieri@example.com', '$2a$11$DteBv8gwVQlJ5lOzarVMuO3jfn31Vn8OzWLYrVcErO.UnJ53XWUT2', 'resources\brunobarbieri\Curriculum.pdf'),
('antonellocolonna', 'Antonello', 'Colonna', 'CLNANT56A01F987Y', '1956-07-27', 'Roma', 'antonello.colonna@resort.com', '$2a$11$AE7ueEshrsDNstUgg4Mie.fp8TAVePMFs8ie2EWBSrTEmR.ujwr5K', 'resources\antonellocolonna\Curriculum.pdf'),
('angelofumagalli', 'Angelo', 'Fumagalli', 'FUMANG70B01G123Z', '1970-03-05', 'Milano', 'angelo.fumagalli@ristorante.com', '$2a$11$iBbJyUfEBPxWLnovE0DJTOFkZGHN.y1h1M2RVkPORjTj07ESnDyOS', 'resources\angelofumagalli\Curriculum.pdf'),
('mmorimoto', 'Masaharu', 'Morimoto', 'MRMMSH55B22Z404Y', '1955-05-22', 'Hiroshima', 'm.morimoto@chefmail.com', '$2a$11$uhy.GNMQCKh8HAPUtqwV.uuF8kG0ffaSWXhgy4o8Nt97jt.Z1U.Zu', 'resources\mmorimoto\Curriculum.pdf'),
('nigi.nigella', 'Nigella', 'Lawson', 'LWSNGL60E01H839H', '1960-01-06', 'Londra', 'nigella.lawson@cookbooks.com', '$2a$11$U285Po7uja1bmPrUCPnDau7zJDbv3xEWj0FFZvpfZM6A800R6DDQ.', 'resources\nigi.nigella\Curriculum.pdf'),
('ynarisawa', 'Yoshihiro', 'Narisawa', 'NRSYSH70A01Z404X', '1970-03-01', 'Nagoya', 'y.narisawa@chefmail.com', '$2a$11$0K823jpBqhI.XBhyBL2lDu4enSqY.WOwufEDDt.kR1A1okFyViD9m', 'resources\ynarisawa\Curriculum.pdf'),
('cristina.bowerman', 'Cristina', 'Bowerman', 'BWRCRS70G01L839J', '1970-07-26', 'Taranto', 'cristina.bowerman@glasshostaria.com', '$2a$11$VG4aquAOprHuMIP7FrSYFeZVdImIPXSYEBkahSmi3fVUf5tQEsNBe', 'resources\cristina.bowerman\Curriculum.pdf');

-- Argomento
INSERT INTO Argomento (Nome) VALUES
('Cucina'),
('Pasticceria'),
('Panificazione e Grandi Lievitati'),
('Finger Food e Aperitivi'),
('Street Food'),
('Molecolare'),
('Cioccolateria'),
('Etica e Sostenibile'),
('Tecniche di Base e Avanzate'),
('Basi per Principianti'),
('Food Styling e Presentazione dei Piatti'),
('Vegetariana'),
('Vegana'),
('Senza Glutine'),
('Senza Lattosio'),
('Fusion e Creativa'),
('Veloce e Quotidiana'),
('Fermentata e Conservazione Naturale'),
('Eventi e Feste'),
('Benessere e Light'),
('Bambini e Famiglie'),
('Erbe Aromatiche e Spezie'),
('Internazionale'),
('Italiana'),
('Francese'),
('Spagnola'),
('Portoghese'),
('Giapponese'),
('Cinese'),
('Indiana'),
('Thailandese'),
('Vietnamita'),
('Messicana'),
('Africana'),
('USA'),
('Brasiliana'),
('Argentina'),
('Sudamericana'),
('Nordamericana'),
('Mediterranea'),
('Regionale'),
('Europea');

-- Corso
INSERT INTO Corso (Nome, DataInizio, NumeroSessioni, FrequenzaSessioni, Limite, Descrizione, Costo, isPratico, IdChef) VALUES

('In cucina con Nonna', '2026-06-17', 0, 'Settimanale', 34, 'Cucina classica napoletana con ingredienti tipici della tradizione', 90.0, true, (SELECT IdChef FROM Chef WHERE Username = 'nonnaada')),
('La tradizione giapponese', '2026-05-17', 0, 'Mensile', 50, 'Piatti e dolci tipici giapponesi', 250.0, true, (SELECT IdChef FROM Chef WHERE Username = 'mmorimoto')),
('Cucina Fusion Asiatica', '2026-11-12', 0, 'Settimanale', 30, 'Fusione tra cucina asiatica e italiana con tecniche innovative', 200.0, true, (SELECT IdChef FROM Chef WHERE Username = 'ynarisawa')),
('Il messico in casa tua', '2025-08-17', 0, 'Settimanale', NULL, 'Guacamole, zuppa di fagioli e involtini', 100.0, false, (SELECT IdChef FROM Chef WHERE Username = 'wolfgangpuck')),
('Piatti semplici per principianti', '2026-01-01', 0, 'Giornaliera', 90, 'Se non riesci a cuocere neanche un uovo senza far scoppiare la cucina, questo è il corso per te!!', 30.0, true, (SELECT IdChef FROM Chef WHERE Username = 'ennicocrippa')),
('Street Food Internazionale', '2027-01-15', 0, 'Settimanale', 35, 'Tecniche per creare street food di qualità da diverse tradizioni', 90.0, true, (SELECT IdChef FROM Chef WHERE Username = 'wolfgangpuck')),
('Cucina Molecolare Base', '2025-08-20', 0, 'Bisettimanale', NULL, 'Introduzione alle tecniche di gastronomia molecolare e sferificazione', 320.0, false, (SELECT IdChef FROM Chef WHERE Username = 'hestonblumen')),
('Cucina per Bambini', '2027-04-05', 0, 'Settimanale', 40, 'Ricette sane e divertenti per coinvolgere i più piccoli', 80.0, true, (SELECT IdChef FROM Chef WHERE Username = 'antoninochef')),
('Panificazione Artesanale', '2026-10-05', 0, 'Settimanale', NULL, 'Tecniche tradizionali e moderne per pane e grandi lievitati', 140.0, false, (SELECT IdChef FROM Chef WHERE Username = 'brunobarbieri')),
('Cucina Francese Classica', '2027-03-10', 0, 'Bisettimanale', 69, 'Fondamenti teorici della cucina francese tradizionale', 200.0, true, (SELECT IdChef FROM Chef WHERE Username = 'alainducasse'));

-- Ricetta
INSERT INTO Ricetta (Nome, Provenienza, Tempo, Calorie, Difficolta, Allergeni, IdChef) VALUES
('Pizza Margherita', 'Italia', 45, 280, 'Facile', 'Glutine, Lattosio', (SELECT IdChef FROM Chef WHERE Username = 'nonnaada')),
('Risotto Ai Funghi Porcini', 'Italia', 30, 320, 'Medio', 'Lattosio', (SELECT IdChef FROM Chef WHERE Username = 'nonnaada')),
('Sushi Misto', 'Giappone', 90, 300, 'Difficile', 'Pesce', (SELECT IdChef FROM Chef WHERE Username = 'mmorimoto')),
('Ramen Tradizionale', 'Giappone', 120, 350, 'Difficile', 'Glutine, Soia', (SELECT IdChef FROM Chef WHERE Username = 'mmorimoto')),
('Carbonara', 'Italia', 15, 420, 'Medio', 'Uova, Lattosio, Glutine', (SELECT IdChef FROM Chef WHERE Username = 'ennicocrippa')),
('Coq Au Vin', 'Francia', 120, 380, 'Difficile', NULL, (SELECT IdChef FROM Chef WHERE Username = 'alainducasse')),
('Croissant', 'Francia', 180, 400, 'Esperto', 'Glutine, Lattosio, Uova', (SELECT IdChef FROM Chef WHERE Username = 'alainducasse')),
('Arancini Di Riso', 'Italia', 110, 2506, 'Medio', 'Lattosio, Glutine', (SELECT IdChef FROM Chef WHERE Username = 'wolfgangpuck')),
('Hot Dog', 'USA', 55, 379.5, 'Facile', 'Lattosio, Glutine', (SELECT IdChef FROM Chef WHERE Username = 'wolfgangpuck')),
('Tiramisù', 'Italia', 20, 380, 'Facile', 'Uova, Lattosio, Glutine', (SELECT IdChef FROM Chef WHERE Username = 'antoninochef'));

-- SessioneOnline
INSERT INTO SessioneOnline (Durata, Orario, Data, LinkRiunione, IdCorso) VALUES
(120, '10:00', '2026-11-12', 'https://meet.google.com/nop-qrst-uvwx', (SELECT IdCorso FROM Corso WHERE Nome = 'Cucina Fusion Asiatica')), 
(120, '09:00', '2026-11-19', 'https://meet.google.com/fgh-ijkl-mnop', (SELECT IdCorso FROM Corso WHERE Nome = 'Cucina Fusion Asiatica')),
(120, '15:00', '2026-11-26', 'https://meet.google.com/yza-bcde-fghi', (SELECT IdCorso FROM Corso WHERE Nome = 'Cucina Fusion Asiatica')), 
(120, '10:00', '2025-08-17', 'https://meet.google.com/abc-defg-hij', (SELECT IdCorso FROM Corso WHERE Nome = 'Il messico in casa tua')),
(90, '14:30', '2025-08-24', 'https://meet.google.com/klm-nopq-rst', (SELECT IdCorso FROM Corso WHERE Nome = 'Il messico in casa tua')),
(120, '16:00', '2026-10-05', 'https://meet.google.com/xyz-uvwx-yzab', (SELECT IdCorso FROM Corso WHERE Nome = 'Panificazione Artesanale')), 
(90, '18:00', '2026-10-12', 'https://meet.google.com/cde-fghi-jklm', (SELECT IdCorso FROM Corso WHERE Nome = 'Panificazione Artesanale')),
(120, '11:00', '2025-08-20', 'https://meet.google.com/jkl-mnop-qrst', (SELECT IdCorso FROM Corso WHERE Nome = 'Cucina Molecolare Base')),
(90, '17:30', '2025-09-03', 'https://meet.google.com/uvw-xyza-bcde', (SELECT IdCorso FROM Corso WHERE Nome = 'Cucina Molecolare Base')), 
(90, '14:00', '2025-09-17', 'https://meet.google.com/qrs-tuvw-xyza', (SELECT IdCorso FROM Corso WHERE Nome = 'Cucina Molecolare Base')); 

-- SessionePratica
INSERT INTO SessionePratica (Durata, Orario, Data, NumeroPartecipanti, Luogo, IdCorso) VALUES
(180, '09:00', '2026-06-17', 0, 'Laboratorio A - Piano Terra', (SELECT IdCorso FROM Corso WHERE Nome = 'In cucina con Nonna')),
(240, '14:00', '2026-06-24', 0, 'Laboratorio B - Primo Piano', (SELECT IdCorso FROM Corso WHERE Nome = 'In cucina con Nonna')),
(180, '10:00', '2026-05-17', 0, 'Laboratorio C - Secondo Piano', (SELECT IdCorso FROM Corso WHERE Nome = 'La tradizione giapponese')), 
(240, '15:30', '2026-06-17', 0, 'Laboratorio A - Piano Terra', (SELECT IdCorso FROM Corso WHERE Nome = 'La tradizione giapponese')), 
(160, '09:00', '2027-03-10', 0, 'Laboratorio Cucina Francese', (SELECT IdCorso FROM Corso WHERE Nome = 'Cucina Francese Classica')), 
(180, '14:30', '2027-03-24', 0, 'Laboratorio Cucina Francese', (SELECT IdCorso FROM Corso WHERE Nome = 'Cucina Francese Classica')), 
(140, '11:30', '2026-01-01', 0, 'Via Scarlatti 3', (SELECT IdCorso FROM Corso WHERE Nome = 'Piatti semplici per principianti')),
(120, '10:30', '2027-01-15', 0, 'Laboratorio Street Food', (SELECT IdCorso FROM Corso WHERE Nome = 'Street Food Internazionale')), 
(150, '16:00', '2027-01-22', 0, 'Laboratorio Street Food', (SELECT IdCorso FROM Corso WHERE Nome = 'Street Food Internazionale')), 
(140, '11:30', '2027-04-05', 0, 'Via Roma 3', (SELECT IdCorso FROM Corso WHERE Nome = 'Cucina per Bambini'));

-- Iscrizioni (che aggiornerà automaticamente NumeroCorsi tramite trigger)
INSERT INTO Iscrizioni (IdPartecipante, IdCorso) VALUES
((SELECT IdPartecipante FROM Partecipante WHERE Username = 'virgi'), (SELECT IdCorso FROM Corso WHERE Nome = 'In cucina con Nonna')),
((SELECT IdPartecipante FROM Partecipante WHERE Username = 'virgi'), (SELECT IdCorso FROM Corso WHERE Nome = 'La tradizione giapponese')),
((SELECT IdPartecipante FROM Partecipante WHERE Username = 'gennarino'), (SELECT IdCorso FROM Corso WHERE Nome = 'In cucina con Nonna')),
((SELECT IdPartecipante FROM Partecipante WHERE Username = 'gennarino'), (SELECT IdCorso FROM Corso WHERE Nome = 'La tradizione giapponese')),
((SELECT IdPartecipante FROM Partecipante WHERE Username = 'kiyo'), (SELECT IdCorso FROM Corso WHERE Nome = 'Cucina Molecolare Base')),
((SELECT IdPartecipante FROM Partecipante WHERE Username = 'kiyo'), (SELECT IdCorso FROM Corso WHERE Nome = 'Cucina Fusion Asiatica')),
((SELECT IdPartecipante FROM Partecipante WHERE Username = 'chiaraconti'), (SELECT IdCorso FROM Corso WHERE Nome = 'Street Food Internazionale')),
((SELECT IdPartecipante FROM Partecipante WHERE Username = 'chicolococon'), (SELECT IdCorso FROM Corso WHERE Nome = 'Street Food Internazionale')),
((SELECT IdPartecipante FROM Partecipante WHERE Username = 'chicolococon'), (SELECT IdCorso FROM Corso WHERE Nome = 'Cucina per Bambini')),
((SELECT IdPartecipante FROM Partecipante WHERE Username = 'giacomobruno'), (SELECT IdCorso FROM Corso WHERE Nome = 'Il messico in casa tua')),
((SELECT IdPartecipante FROM Partecipante WHERE Username = 'giacomobruno'), (SELECT IdCorso FROM Corso WHERE Nome = 'Panificazione Artesanale')),
((SELECT IdPartecipante FROM Partecipante WHERE Username = 'andrearizzo'), (SELECT IdCorso FROM Corso WHERE Nome = 'Cucina Francese Classica')),
((SELECT IdPartecipante FROM Partecipante WHERE Username = 'elenagallo'), (SELECT IdCorso FROM Corso WHERE Nome = 'Piatti semplici per principianti'));

-- Argomenti_Corso
INSERT INTO Argomenti_Corso (IdCorso, IdArgomento) VALUES
((SELECT IdCorso FROM Corso WHERE Nome = 'In cucina con Nonna'), (SELECT IdArgomento FROM Argomento WHERE Nome = 'Italiana')),
((SELECT IdCorso FROM Corso WHERE Nome = 'In cucina con Nonna'), (SELECT IdArgomento FROM Argomento WHERE Nome = 'Regionale')),
((SELECT IdCorso FROM Corso WHERE Nome = 'In cucina con Nonna'), (SELECT IdArgomento FROM Argomento WHERE Nome = 'Cucina')),
((SELECT IdCorso FROM Corso WHERE Nome = 'La tradizione giapponese'), (SELECT IdArgomento FROM Argomento WHERE Nome = 'Giapponese')),
((SELECT IdCorso FROM Corso WHERE Nome = 'La tradizione giapponese'), (SELECT IdArgomento FROM Argomento WHERE Nome = 'Internazionale')),
((SELECT IdCorso FROM Corso WHERE Nome = 'La tradizione giapponese'), (SELECT IdArgomento FROM Argomento WHERE Nome = 'Pasticceria')),
((SELECT IdCorso FROM Corso WHERE Nome = 'Cucina Molecolare Base'), (SELECT IdArgomento FROM Argomento WHERE Nome = 'Molecolare')),
((SELECT IdCorso FROM Corso WHERE Nome = 'Cucina Molecolare Base'), (SELECT IdArgomento FROM Argomento WHERE Nome = 'Tecniche di Base e Avanzate')),
((SELECT IdCorso FROM Corso WHERE Nome = 'Cucina Molecolare Base'), (SELECT IdArgomento FROM Argomento WHERE Nome = 'Fusion e Creativa')),
((SELECT IdCorso FROM Corso WHERE Nome = 'Panificazione Artesanale'), (SELECT IdArgomento FROM Argomento WHERE Nome = 'Panificazione e Grandi Lievitati')),
((SELECT IdCorso FROM Corso WHERE Nome = 'Panificazione Artesanale'), (SELECT IdArgomento FROM Argomento WHERE Nome = 'Tecniche di Base e Avanzate')),
((SELECT IdCorso FROM Corso WHERE Nome = 'Panificazione Artesanale'), (SELECT IdArgomento FROM Argomento WHERE Nome = 'Italiana')),
((SELECT IdCorso FROM Corso WHERE Nome = 'Cucina Fusion Asiatica'), (SELECT IdArgomento FROM Argomento WHERE Nome = 'Fusion e Creativa')),
((SELECT IdCorso FROM Corso WHERE Nome = 'Cucina Fusion Asiatica'), (SELECT IdArgomento FROM Argomento WHERE Nome = 'Giapponese')),
((SELECT IdCorso FROM Corso WHERE Nome = 'Cucina Fusion Asiatica'), (SELECT IdArgomento FROM Argomento WHERE Nome = 'Cinese')),
((SELECT IdCorso FROM Corso WHERE Nome = 'Street Food Internazionale'), (SELECT IdArgomento FROM Argomento WHERE Nome = 'Street Food')),
((SELECT IdCorso FROM Corso WHERE Nome = 'Street Food Internazionale'), (SELECT IdArgomento FROM Argomento WHERE Nome = 'Internazionale')),
((SELECT IdCorso FROM Corso WHERE Nome = 'Street Food Internazionale'), (SELECT IdArgomento FROM Argomento WHERE Nome = 'Veloce e Quotidiana')),
((SELECT IdCorso FROM Corso WHERE Nome = 'Cucina Francese Classica'), (SELECT IdArgomento FROM Argomento WHERE Nome = 'Francese')),
((SELECT IdCorso FROM Corso WHERE Nome = 'Cucina Francese Classica'), (SELECT IdArgomento FROM Argomento WHERE Nome = 'Europea')),
((SELECT IdCorso FROM Corso WHERE Nome = 'Cucina Francese Classica'), (SELECT IdArgomento FROM Argomento WHERE Nome = 'Tecniche di Base e Avanzate')),
((SELECT IdCorso FROM Corso WHERE Nome = 'Cucina per Bambini'), (SELECT IdArgomento FROM Argomento WHERE Nome = 'Bambini e Famiglie')),
((SELECT IdCorso FROM Corso WHERE Nome = 'Cucina per Bambini'), (SELECT IdArgomento FROM Argomento WHERE Nome = 'Veloce e Quotidiana')),
((SELECT IdCorso FROM Corso WHERE Nome = 'Cucina per Bambini'), (SELECT IdArgomento FROM Argomento WHERE Nome = 'Benessere e Light')),
((SELECT IdCorso FROM Corso WHERE Nome = 'Il messico in casa tua'), (SELECT IdArgomento FROM Argomento WHERE Nome = 'Messicana')),
((SELECT IdCorso FROM Corso WHERE Nome = 'Il messico in casa tua'), (SELECT IdArgomento FROM Argomento WHERE Nome = 'Cucina')),
((SELECT IdCorso FROM Corso WHERE Nome = 'Il messico in casa tua'), (SELECT IdArgomento FROM Argomento WHERE Nome = 'Internazionale')),
((SELECT IdCorso FROM Corso WHERE Nome = 'Piatti semplici per principianti'), (SELECT IdArgomento FROM Argomento WHERE Nome = 'Veloce e Quotidiana')),
((SELECT IdCorso FROM Corso WHERE Nome = 'Piatti semplici per principianti'), (SELECT IdArgomento FROM Argomento WHERE Nome = 'Basi per Principianti')),
((SELECT IdCorso FROM Corso WHERE Nome = 'Piatti semplici per principianti'), (SELECT IdArgomento FROM Argomento WHERE Nome = 'Benessere e Light'));

-- Adesioni (che aggiornerà automaticamente NumeroUtenti tramite trigger)
INSERT INTO Adesioni (IdPartecipante, IdSessionePratica, DataAdesione) VALUES
((SELECT IdPartecipante FROM Partecipante WHERE Username = 'virgi'), (SELECT IdSessionePratica FROM SessionePratica WHERE (Data='2026-06-17' AND IdCorso = (SELECT IdCorso FROM Corso WHERE Nome = 'In cucina con Nonna'))), CURRENT_DATE),
((SELECT IdPartecipante FROM Partecipante WHERE Username = 'virgi'), (SELECT IdSessionePratica FROM SessionePratica WHERE (Data='2026-06-24' AND IdCorso = (SELECT IdCorso FROM Corso WHERE Nome = 'In cucina con Nonna'))), CURRENT_DATE),
((SELECT IdPartecipante FROM Partecipante WHERE Username = 'virgi'), (SELECT IdSessionePratica FROM SessionePratica WHERE (Data='2026-05-17' AND IdCorso = (SELECT IdCorso FROM Corso WHERE Nome = 'La tradizione giapponese'))), CURRENT_DATE), 
((SELECT IdPartecipante FROM Partecipante WHERE Username = 'virgi'), (SELECT IdSessionePratica FROM SessionePratica WHERE (Data='2026-06-17' AND IdCorso = (SELECT IdCorso FROM Corso WHERE Nome = 'La tradizione giapponese'))), CURRENT_DATE),
((SELECT IdPartecipante FROM Partecipante WHERE Username = 'gennarino'), (SELECT IdSessionePratica FROM SessionePratica WHERE (Data='2026-06-17' AND IdCorso = (SELECT IdCorso FROM Corso WHERE Nome = 'In cucina con Nonna'))), CURRENT_DATE),
((SELECT IdPartecipante FROM Partecipante WHERE Username = 'gennarino'), (SELECT IdSessionePratica FROM SessionePratica WHERE (Data='2026-06-24' AND IdCorso = (SELECT IdCorso FROM Corso WHERE Nome = 'In cucina con Nonna'))), CURRENT_DATE), 
((SELECT IdPartecipante FROM Partecipante WHERE Username = 'gennarino'), (SELECT IdSessionePratica FROM SessionePratica WHERE (Data='2026-05-17' AND IdCorso = (SELECT IdCorso FROM Corso WHERE Nome = 'La tradizione giapponese'))), CURRENT_DATE),
((SELECT IdPartecipante FROM Partecipante WHERE Username = 'gennarino'), (SELECT IdSessionePratica FROM SessionePratica WHERE (Data='2026-06-17' AND IdCorso = (SELECT IdCorso FROM Corso WHERE Nome = 'La tradizione giapponese'))), CURRENT_DATE), 
((SELECT IdPartecipante FROM Partecipante WHERE Username = 'chiaraconti'), (SELECT IdSessionePratica FROM SessionePratica WHERE (Data='2027-01-15' AND IdCorso = (SELECT IdCorso FROM Corso WHERE Nome = 'Street Food Internazionale'))), CURRENT_DATE), 
((SELECT IdPartecipante FROM Partecipante WHERE Username = 'chiaraconti'), (SELECT IdSessionePratica FROM SessionePratica WHERE (Data='2027-01-22' AND IdCorso = (SELECT IdCorso FROM Corso WHERE Nome = 'Street Food Internazionale'))), CURRENT_DATE), 
((SELECT IdPartecipante FROM Partecipante WHERE Username = 'chicolococon'), (SELECT IdSessionePratica FROM SessionePratica WHERE (Data='2027-01-15' AND IdCorso = (SELECT IdCorso FROM Corso WHERE Nome = 'Street Food Internazionale'))), CURRENT_DATE), 
((SELECT IdPartecipante FROM Partecipante WHERE Username = 'chicolococon'), (SELECT IdSessionePratica FROM SessionePratica WHERE (Data='2027-01-22' AND IdCorso = (SELECT IdCorso FROM Corso WHERE Nome = 'Street Food Internazionale'))), CURRENT_DATE), 
((SELECT IdPartecipante FROM Partecipante WHERE Username = 'chicolococon'), (SELECT IdSessionePratica FROM SessionePratica WHERE (Data='2027-04-05' AND IdCorso = (SELECT IdCorso FROM Corso WHERE Nome = 'Cucina per Bambini'))), CURRENT_DATE),
((SELECT IdPartecipante FROM Partecipante WHERE Username = 'andrearizzo'), (SELECT IdSessionePratica FROM SessionePratica WHERE (Data='2027-03-10' AND IdCorso = (SELECT IdCorso FROM Corso WHERE Nome = 'Cucina Francese Classica'))), CURRENT_DATE), 
((SELECT IdPartecipante FROM Partecipante WHERE Username = 'andrearizzo'), (SELECT IdSessionePratica FROM SessionePratica WHERE (Data='2027-03-24' AND IdCorso = (SELECT IdCorso FROM Corso WHERE Nome = 'Cucina Francese Classica'))), CURRENT_DATE),
((SELECT IdPartecipante FROM Partecipante WHERE Username = 'elenagallo'), (SELECT IdSessionePratica FROM SessionePratica WHERE (Data='2026-01-01' AND IdCorso = (SELECT IdCorso FROM Corso WHERE Nome = 'Piatti semplici per principianti'))), CURRENT_DATE); 

-- Preparazioni
INSERT INTO Preparazioni (IdSessionePratica, IdRicetta) VALUES
((SELECT IdSessionePratica FROM SessionePratica WHERE (Data='2026-06-17' AND IdCorso = (SELECT IdCorso FROM Corso WHERE Nome = 'In cucina con Nonna'))), (SELECT IdRicetta FROM Ricetta WHERE Nome = 'Pizza Margherita')),
((SELECT IdSessionePratica FROM SessionePratica WHERE (Data='2026-06-24' AND IdCorso = (SELECT IdCorso FROM Corso WHERE Nome = 'In cucina con Nonna'))), (SELECT IdRicetta FROM Ricetta WHERE Nome = 'Risotto Ai Funghi Porcini')),
((SELECT IdSessionePratica FROM SessionePratica WHERE (Data='2026-05-17' AND IdCorso = (SELECT IdCorso FROM Corso WHERE Nome = 'La tradizione giapponese'))), (SELECT IdRicetta FROM Ricetta WHERE Nome = 'Sushi Misto')),
((SELECT IdSessionePratica FROM SessionePratica WHERE (Data='2026-06-17' AND IdCorso = (SELECT IdCorso FROM Corso WHERE Nome = 'La tradizione giapponese'))), (SELECT IdRicetta FROM Ricetta WHERE Nome = 'Ramen Tradizionale')),
((SELECT IdSessionePratica FROM SessionePratica WHERE (Data='2026-01-01' AND IdCorso = (SELECT IdCorso FROM Corso WHERE Nome = 'Piatti semplici per principianti'))), (SELECT IdRicetta FROM Ricetta WHERE Nome = 'Carbonara')),
((SELECT IdSessionePratica FROM SessionePratica WHERE (Data='2027-03-10' AND IdCorso = (SELECT IdCorso FROM Corso WHERE Nome = 'Cucina Francese Classica'))), (SELECT IdRicetta FROM Ricetta WHERE Nome = 'Coq Au Vin')),
((SELECT IdSessionePratica FROM SessionePratica WHERE (Data='2027-03-24' AND IdCorso = (SELECT IdCorso FROM Corso WHERE Nome = 'Cucina Francese Classica'))), (SELECT IdRicetta FROM Ricetta WHERE Nome = 'Croissant')),
((SELECT IdSessionePratica FROM SessionePratica WHERE (Data='2027-01-15' AND IdCorso = (SELECT IdCorso FROM Corso WHERE Nome = 'Street Food Internazionale'))), (SELECT IdRicetta FROM Ricetta WHERE Nome = 'Arancini Di Riso')),
((SELECT IdSessionePratica FROM SessionePratica WHERE (Data='2027-01-22' AND IdCorso = (SELECT IdCorso FROM Corso WHERE Nome = 'Street Food Internazionale'))), (SELECT IdRicetta FROM Ricetta WHERE Nome = 'Hot Dog')),
((SELECT IdSessionePratica FROM SessionePratica WHERE (Data='2027-04-05' AND IdCorso = (SELECT IdCorso FROM Corso WHERE Nome = 'Cucina per Bambini'))), (SELECT IdRicetta FROM Ricetta WHERE Nome = 'Tiramisù'));

-- Ingredienti
INSERT INTO Ingrediente (Nome, Origine) VALUES
('Farina 00', 'Vegetale'),
('Pomodoro San Marzano', 'Vegetale'),
('Mozzarella di Bufala', 'Animale'),
('Basilico', 'Vegetale'),
('Olio Extra Vergine di Oliva', 'Vegetale'),
('Sale', 'Minerale'),
('Lievito di birra fresco', 'Microbico'),
('Acqua', 'Minerale'),
('Riso Carnaroli', 'Vegetale'),
('Funghi Porcini', 'Fungino'),
('Parmigiano Reggiano', 'Animale'),
('Burro', 'Animale'),
('Cipolla', 'Vegetale'),
('Brodo Vegetale', 'Altro'),
('Vino Bianco Secco', 'Altro'),
('Prezzemolo', 'Vegetale'),
('Aglio', 'Vegetale'),
('Riso per Sushi', 'Vegetale'),
('Salmone', 'Animale'),
('Tonno', 'Animale'),
('Gamberi', 'Animale'),
('Alga Nori', 'Vegetale'),
('Aceto di Riso', 'Altro'),
('Zucchero', 'Vegetale'),
('Salsa di Soia', 'Altro'),
('Wasabi', 'Vegetale'),
('Zenzero Marinato', 'Vegetale'),
('Cetriolo', 'Vegetale'),
('Avocado', 'Vegetale'),
('Noodles per Ramen', 'Vegetale'),
('Brodo di Maiale', 'Altro'),
('Fettine di Maiale Arrosto', 'Animale'),
('Uovo Sodo Marinato', 'Animale'),
('Germogli di Bambù', 'Vegetale'),
('Cipollina', 'Vegetale'),
('Miso', 'Microbico'),
('Zenzero', 'Vegetale'),
('Olio di Sesamo', 'Vegetale'),
('Uova', 'Animale'),
('Pecorino Romano', 'Animale'),
('Pepe Nero', 'Vegetale'),
('Guanciale', 'Animale'),
('Spaghetti', 'Vegetale'),
('Coscia di Pollo', 'Animale'),
('Vino Rosso', 'Altro'),
('Pancetta', 'Animale'),
('Cipolline Borettane', 'Vegetale'),
('Funghi Champignon', 'Fungino'),
('Brodo di Pollo', 'Altro'),
('Carota', 'Vegetale'),
('Sedano', 'Vegetale'),
('Timo', 'Vegetale'),
('Alloro', 'Vegetale'),
('Latte', 'Animale'),
('Ragù', 'Altro'),
('Mozzarella', 'Animale'),
('Piselli', 'Vegetale'),
('Zafferano', 'Vegetale'),
('Pangrattato', 'Vegetale'),
('Olio per Friggere', 'Vegetale'),
('Wurstel', 'Altro'),
('Panino per Hot Dog', 'Vegetale'),
('Ketchup', 'Altro'),
('Senape', 'Vegetale'),
('Sottaceti', 'Vegetale'),
('Mascarpone', 'Animale'),
('Caffè Espresso', 'Altro'),
('Savoiardi', 'Vegetale'),
('Cacao Amaro in Polvere', 'Vegetale');

-- Utilizzi
-- Pizza Margherita
INSERT INTO Utilizzi (IdRicetta, IdIngrediente, Quantita, UDM) VALUES
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Pizza Margherita'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Farina 00'), 120, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Pizza Margherita'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Pomodoro San Marzano'), 90, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Pizza Margherita'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Mozzarella Di Bufala'), 70, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Pizza Margherita'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Basilico'), 5, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Pizza Margherita'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Olio Extra Vergine Di Oliva'), 1, 'Cucchiaio'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Pizza Margherita'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Sale'), 0.5, 'Cucchiaino'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Pizza Margherita'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Lievito Di Birra Fresco'), 4, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Pizza Margherita'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Acqua'), 70, 'Millilitri'),

-- Risotto ai Funghi Porcini
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Risotto Ai Funghi Porcini'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Riso Carnaroli'), 90, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Risotto Ai Funghi Porcini'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Funghi Porcini'), 110, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Risotto Ai Funghi Porcini'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Parmigiano Reggiano'), 25, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Risotto Ai Funghi Porcini'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Burro'), 18, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Risotto Ai Funghi Porcini'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Cipolla'), 30, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Risotto Ai Funghi Porcini'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Brodo Vegetale'), 350, 'Millilitri'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Risotto Ai Funghi Porcini'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Vino Bianco Secco'), 30, 'Millilitri'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Risotto Ai Funghi Porcini'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Prezzemolo'), 5, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Risotto Ai Funghi Porcini'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Aglio'), 1, 'Unita'),

-- Sushi Misto
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Sushi Misto'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Riso Per Sushi'), 180, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Sushi Misto'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Salmone'), 50, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Sushi Misto'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Tonno'), 50, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Sushi Misto'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Gamberi'), 30, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Sushi Misto'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Alga Nori'), 1, 'Unita'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Sushi Misto'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Aceto Di Riso'), 15, 'Millilitri'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Sushi Misto'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Zucchero'), 5, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Sushi Misto'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Sale'), 2, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Sushi Misto'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Salsa Di Soia'), 10, 'Millilitri'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Sushi Misto'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Wasabi'), 5, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Sushi Misto'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Zenzero Marinato'), 10, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Sushi Misto'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Cetriolo'), 20, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Sushi Misto'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Avocado'), 20, 'Grammi'),

-- Ramen Tradizionale
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Ramen Tradizionale'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Noodles Per Ramen'), 180, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Ramen Tradizionale'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Brodo Di Maiale'), 450, 'Millilitri'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Ramen Tradizionale'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Fettine Di Maiale Arrosto'), 90, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Ramen Tradizionale'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Uovo Sodo Marinato'), 1, 'Unita'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Ramen Tradizionale'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Alga Nori'), 1, 'Unita'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Ramen Tradizionale'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Germogli Di Bambù'), 30, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Ramen Tradizionale'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Cipollina'), 10, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Ramen Tradizionale'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Salsa Di Soia'), 18, 'Millilitri'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Ramen Tradizionale'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Miso'), 15, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Ramen Tradizionale'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Zenzero'), 5, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Ramen Tradizionale'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Aglio'), 5, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Ramen Tradizionale'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Olio Di Sesamo'), 5, 'Millilitri'),

-- Carbonara
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Carbonara'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Uova'), 2, 'Unita'), 
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Carbonara'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Pecorino Romano'), 55, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Carbonara'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Pepe Nero'), 2.5, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Carbonara'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Guanciale'), 90, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Carbonara'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Spaghetti'), 110, 'Grammi'),

-- Coq au Vin
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Coq Au Vin'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Coscia Di Pollo'), 1, 'Unita'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Coq Au Vin'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Vino Rosso'), 220, 'Millilitri'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Coq Au Vin'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Pancetta'), 50, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Coq Au Vin'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Cipolline Borettane'), 90, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Coq Au Vin'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Funghi Champignon'), 100, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Coq Au Vin'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Brodo Di Pollo'), 100, 'Millilitri'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Coq Au Vin'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Carota'), 50, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Coq Au Vin'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Sedano'), 30, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Coq Au Vin'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Farina 00'), 10, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Coq Au Vin'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Burro'), 15, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Coq Au Vin'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Aglio'), 1, 'Unita'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Coq Au Vin'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Timo'), 1, 'Unita'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Coq Au Vin'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Alloro'), 1, 'Unita'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Coq Au Vin'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Sale'), 3, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Coq Au Vin'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Pepe Nero'), 2, 'Grammi'),

-- Croissant
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Croissant'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Farina 00'), 50, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Croissant'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Burro'), 30, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Croissant'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Acqua'), 25, 'Millilitri'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Croissant'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Latte'), 10, 'Millilitri'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Croissant'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Zucchero'), 5, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Croissant'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Lievito Di Birra Fresco'), 2, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Croissant'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Sale'), 1, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Croissant'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Uova'), 1, 'Unita'),

-- Arancini di riso
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Arancini Di Riso'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Riso Carnaroli'), 80, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Arancini Di Riso'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Ragù'), 40, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Arancini Di Riso'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Mozzarella'), 20, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Arancini Di Riso'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Piselli'), 10, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Arancini Di Riso'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Burro'), 10, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Arancini Di Riso'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Parmigiano Reggiano'), 10, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Arancini Di Riso'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Zafferano'), 0.05, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Arancini Di Riso'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Uova'), 0.5, 'Unita'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Arancini Di Riso'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Pangrattato'), 30, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Arancini Di Riso'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Olio Per Friggere'), 100, 'Millilitri'),

-- Hot Dog
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Hot Dog'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Wurstel'), 1, 'Unita'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Hot Dog'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Panino Per Hot Dog'), 1, 'Unita'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Hot Dog'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Ketchup'), 20, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Hot Dog'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Senape'), 15, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Hot Dog'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Cipolla'), 20, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Hot Dog'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Sottaceti'), 15, 'Grammi'),

-- Tiramisù
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Tiramisù'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Uova'), 1, 'Unita'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Tiramisù'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Zucchero'), 25, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Tiramisù'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Mascarpone'), 50, 'Grammi'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Tiramisù'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Caffè Espresso'), 50, 'Millilitri'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Tiramisù'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Savoiardi'), 3, 'Unita'),
((SELECT IdRicetta FROM Ricetta WHERE Nome = 'Tiramisù'), (SELECT IdIngrediente FROM Ingrediente WHERE Nome = 'Cacao Amaro In Polvere'), 5, 'Grammi');