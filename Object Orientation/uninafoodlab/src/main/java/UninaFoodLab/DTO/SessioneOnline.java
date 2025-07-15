package UninaFoodLab.DTO;

import java.sql.Time;
import java.time.LocalDate;
import java.util.Objects;

public class SessioneOnline extends Sessione
{
    private String linkRiunione;

    public SessioneOnline(int durata, Time orario, LocalDate data, String linkRiunione)
    {
        super(durata, orario, data);
        this.linkRiunione = linkRiunione;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass())
            return false;

        SessioneOnline s = (SessioneOnline) o;
        return getDurata() == s.getDurata() && getOrario().equals(s.getOrario()) && getData().equals(s.getData()) && linkRiunione.equals(s.linkRiunione);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getDurata(), getOrario(), getData(), linkRiunione);
    }

    public String getLinkRiunione()
    {
        return linkRiunione;
    }

    public void setLinkRiunione(String linkRiunione)
    {
        this.linkRiunione = linkRiunione;
    }
}