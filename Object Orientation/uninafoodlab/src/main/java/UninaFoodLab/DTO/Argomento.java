package UninaFoodLab.DTO;

import java.util.Objects;

public class Argomento
{
    private int id;
    private String nome;

    public Argomento(String nome)
    {
        this.nome = nome;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass())
            return false;

        Argomento argomento = (Argomento) o;
        return nome.equals(argomento.nome);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(nome);
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getNome()
    {
        return nome;
    }
}