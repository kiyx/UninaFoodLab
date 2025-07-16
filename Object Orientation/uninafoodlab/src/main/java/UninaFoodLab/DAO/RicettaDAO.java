package UninaFoodLab.DAO;

import UninaFoodLab.DTO.Ricetta;

import java.util.List;

public interface RicettaDAO
{
	void save(Ricetta toSaveRicetta, int IdChef);
	Ricetta getRicettaById(int idRicetta);
    List<Ricetta> getRicetteByIdChef(int idChef);
    List<Ricetta> getRicettaByIdSessionePratica(int idSessionePratica);
    void update(Ricetta previousRicetta, Ricetta updatedRicetta);
    void delete(int idRicetta);
}