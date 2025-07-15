package UninaFoodLab.DAO;

import java.util.List;

import UninaFoodLab.DTO.SessionePratica;

public interface SessionePraticaDAO
{
	void save(SessionePratica toSaveSessione);
	SessionePratica getSessionePraticaById(int id);
    List<SessionePratica> getSessioniPraticaByIdCorso(int idCorso);
    void update(SessionePratica oldSessione, SessionePratica newSessione);
    void delete(int IdSessioneOnline);
}