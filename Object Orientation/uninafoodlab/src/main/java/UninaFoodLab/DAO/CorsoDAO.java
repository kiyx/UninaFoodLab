package UninaFoodLab.DAO;

import UninaFoodLab.DTO.Corso;

import java.util.List;

public interface CorsoDAO
{
	void save(Corso toSaveCorso);
    Corso getCorsoById(int idCorso);
    List<Corso> getCorsiByChef(int idChef);
    List<Corso> getCorsiByArgomenti(List<Integer> idsArgomenti);
    List<Corso> getAllCorsi();
    void update(Corso oldCorso, Corso newCorso);
    void delete(int IdCorso);
}