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