package UninaFoodLab.DAO;

import UninaFoodLab.DTO.Corso;

import java.sql.Connection;
import java.util.List;

public interface CorsoDAO
{
	void save(Corso toSaveCorso);
	void save(Corso toSaveCorso, Connection conn);
    Corso getCorsoById(int idCorso);
    List<Corso> getCorsiByChef(int idChef);
    List<Corso> getCorsiByArgomenti(List<Integer> idsArgomenti);
    List<Corso> getAllCorsi();
    void update(Corso oldCorso, Corso newCorso);
    void delete(int IdCorso);
}