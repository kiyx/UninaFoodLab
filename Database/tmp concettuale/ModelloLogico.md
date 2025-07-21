<head>
  <style>
    fk {
      text-decoration: underline;
      text-decoration-color: black;
      text-decoration-style: double;
      text-decoration-skip-ink: none;
      text-underline-offset: 4px; /* Sposta la sottolineatura più in basso */
      display: inline;
    }
    pk {
      text-decoration: underline;
      text-decoration-color: black;
      text-decoration-skip-ink: none;
      text-underline-offset: 4px; /* Sposta la sottolineatura più in basso */
      display: inline;
    }
    /* CTRL + SHIFT + V */
  </style>
</head>

Partecipante(<pk>IdPartecipante</pk>, Username, Nome, Cognome, CodiceFiscale, DataDiNascita, LuogoDiNascita, Email, Password, NumeroCorsi)
Chef(<pk>IdChef</pk>, Username, Nome, Cognome, CodiceFiscale, DataDiNascita, LuogoDiNascita, Email, Password, Curriculum)
Corso(<pk>IdCorso</pk>, Nome, DataInizio, NumeroSessioni, FrequenzaSessioni, Limite, Descrizione, Costo, isPratico, <fk>IdChef</fk>)
Argomento(<pk>IdArgomento</pk>, Nome)
SessionePratica(<pk>IdSessionePratica</pk>, Durata, Orario, Data, NumeroUtenti, Luogo, <fk>IdCorso</fk>)
SessioneOnline(<pk>IdSessioneOnline</pk>, Durata, Orario, Data, LinkRiunione, <fk>IdCorso</fk>)
Ricetta(<pk>IdRicetta</pk>, Nome, Provenienza, Tempo, Calorie, Difficoltà, Allergeni, <fk>IdChef</fk>)
Ingrediente(<pk>IdIngrediente</pk>, Nome, Origine)
Iscrizioni(<fk>IdPartecipante</fk>, <fk>IdCorso</fk>)
Argomenti_Corso(<pk><fk>IdCorso</fk>, <fk>IdArgomento</fk></pk>)
Adesioni(<fk>IdPartecipante</fk>, <fk>IdSessionePratica</fk>, DataAdesione)
Preparazioni(<fk>IdSessionePratica</fk>, <fk>IdRicetta</fk>)
Utilizzi(<pk><fk>IdRicetta</fk>, <fk>IdIngrediente</fk></pk>, Quantità, UDM)