package UninaFoodLab.DTO;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;

import UninaFoodLab.Exceptions.RequiredCorsoException;

public abstract class Sessione
{
    private int id;
    private int durata;
    private Time orario;
    private LocalDate data;
    private Corso corso;
    
    protected Sessione(int durata, Time orario, LocalDate data)
    {
        this.durata = durata;
        this.orario = orario;
        this.data = data;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public int getDurata() 
    {
        return durata;
    }

    public void setDurata(int durata) 
    {
        this.durata = durata;
    }

    public Time getOrario() 
    {
        return orario;
    }

    public void setOrario(Time orario) 
    {
        this.orario = orario;
    }

    public Date getData() 
    {
        return Date.valueOf(data);
    }

    public void setData(LocalDate data) 
    {
        this.data = data;
    }
    
    public Corso getCorso() 
    {
        return corso;
    }
    
    public void setCorso(Corso corso)
    {
        if(corso == null)
            throw new RequiredCorsoException();
        this.corso = corso;
    }
}