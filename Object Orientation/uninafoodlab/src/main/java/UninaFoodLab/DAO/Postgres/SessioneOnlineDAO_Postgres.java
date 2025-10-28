package UninaFoodLab.DAO.Postgres;

import UninaFoodLab.DAO.SessioneOnlineDAO;
import UninaFoodLab.DTO.SessioneOnline;
import UninaFoodLab.Exceptions.DAOException;
import UninaFoodLab.Exceptions.SessioneNotFoundException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SessioneOnlineDAO_Postgres implements SessioneOnlineDAO
{	
	@Override
	public void save(SessioneOnline sessione) 
	{
	    try(Connection conn = ConnectionManager.getConnection()) 
	    {
	        save(sessione, conn);
	    } 
	    catch (SQLException e) 
	    {
	        throw new DAOException("Errore DB durante salvataggio SessioneOnline", e);
	    }
	}
	
	@Override
    public void save(SessioneOnline toSaveSessione, Connection conn)
    {
        String sql = "INSERT INTO SessioneOnline (Durata, Orario, Data, LinkRiunione, IdCorso) "
        		   + "VALUES (?, ?, ?, ?, ?)";
        
        
        try(PreparedStatement s = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            s.setInt(1, toSaveSessione.getDurata());
            s.setTime(2, toSaveSessione.getOrario());
            s.setDate(3, toSaveSessione.getData());
            s.setString(4, toSaveSessione.getLinkRiunione());
            s.setInt(5, toSaveSessione.getCorso().getId());
            s.executeUpdate();
            
            try(ResultSet genKeys = s.getGeneratedKeys())
            {
            	if(genKeys.next())
            		toSaveSessione.setId(genKeys.getInt(1));
            	else
            		throw new DAOException("Creazione SessioneOnline fallita, nessun ID ottenuto.");
            }      
        }
        catch(SQLException e)
        {
        	throw new DAOException("Errore DB durante salvataggio SessioneOnline", e);
        }
    }
	
	@Override
    public SessioneOnline getSessioneOnlineById(int idSessioneOnline)
	{
        String sql =
        			 "SELECT * "
        		   + "FROM SessioneOnline "
        		   + "WHERE IdSessioneOnline = ?";
        
        try(Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
        {
            s.setInt(1, idSessioneOnline);
            ResultSet rs = s.executeQuery();

            if(rs.next())
                return mapResultSetToSessioneOnline(rs);
            else
            	throw new SessioneNotFoundException("Non è stata trovata la sessione online con id " + idSessioneOnline);
        }
        catch(SQLException e)
        {
        	throw new DAOException("Errore DB durante getSessioneOnlineById", e);
        }
    }

	@Override
    public List<SessioneOnline> getSessioniOnlineByIdCorso(int idCorso)
    {
        String sql =
        			 "SELECT * "
        		   + "FROM SessioneOnline "
        		   + "WHERE IdCorso = ?";
        
        List<SessioneOnline> sessions = new ArrayList<>();
        
        try(Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
        {
            s.setInt(1, idCorso);
            ResultSet rs = s.executeQuery();

            while(rs.next())
            	sessions.add(mapResultSetToSessioneOnline(rs));
        }
        catch(SQLException e)
        {
        	throw new DAOException("Errore DB durante getSessioniOnlineByCorso", e);
        }
        
        return sessions;
    }
	
	@Override
	public void update(SessioneOnline oldSessione, SessioneOnline newSessione)
	{
		String sql = "UPDATE SessioneOnline SET ";
		List<Object> param = new ArrayList<>();

		if(oldSessione.getDurata() != newSessione.getDurata())
		{
			sql += "Durata = ?, ";
			param.add(newSessione.getDurata());
		}
		
		if(!oldSessione.getOrario().equals(newSessione.getOrario()))
		{
			sql += "Orario = ?, ";
			param.add(newSessione.getOrario());
		}
		
		if(!oldSessione.getData().equals(newSessione.getData()))
		{
			sql += "Data = ?, ";
			param.add(newSessione.getData());
		}
		
		if(!oldSessione.getLinkRiunione().equals(newSessione.getLinkRiunione()))
		{
			sql += "LinkRiunione = ?, ";
			param.add(newSessione.getLinkRiunione());
		}

		if(!param.isEmpty())
		{
			sql = sql.substring(0, sql.length() - 2);
			sql += " WHERE IdSessioneOnline = ?";
			param.add(newSessione.getId());

			try (Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
			{
				for (int i = 0; i < param.size(); i++)
					s.setObject(i + 1, param.get(i));

				s.executeUpdate();
			}
			catch (SQLException e)
			{
				throw new DAOException("Errore DB durante update SessioneOnline", e);
			}
		}
	}

    @Override
    public void delete(int idSessioneOnline)
    {
        String sql =
            "DELETE FROM SessioneOnline " +
            "WHERE IdSessioneOnline = ? " +
            "  AND Data > CURRENT_DATE";

        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
        {
            s.setInt(1, idSessioneOnline);
            s.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new DAOException("Errore DB durante eliminazione SessioneOnline", e);
        }
    }
	
	private SessioneOnline mapResultSetToSessioneOnline(ResultSet rs) throws SQLException 
	{    
	    SessioneOnline so = new SessioneOnline
		    				(
		    				  rs.getInt("Durata"), 
		    				  rs.getTime("Orario"),
		    				  rs.getDate("Data").toLocalDate(), 
		    				  rs.getString("LinkRiunione")
		    			   );
	    so.setId(rs.getInt("IdSessioneOnline"));
	    
	    return so;
	}
}