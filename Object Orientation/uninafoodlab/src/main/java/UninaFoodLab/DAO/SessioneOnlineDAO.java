package UninaFoodLab.DAO;

import UninaFoodLab.DTO.SessioneOnline;

import java.sql.Connection;
import java.util.List;

public interface SessioneOnlineDAO
{
	void save(SessioneOnline toSaveSessione);
	void save(SessioneOnline toSaveSessione, Connection conn);
    SessioneOnline getSessioneOnlineById(int idSessioneOnline);
    List<SessioneOnline> getSessioniOnlineByIdCorso(int idCorso);
    void update(SessioneOnline oldSessione, SessioneOnline newSessione);
    void delete(int IdSessioneOnline);
}