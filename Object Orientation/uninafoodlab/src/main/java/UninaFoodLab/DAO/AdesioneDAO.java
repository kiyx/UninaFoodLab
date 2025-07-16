package UninaFoodLab.DAO;

import java.util.List;

import UninaFoodLab.DTO.Adesione;

public interface AdesioneDAO
{
	void save(Adesione toSaveAdesione);
	List<Adesione> getAdesioniByIdSessionePratica(int idSessionePratica);
	void delete(int idPartecipante, int idSessionePratica);
}