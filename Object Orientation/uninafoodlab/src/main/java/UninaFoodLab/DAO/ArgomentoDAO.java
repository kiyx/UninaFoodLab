package UninaFoodLab.DAO;

import UninaFoodLab.DTO.Argomento;

import java.util.List;

public interface ArgomentoDAO
{
	void saveArgomentiCorso(int idCorso, List<Argomento> argomenti);
    List<Argomento> getAllArgomenti();
    List<Argomento> getArgomentiByIdCorso(int idCorso);
    void deleteArgomentoFromCorso(int idCorso, int idArgomento);
} 