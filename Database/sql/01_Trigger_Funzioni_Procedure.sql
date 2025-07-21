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

CREATE OR REPLACE TRIGGER trg_normalizza_partecipante
BEFORE INSERT OR UPDATE ON Partecipante
FOR EACH ROW
EXECUTE FUNCTION fun_normalizza_utente();

CREATE OR REPLACE TRIGGER trg_normalizza_chef
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

CREATE OR REPLACE TRIGGER trg_normalizza_ingrediente
BEFORE INSERT OR UPDATE ON Ingrediente
FOR EACH ROW
EXECUTE FUNCTION fun_normalizza_ricetta_ingr_chef();

CREATE OR REPLACE TRIGGER trg_normalizza_ricetta
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

	IF TG_TABLE_NAME = 'Partecipante' THEN
    		IF EXISTS (SELECT 1 FROM Chef WHERE Username = NEW.Username) THEN
    			RAISE EXCEPTION 'Username già usato in Chef';
		END IF;
	END IF;
	
	IF TG_TABLE_NAME = 'Chef' THEN
		IF EXISTS (SELECT 1 FROM Partecipante WHERE Username = NEW.Username) THEN
    			RAISE EXCEPTION 'Username già usato in Partecipante';
		END IF;
	END IF;
	RETURN NEW;

END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_unico_username_partecipante
BEFORE INSERT OR UPDATE ON Partecipante 
FOR EACH ROW
EXECUTE FUNCTION fun_username_unique();

CREATE OR REPLACE TRIGGER trg_unico_username_chef
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

CREATE OR REPLACE TRIGGER trg_setta_numero_sessioni_corsi_iniziali
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

CREATE OR REPLACE TRIGGER trg_incremento_numsessioni_online
AFTER INSERT ON SessioneOnline
FOR EACH ROW
EXECUTE FUNCTION fun_incrementa_num_sessioni();

CREATE OR REPLACE TRIGGER trg_incremento_numsessioni_pratiche
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
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_decrementa_num_sessioni_pratica
AFTER DELETE ON SessionePratica
FOR EACH ROW
EXECUTE FUNCTION fun_decrementa_num_sessioni();

CREATE OR REPLACE TRIGGER trg_decrementa_num_sessioni_online
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

CREATE OR REPLACE TRIGGER trg_ispratico_insert
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

CREATE OR REPLACE TRIGGER trg_setta_numero_partecipanti_iniziali
BEFORE INSERT OR UPDATE ON SessionePratica
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

CREATE OR REPLACE TRIGGER trg_setta_data_adesione
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

CREATE OR REPLACE TRIGGER trg_incremento_numutenti
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

CREATE OR REPLACE TRIGGER trg_decrementa_num_utenti
AFTER DELETE ON Adesioni
FOR EACH ROW
EXECUTE FUNCTION fun_decrementa_num_utenti();

-----------------------------------------------------------------------------------------------------------------------

-- Gestione numero corsi a cui è iscritto il partecipante

-- Se viene inserita un partecipante con un numero iscrizioni diverso da 0, lanciamo una eccezione perchè è gestita automaticamente dal db

CREATE OR REPLACE FUNCTION fun_setta_numero_iscrizioni_iniziali()
RETURNS TRIGGER AS 
$$ 
BEGIN 
		IF NEW.NumeroCorsi <> 0 THEN
			RAISE EXCEPTION 'Il partecipante deve partire con numero di iscrizioni 0! Vengono aggiornate automaticamente';
		END IF;
        RETURN NEW;
END; 
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_setta_numero_iscrizioni_iniziali
BEFORE INSERT ON Partecipante
FOR EACH ROW 
EXECUTE FUNCTION fun_setta_numero_iscrizioni_iniziali();


-- Trigger aggiornamento numero corsi(increment)

CREATE OR REPLACE FUNCTION fun_incrementa_num_iscrizioni()
RETURNS TRIGGER AS
$$    
BEGIN
    UPDATE Partecipante
    SET NumeroCorsi = NumeroCorsi + 1
    WHERE IdPartecipante = NEW.IdPartecipante;
    RETURN NULL;

END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_incrementa_num_iscrizioni
AFTER INSERT ON Iscrizioni
FOR EACH ROW
EXECUTE FUNCTION fun_incrementa_num_iscrizioni();


-- Trigger aggiornamento numero corsi(decrement)

CREATE OR REPLACE FUNCTION fun_decrementa_num_iscrizioni()
RETURNS TRIGGER AS
$$    
BEGIN
    UPDATE Partecipante
    SET NumeroCorsi = GREATEST(NumeroCorsi - 1, 0)
    WHERE IdPartecipante = OLD.IdPartecipante;
    RETURN OLD;

END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_decrementa_num_iscrizioni
AFTER DELETE ON Iscrizioni
FOR EACH ROW
EXECUTE FUNCTION fun_decrementa_num_iscrizioni();

-----------------------------------------------------------------------------------------------------------------------
-- Gestione sessioni (frequenza e orari)

-- Interrelazionale: Non ci possono essere più sessioni per lo stesso corso nello stesso giorno

CREATE OR REPLACE FUNCTION fun_unicita_sessione_giorno()
RETURNS TRIGGER AS
$$
BEGIN
    	IF EXISTS ( SELECT 1 FROM SessioneOnline WHERE Data = NEW.Data AND IdCorso = NEW.IdCorso ) THEN
    		RAISE EXCEPTION 'C''è già una sessione online lo stesso giorno';
	END IF;

    	IF EXISTS ( SELECT 1 FROM SessionePratica WHERE Data = NEW.Data AND IdCorso = NEW.IdCorso) THEN
    		RAISE EXCEPTION 'C''è già una sessione pratica lo stesso giorno';
	END IF;
    	RETURN NEW;

END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_unicita_sessione_online_giorno
BEFORE INSERT OR UPDATE OF Data ON SessioneOnline
FOR EACH ROW
EXECUTE FUNCTION fun_unicita_sessione_giorno();

CREATE OR REPLACE TRIGGER trg_unicita_sessione_pratica_giorno
BEFORE INSERT OR UPDATE OF Data ON SessionePratica
FOR EACH ROW
EXECUTE FUNCTION fun_unicita_sessione_giorno();


--TODO








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

CREATE OR REPLACE TRIGGER trg_limit_argomenti
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

CREATE OR REPLACE TRIGGER trg_limite_iscrizioni 
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

CREATE OR REPLACE TRIGGER trg_iscrizione_before_adesione
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

    	IF (Data_Sessione - NEW.DataAdesione <= 2) THEN
        	RAISE EXCEPTION 'L'' adesione deve essere antecedente (3 giorni) alla data della sessione pratica';
    	END IF;

    	RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_data_adesione
BEFORE INSERT ON Adesioni
FOR EACH ROW
EXECUTE FUNCTION fun_data_adesione();

-----------------------------------------------------------------------------------------------------------------------

-- Interrelazionale: Lo chef non può usare ricette nelle sessioni pratiche che non sono sue

CREATE OR REPLACE FUNCTION fun_ricette_chef_sessione()
RETURNS TRIGGER AS
$$
DECLARE
	id1 Chef.IdChef%TYPE;
	id2 Chef.IdChef%TYPE;
BEGIN
	SELECT IdChef INTO id1 FROM Preparazioni NATURAL JOIN Ricetta WHERE IdRicetta = NEW.IdRicetta;

	SELECT IdChef INTO id2 FROM Preparazioni NATURAL JOIN SessionePratica NATURAL JOIN Corso WHERE IdSessionePratica = NEW.IdSessionePratica;
	
	IF id1 <> id2 THEN
		RAISE EXCEPTION 'Non si possono inserire ricette che non sono dello chef';
	END IF;
	RETURN NEW;

END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_ricette_chef_sessione
BEFORE INSERT ON Preparazioni
FOR EACH ROW
EXECUTE FUNCTION fun_ricette_chef_sessione();













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

CREATE OR REPLACE TRIGGER trg_blocca_aggiorna_chef
BEFORE UPDATE OF CodiceFiscale ON Chef 
FOR EACH ROW
EXECUTE FUNCTION fun_blocca_aggiorna_utente();

CREATE OR REPLACE TRIGGER trg_blocca_aggiorna_partecipante
BEFORE UPDATE OF CodiceFiscale ON Partecipante
FOR EACH ROW
EXECUTE FUNCTION fun_blocca_aggiorna_utente();

-----------------------------------------------------------------------------------------------------------------------

-- Del corso non  è possibile aggiornare IdCorso, Costo, IdChef

CREATE OR REPLACE FUNCTION fun_blocca_aggiorna_corso()
RETURNS TRIGGER AS
$$
BEGIN
    RAISE EXCEPTION 'Non puoi modificare i campi IdCorso, Costo o IdChef.';
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_blocca_aggiorna_corso
BEFORE UPDATE OF IdCorso, Costo, IdChef ON Corso
FOR EACH ROW
EXECUTE FUNCTION fun_blocca_aggiorna_corso();


-- Non si puo cancellare un corso che è attivo o che ha iscritti

CREATE OR REPLACE FUNCTION fun_blocca_delete_corso()
RETURNS TRIGGER AS 
$$
DECLARE 
	num_iscritti INT;
    data_fine_corso DATE;
BEGIN
     SELECT MAX(Data) INTO data_fine_corso
     FROM (
            SELECT Data FROM SessionePratica WHERE IdCorso = OLD.IdCorso
            UNION
            SELECT Data FROM SessioneOnline WHERE IdCorso = OLD.IdCorso
          ) AS date_sessioni;

	IF OLD.DataInizio <= CURRENT_DATE OR (data_fine_corso IS NOT NULL AND CURRENT_DATE < data_fine_corso) THEN
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

CREATE OR REPLACE TRIGGER trg_blocca_delete_corso
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

CREATE OR REPLACE TRIGGER trg_blocca_aggiorna_sessioni_online
BEFORE UPDATE  OF IdSessioneOnline, IdCorso On SessioneOnline
FOR EACH ROW
EXECUTE FUNCTION fun_blocca_aggiorna_sessioni();

CREATE OR REPLACE TRIGGER trg_blocca_aggiorna_sessioni_pratiche
BEFORE UPDATE  OF IdSessionePratica, IdCorso ON SessionePratica
FOR EACH ROW
EXECUTE FUNCTION fun_blocca_aggiorna_sessioni();


-- Non puoi cancellare una sessione passata se il suo corso è ancora attivo

CREATE OR REPLACE FUNCTION fun_blocca_delete_sessioni_passate()
RETURNS TRIGGER 
AS 
$$
DECLARE 
    data_fine_corso DATE;
BEGIN
     SELECT MAX(Data) INTO data_fine_corso
     FROM 
        (
            SELECT Data FROM SessionePratica WHERE IdCorso = OLD.IdCorso
            UNION
            SELECT Data FROM SessioneOnline WHERE IdCorso = OLD.IdCorso
        ) AS date_sessioni;

    IF OLD.Data <= CURRENT_DATE OR (data_fine_corso IS NOT NULL AND CURRENT_DATE < data_fine_corso) THEN
        RAISE EXCEPTION 'Non puoi cancellare una sessione già avvenuta in un corso non ancora terminato';
    END IF;

    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_blocca_delete_sessioni_pratiche_passate
BEFORE DELETE ON SessionePratica
FOR EACH ROW
EXECUTE FUNCTION fun_blocca_delete_sessioni_passate();

CREATE OR REPLACE TRIGGER trg_blocca_delete_sessioni_online_passate
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

CREATE OR REPLACE TRIGGER trg_blocca_aggiorna_argomento
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

CREATE OR REPLACE TRIGGER trg_blocca_aggiorna_argomenticorso
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

CREATE OR REPLACE TRIGGER trg_blocca_aggiorna_ricetta
BEFORE UPDATE  OF IdRicetta, IdChef ON Ricetta
FOR EACH ROW
EXECUTE FUNCTION fun_blocca_aggiorna_ricetta();

-----------------------------------------------------------------------------------------------------------------------

--Degli Ingredienti non deve essere possibile aggiornare IdIngrediente, Nome, Origine

CREATE OR REPLACE FUNCTION fun_blocca_aggiorna_ingrediente()
RETURNS TRIGGER AS
$$
BEGIN
	RAISE EXCEPTION 'Non puoi modificare o cancellare un ingrediente!!!';
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_blocca_aggiorna_ingrediente
BEFORE DELETE OR UPDATE  OF IdIngrediente, Nome, Origine ON Ingrediente
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

CREATE OR REPLACE TRIGGER trg_blocca_aggiorna_utilizzi
BEFORE UPDATE  OF IdRicetta, IdIngrediente ON Utilizzi
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

CREATE OR REPLACE TRIGGER trg_blocca_aggiorna_preparazioni
BEFORE DELETE OR UPDATE OF IdSessionePratica, IdRicetta ON Preparazioni
FOR EACH ROW
EXECUTE FUNCTION fun_blocca_aggiorna_preparazioni();

-----------------------------------------------------------------------------------------------------------------------

-- Non deve essere possibile aggiornare le iscrizioni

CREATE OR REPLACE FUNCTION fun_blocca_aggiorna_iscrizioni()
RETURNS TRIGGER AS
$$
BEGIN
 	RAISE EXCEPTION 'Non puoi modificare i campi IdPartecipante, IdCorso.';
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_blocca_aggiorna_iscrizioni
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

    THEN RAISE EXCEPTION 'Non puoi disiscriverti dal corso perchè hai aderito a una sessione che dista 2 giorni o meno';
    END IF;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_impedisci_disiscrizione
BEFORE DELETE ON Iscrizioni
FOR EACH ROW
EXECUTE FUNCTION fun_impedisci_disiscrizione();


-- Se è possibile disiscriversi vengono tolte le adesioni effettuate da oggi in poi alle sessioni pratiche potrei averne altre i giorni subito successivi

CREATE OR REPLACE FUNCTION fun_gestisci_disiscrizione()
RETURNS TRIGGER AS
$$
BEGIN
        DELETE 
        FROM Adesioni
        WHERE IdPartecipante = OLD.IdPartecipante AND DataAdesione > CURRENT_DATE; 
        RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_gestisci_disiscrizione
AFTER DELETE ON Iscrizioni
FOR EACH ROW
EXECUTE FUNCTION fun_gestisci_disiscrizione();

-----------------------------------------------------------------------------------------------------------------------

-- Non deve essere possibile aggiornare le adesioni

CREATE OR REPLACE FUNCTION fun_blocca_aggiorna_adesioni()
RETURNS TRIGGER AS
$$
BEGIN
 	RAISE EXCEPTION 'Non puoi modificare i campi IdPartecipante, IdSessionePratica, DataAdesione.';
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_blocca_aggiorna_adesioni
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

CREATE OR REPLACE TRIGGER trg_blocca_cancella_adesioni
BEFORE DELETE ON Adesioni
FOR EACH ROW
EXECUTE FUNCTION fun_blocca_cancella_adesioni();

-----------------------------------------------------------------------------------------------------------------------