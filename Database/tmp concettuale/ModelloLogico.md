<head>
  <style>
    body {
      /* Font stack professionale e standard per la massima compatibilità e leggibilità */
      font-family: "Segoe UI", "Helvetica Neue", Arial, sans-serif;
      line-height: 1.7;
      color: #212529; /* Nero non assoluto per ridurre l'affaticamento visivo */
    }
    h3 {
      border-bottom: 1px solid #dee2e6; /* Bordo più sottile e chiaro */
      padding-bottom: 0.5em;
      margin-top: 32px;
      margin-bottom: 16px;
      font-weight: 600;
    }
    pk {
      /* Solo sottolineatura singola, senza grassetto */
      text-decoration: underline;
      text-decoration-style: solid;
      text-underline-offset: 3px;
    }
    fk {
      /* Solo sottolineatura doppia, senza grassetto */
      text-decoration: underline;
      text-decoration-style: double;
      text-underline-offset: 3px;
    }
    .entity-definition {
      /* Font monospace standard per definizioni di codice/schema */
      font-family: "Consolas", "Menlo", "Courier New", monospace;
      font-size: 0.95em;
      margin-bottom: 8px;
    }
    .constraints {
      font-size: 0.9em;
      color: #343a40; /* Testo scuro per alta leggibilità */
      padding-left: 20px;
      margin-top: 8px;
      border-left: 2px solid #e9ecef;
    }
    .constraints ul {
        list-style-type: none;
        padding-left: 0;
        margin: 0;
    }
    .constraints li {
        margin-bottom: 4px;
    }
  </style>
</head>

### Partecipante
<div class="entity-definition">
Partecipante(<pk>IdPartecipante</pk>, Username, Nome, Cognome, CodiceFiscale, DataDiNascita, LuogoDiNascita, Email, Password, NumeroCorsi)
</div>

### Chef
<div class="entity-definition">
Chef(<pk>IdChef</pk>, Username, Nome, Cognome, CodiceFiscale, DataDiNascita, LuogoDiNascita, Email, Password, Curriculum)
</div>

### Corso
<div class="entity-definition">
Corso(<pk>IdCorso</pk>, Nome, DataInizio, NumeroSessioni, FrequenzaSessioni, Limite, Descrizione, Costo, isPratico, <fk>IdChef</fk>)
</div>
<div class="constraints">
  Chiavi esterne:
  <ul>
    <li><fk>IdChef</fk> → Chef(<pk>IdChef</pk>)</li>
  </ul>
</div>

### Argomento
<div class="entity-definition">
Argomento(<pk>IdArgomento</pk>, Nome)
</div>

### SessionePratica
<div class="entity-definition">
SessionePratica(<pk>IdSessionePratica</pk>, Durata, Orario, Data, NumeroUtenti, Luogo, <fk>IdCorso</fk>)
</div>
<div class="constraints">
  Chiavi esterne:
  <ul>
    <li><fk>IdCorso</fk> → Corso(<pk>IdCorso</pk>)</li>
  </ul>
</div>

### SessioneOnline
<div class="entity-definition">
SessioneOnline(<pk>IdSessioneOnline</pk>, Durata, Orario, Data, LinkRiunione, <fk>IdCorso</fk>)
</div>
<div class="constraints">
  Chiavi esterne:
  <ul>
    <li><fk>IdCorso</fk> → Corso(<pk>IdCorso</pk>)</li>
  </ul>
</div>

### Ricetta
<div class="entity-definition">
Ricetta(<pk>IdRicetta</pk>, Nome, Provenienza, Tempo, Calorie, Difficoltà, Allergeni, <fk>IdChef</fk>)
</div>
<div class="constraints">
  Chiavi esterne:
  <ul>
    <li><fk>IdChef</fk> → Chef(<pk>IdChef</pk>)</li>
  </ul>
</div>

### Ingrediente
<div class="entity-definition">
Ingrediente(<pk>IdIngrediente</pk>, Nome, Origine)
</div>

### Iscrizioni
<div class="entity-definition">
Iscrizioni(<pk><fk>IdPartecipante</fk></pk>, <pk><fk>IdCorso</fk></pk>)
</div>
<div class="constraints">
  Chiavi esterne:
  <ul>
    <li><fk>IdPartecipante</fk> → Partecipante(<pk>IdPartecipante</pk>)</li>
    <li><fk>IdCorso</fk> → Corso(<pk>IdCorso</pk>)</li>
  </ul>
</div>

### Argomenti_Corso
<div class="entity-definition">
Argomenti_Corso(<pk><fk>IdCorso</fk></pk>, <pk><fk>IdArgomento</fk></pk>)
</div>
<div class="constraints">
  Chiavi esterne:
  <ul>
    <li><fk>IdCorso</fk> → Corso(<pk>IdCorso</pk>)</li>
    <li><fk>IdArgomento</fk> → Argomento(<pk>IdArgomento</pk>)</li>
  </ul>
</div>

### Adesioni
<div class="entity-definition">
Adesioni(<pk><fk>IdPartecipante</fk></pk>, <pk><fk>IdSessionePratica</fk></pk>, DataAdesione)
</div>
<div class="constraints">
  Chiavi esterne:
  <ul>
    <li><fk>IdPartecipante</fk> → Partecipante(<pk>IdPartecipante</pk>)</li>
    <li><fk>IdSessionePratica</fk> → SessionePratica(<pk>IdSessionePratica</pk>)</li>
  </ul>
</div>

### Preparazioni
<div class="entity-definition">
Preparazioni(<pk><fk>IdSessionePratica</fk></pk>, <pk><fk>IdRicetta</fk></pk>)
</div>
<div class="constraints">
  Chiavi esterne:
  <ul>
    <li><fk>IdSessionePratica</fk> → SessionePratica(<pk>IdSessionePratica</pk>)</li>
    <li><fk>IdRicetta</fk> → Ricetta(<pk>IdRicetta</pk>)</li>
  </ul>
</div>

### Utilizzi
<div class="entity-definition">
Utilizzi(<pk><fk>IdRicetta</fk></pk>, <pk><fk>IdIngrediente</fk></pk>, Quantità, UDM)
</div>
<div class="constraints">
  Chiavi esterne:
  <ul>
    <li><fk>IdRicetta</fk> → Ricetta(<pk>IdRicetta</pk>)</li>
    <li><fk>IdIngrediente</fk> → Ingrediente(<pk>IdIngrediente</pk>)</li>
  </ul>
</div>