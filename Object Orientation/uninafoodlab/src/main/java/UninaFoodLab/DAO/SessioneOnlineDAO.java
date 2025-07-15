package UninaFoodLab.DAO;

import UninaFoodLab.DTO.SessioneOnline;

import java.util.List;

public interface SessioneOnlineDAO
{
		void save(SessioneOnline toSaveSessione);
        SessioneOnline getSessioneOnlineById(int id);
        List<SessioneOnline> getSessioniOnlineByIdCorso(int idCorso);
        void update(SessioneOnline oldSessione, SessioneOnline newSessione);
        void delete(int IdSessioneOnline);
}