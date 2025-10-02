package UninaFoodLab.DTO;

import java.util.ArrayList;
import java.time.LocalDate;

public class Partecipante extends Utente
{

    private ArrayList<Corso> corsi;
    private ArrayList<Adesione> adesioni;

    public Partecipante(String username, String nome, String cognome, String codiceFiscale, LocalDate dataDiNascita, String luogoDiNascita, String email, String password, ArrayList<Corso> corsi, ArrayList<Adesione> adesioni)
    {
        super(username, nome, cognome, codiceFiscale, dataDiNascita, luogoDiNascita, email, password);

        if(corsi != null)
        {
            this.corsi = corsi;
        }
        else
        {
            this.corsi = new ArrayList<>();
        }

        this.adesioni = (adesioni != null) ? adesioni : new ArrayList<>();
    }

    public ArrayList<Corso> getCorsi()
    {
        return corsi;
    }

    public void aggiungiCorso(Corso toAddCorso)
    {
        corsi.add(toAddCorso);
    }

    public ArrayList<Adesione> getAdesioni()
    {
        return adesioni;
    }

    public void aggiungiAdesione(Adesione toAddAdesione)
    {
        adesioni.add(toAddAdesione);
    }
}