package UninaFoodLab.DTO;

import java.util.Objects;

import UninaFoodLab.Exceptions.RequiredIngredienteException;

public class Utilizzo
{
	private int idRicetta;
	private int idIngrediente;
    private double quantita;
    private UnitaDiMisura udm;
    private Ingrediente ingrediente;

    public Utilizzo(Double quantita, UnitaDiMisura udm, Ingrediente ingrediente)
    {
    	if(ingrediente == null) throw new RequiredIngredienteException();
    		
        this.quantita = quantita;
        this.udm = udm;
        this.ingrediente = ingrediente;
        this.idIngrediente = ingrediente.getId();
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass())
            return false;

        Utilizzo utilizzo = (Utilizzo) o;
        return quantita == utilizzo.quantita && udm == utilizzo.udm && ingrediente == utilizzo.ingrediente;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(quantita, udm, ingrediente);
    }

    public int getIdRicetta()
    {
        return idRicetta;
    }

    public void setIdRicetta(int id)
    {
        this.idRicetta = id;
    }
    
    public int getIdIngrediente()
    {
        return idIngrediente;
    }

    public void setIdIngrediente(int id)
    {
        this.idIngrediente = id;
    }
    
    public double getQuantita()
    {
        return quantita;
    }

    public void setQuantita(double quantita)
    {
        this.quantita = quantita;
    }

    public UnitaDiMisura getUdm()
    {
        return udm;
    }

    public void setUdm(UnitaDiMisura udm)
    {
        this.udm = udm;
    }

    public Ingrediente getIngrediente()
    {
        return ingrediente;
    }

}