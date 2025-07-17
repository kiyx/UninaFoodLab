package UninaFoodLab.DAO;

import UninaFoodLab.DTO.Utilizzo;

import java.sql.Connection;
import java.util.List;

public interface UtilizzoDAO
{
	void save(Utilizzo toSaveUtilizzo, Connection conn);
    List<Utilizzo> getUtilizziByIdRicetta(int idRicetta);
    void update(Utilizzo previousUtilizzo, Utilizzo updatedUtilizzo);
    void delete(int idRicetta, int idIngrediente);
}