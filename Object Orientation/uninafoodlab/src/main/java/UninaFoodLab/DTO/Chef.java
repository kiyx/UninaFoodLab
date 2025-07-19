package UninaFoodLab.DTO;

import java.util.ArrayList;
import java.time.LocalDate;

public class Chef extends Utente
{
    private String curriculum;
    private ArrayList<Ricetta> ricette;
    private ArrayList<Corso> corsi;

    public Chef(String username, String nome, String cognome, String codiceFiscale, LocalDate dataDiNascita, String luogoDiNascita, String email, String password, String curriculum, ArrayList<Ricetta> ricette, ArrayList<Corso> corsi)
    {
        super(username, nome, cognome, codiceFiscale, dataDiNascita, luogoDiNascita, email, password);
        this.curriculum = curriculum;
        this.ricette = (ricette != null) ? ricette : new ArrayList<>();
        this.corsi = (corsi != null) ? corsi : new ArrayList<>();
    }

    public String getCurriculum()
    {
        return curriculum;
    }

    public void setCurriculum(String curriculum)
    {
        this.curriculum = curriculum;
    }

    public ArrayList<Ricetta> getRicette()
    {
        return ricette;
    }

    public ArrayList<Corso> getCorsi()
    {
        return corsi;
    }

    public void addRicetta(Ricetta toAddRicetta)
    {
        ricette.add(toAddRicetta);
    }

    public void addCorso(Corso toAddCorso)
    {
        corsi.add(toAddCorso);
    }

    public int getNumeroCorsi()
    {
        return corsi.size();
    }
}