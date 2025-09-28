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
    List<Integer> getCorsiByIdChef(int idChef);
    List<Integer> getIdCorsiIscrittiByIdPartecipante(int idPartecipante);
    List<Corso> getAllCorsi();
    Integer getNumeroIscrittiById(int idCorso);
    boolean checkIscrizione(int idCorso, int idPartecipante);
    void update(Corso oldCorso, Corso newCorso);
    void deleteIscrizione(int idCorso, int idPartecipante);
    void delete(int idCorso);
}