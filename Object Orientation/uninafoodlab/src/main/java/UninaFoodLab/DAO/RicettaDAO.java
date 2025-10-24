package UninaFoodLab.DAO;

import UninaFoodLab.DTO.Ricetta;
import UninaFoodLab.DTO.Utilizzo;

import java.util.ArrayList;
import java.util.List;

public interface RicettaDAO
{
	void save(Ricetta toSaveRicetta, int IdChef);
	Ricetta getRicettaById(int idRicetta);
	Ricetta getRicettaByNome(Ricetta toSaveRicetta, int idChef);
    List<Ricetta> getRicetteByIdChef(int idChef);
    boolean existsRicettaByNome(Ricetta toSaveRicetta, int idChef);
    List<Ricetta> getRicettaByIdSessionePratica(int idSessionePratica);
    void update(Ricetta previousRicetta, Ricetta updatedRicetta, ArrayList<Utilizzo> toAddUtilizzi, ArrayList<Utilizzo> toUpdateUtilizzi, ArrayList<Utilizzo> toDeleteUtilizzi);
    void delete(int idRicetta);
}