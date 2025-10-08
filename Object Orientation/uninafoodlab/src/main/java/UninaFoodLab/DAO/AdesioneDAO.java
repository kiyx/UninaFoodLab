package UninaFoodLab.DAO;

import java.util.List;

import UninaFoodLab.DTO.Adesione;

public interface AdesioneDAO
{
	void save(Adesione toSaveAdesione);
	List<Integer> getAdesioniByIdSessionePratica(int idSessionePratica);
	Integer getNumeroAdesioniByIdSessionePratica(int idSessionePratica);
	boolean checkAdesione(int idSessionePratica, int idPartecipante);
	void delete(int idPartecipante, int idSessionePratica);
}