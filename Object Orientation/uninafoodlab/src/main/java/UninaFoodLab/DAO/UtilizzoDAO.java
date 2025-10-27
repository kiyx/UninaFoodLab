package UninaFoodLab.DAO;

import UninaFoodLab.DTO.Utilizzo;

import java.sql.Connection;
import java.util.List;

public interface UtilizzoDAO
{
	void save(Utilizzo toSaveUtilizzo, Connection conn);
    List<Utilizzo> getUtilizziByIdRicetta(int idRicetta);
    void update(Utilizzo updatedUtilizzo, Connection conn);
    void delete(int idRicetta, int idIngrediente, Connection conn);
}