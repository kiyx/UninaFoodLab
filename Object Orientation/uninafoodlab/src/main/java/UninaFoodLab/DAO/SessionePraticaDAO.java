package UninaFoodLab.DAO;

import java.sql.Connection;
import java.util.List;

import UninaFoodLab.DTO.SessionePratica;

public interface SessionePraticaDAO
{
	void save(SessionePratica toSaveSessione);
	void save(SessionePratica toSaveSessione, Connection conn);
	SessionePratica getSessionePraticaById(int idSessionePratica);
    List<SessionePratica> getSessioniPraticheByIdCorso(int idCorso);
    void update(SessionePratica oldSessione, SessionePratica newSessione);
    void update(SessionePratica oldSessione, SessionePratica newSessione, Connection conn);
    void delete(int IdSessioneOnline);
}