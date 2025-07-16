package UninaFoodLab.DAO;

import UninaFoodLab.DTO.Argomento;

import java.sql.Connection;
import java.util.List;

public interface ArgomentoDAO
{
	void saveArgomentiCorso(int idCorso, List<Argomento> argomenti, Connection conn);
    List<Argomento> getAllArgomenti();
    List<Argomento> getArgomentiByIdCorso(int idCorso);
    void deleteArgomentoFromCorso(int idCorso, int idArgomento);
} 