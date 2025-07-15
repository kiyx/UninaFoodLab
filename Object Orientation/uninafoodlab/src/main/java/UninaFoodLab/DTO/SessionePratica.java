package UninaFoodLab.DTO;

import UninaFoodLab.Exceptions.RequiredRicettaException;

import java.sql.Time;
import java.util.ArrayList;
import java.time.LocalDate;
import java.util.Objects;

public class SessionePratica extends Sessione
{
    private int numeroPartecipanti = 0;
    private String indirizzo;
    ArrayList<Partecipante> partecipanti = new ArrayList<>();
    ArrayList<Ricetta> ricette;
    ArrayList<Adesione> adesioni = new ArrayList<>();

    public SessionePratica(int durata, Time orario, LocalDate data, String indirizzo, ArrayList<Ricetta> ricette)
    {
        super(durata, orario, data);
        
        if(ricette == null) throw new RequiredRicettaException();
        this.ricette = ricette;
        
        this.indirizzo = indirizzo; 
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass())
            return false;

        SessionePratica s = (SessionePratica) o;
        return getDurata() == s.getDurata() && getOrario().equals(s.getOrario()) && getData().equals(s.getData()) && numeroPartecipanti == s.numeroPartecipanti && indirizzo.equals(s.indirizzo) && partecipanti.equals(s.partecipanti) && ricette.equals(s.ricette);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getDurata(), getOrario(), getData(), numeroPartecipanti, indirizzo, partecipanti, ricette);
    }

    public int getNumeroPartecipanti()
    {
        return numeroPartecipanti;
    }

    public String getIndirizzo()
    {
        return indirizzo;
    }

    public void setIndirizzo(String indirizzo)
    {
        this.indirizzo = indirizzo;
    }

    public ArrayList<Partecipante> getPartecipanti()
    {
        return partecipanti;
    }

    public void addPartecipante (Partecipante toAddPartecipante)
    {
        partecipanti.add(toAddPartecipante);
        numeroPartecipanti++;
    }

    public ArrayList<Ricetta> getRicette()
    {
        return ricette;
    }

    public void addRicetta(Ricetta toAddRicetta)
    {
        ricette.add(toAddRicetta);
    }

    public ArrayList<Adesione> getAdesioni()
    {
        return adesioni;
    }

    public void addAdesione (Adesione toAddAdesione)
    {
        adesioni.add(toAddAdesione);
    }
}