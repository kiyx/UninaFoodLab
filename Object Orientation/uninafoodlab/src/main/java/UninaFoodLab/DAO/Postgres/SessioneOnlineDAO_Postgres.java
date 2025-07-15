package UninaFoodLab.DAO.Postgres;

import UninaFoodLab.DAO.SessioneOnlineDAO;
import UninaFoodLab.DTO.Corso;
import UninaFoodLab.DTO.SessioneOnline;
import UninaFoodLab.Exceptions.DAOException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SessioneOnlineDAO_Postgres implements SessioneOnlineDAO
{
	
	
	
	@Override
    public void save(SessioneOnline toSaveSessione)
    {
        String sql = "INSERT INTO SessioneOnline (Durata, Orario, Data, LinkRiunione, IdCorso) "
        		   + "VALUES (?, ?, ?, ?, ?)";
        
        
        try(Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
        {
            s.setInt(1, toSaveSessione.getDurata());
            s.setTime(2, toSaveSessione.getOrario());
            s.setDate(3, toSaveSessione.getData());
            s.setString(4, toSaveSessione.getLinkRiunione());
            s.setInt(5, toSaveSessione.getCorso().getId());
            s.executeUpdate();
        }
        catch(SQLException e)
        {
        	throw new DAOException("Errore DB durante salvataggio SessioneOnline", e);
        }
    }

	
	@Override
    public SessioneOnline getSessioneOnlineById(int id)
	{
        String sql ="SELECT * FROM SessioneOnline WHERE IdCorso = ?";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
        {
            s.setInt(1, id);
            ResultSet rs = s.executeQuery();

            while(rs.next())
            {
                return new SessioneOnline(
                        rs.getInt("Durata"),
                        rs.getTime("Orario"),
                        rs.getDate("Data").toLocalDate(),
                        rs.getString("LinkRiunione")
                );
            }
        }
        catch(SQLException e)
        {
        	throw new DAOException("Errore DB durante getSessioneOnlineById", e);
        }
        
        return null;
    }

	@Override
    public List<SessioneOnline> getSessioniOnlineByIdCorso(int idCorso)
    {
        String sql ="SELECT * FROM SessioneOnline WHERE IdCorso = ?";
        List<SessioneOnline> ret = new ArrayList<SessioneOnline>();
        
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
        {
            s.setInt(1, idCorso);
            ResultSet rs = s.executeQuery();

            while(rs.next())
            {
                ret.add(new SessioneOnline(
                        rs.getInt("Durata"),
                        rs.getTime("Orario"),
                        rs.getDate("Data").toLocalDate(),
                        rs.getString("LinkRiunione")
                )) ;
            }
        }
        catch(SQLException e)
        {
        	throw new DAOException("Errore DB durante getSessioniOnlineByCorso", e);
        }
        
        return ret;
    }
	
	@Override
    public void update(SessioneOnline oldSessione, SessioneOnline newSessione)
    {
        if(!oldSessione.getLinkRiunione().equals(newSessione.getLinkRiunione()))
        {
            String sql = "UPDATE SessioneOnline SET LinkRiunione = ? WHERE IdSessioneOnline = ?";
            try (Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
            {
                s.setString(1, newSessione.getLinkRiunione());
                s.setInt(2, newSessione.getId());
                s.executeUpdate();
            }
            catch(SQLException e)
            {
            	throw new DAOException("Errore DB durante update SessioneOnline", e);
            }
        }
    }

	@Override
    public void delete(int IdSessioneOnline)
    {
        String sql = "DELETE FROM Corso WHERE IdSessioneOnline = ?";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
        {
            s.setInt(1, IdSessioneOnline);
            s.executeUpdate();
        }
        catch(SQLException e)
        {
        	throw new DAOException("Errore DB durante eliminazione SessioneOnline", e);
        }

    }
	
}
