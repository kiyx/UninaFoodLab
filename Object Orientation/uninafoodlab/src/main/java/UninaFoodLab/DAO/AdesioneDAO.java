package UninaFoodLab.DAO;

import java.util.List;

import UninaFoodLab.DTO.Adesione;

public interface AdesioneDAO
{
	void save(Adesione toSaveAdesione);
	List<Integer> getAdesioniByIdSessionePratica(int idSessionePratica);
	void delete(int idPartecipante, int idSessionePratica);
}