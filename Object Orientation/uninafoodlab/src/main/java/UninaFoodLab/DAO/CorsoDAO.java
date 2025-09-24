package UninaFoodLab.DAO;

import UninaFoodLab.DTO.Corso;

import java.sql.Connection;
import java.util.List;

public interface CorsoDAO
{
	void save(Corso toSaveCorso);
	void save(Corso toSaveCorso, Connection conn);
	void saveIscrizione(int idCorso, int idPartecipante);
    Corso getCorsoById(int idCorso);
    List<Corso> getCorsiByIdChef(int idChef);
    List<Corso> getCorsiByIdPartecipante(int idPartecipante);
    List<Corso> getAllCorsi();
    Integer getNumeroIscrittiById(int idCorso);
    void update(Corso oldCorso, Corso newCorso);
    void delete(int idCorso);
}