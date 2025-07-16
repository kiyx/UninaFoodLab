package UninaFoodLab.DAO.Postgres;

import UninaFoodLab.DAO.SessionePraticaDAO;
import UninaFoodLab.DTO.Ricetta;
import UninaFoodLab.DTO.SessionePratica;
import UninaFoodLab.Exceptions.DAOException;
import UninaFoodLab.Exceptions.SessioneNotFoundException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SessionePraticaDAO_Postgres implements SessionePraticaDAO
{
	private SessionePratica mapResultSetToSessionePratica(ResultSet rs) throws SQLException 
	{    
		SessionePratica sp = new SessionePratica
		    				(
		    				  rs.getInt("Durata"), 
		    				  rs.getTime("Orario"),
		    				  rs.getDate("Data").toLocalDate(), 
		    				  rs.getString("Indirizzo"),
		    				  new ArrayList<Ricetta>(new RicettaDAO_Postgres().getRicettaByIdSessionePratica(rs.getInt("IdSessionePratica")))
		    			   );
	    sp.setId(rs.getInt("IdSessionePratica"));
	    
	    return sp;
	}
	
	@Override
    public void save(SessionePratica toSaveSessione)
    {
        String sql = 
        			 "INSERT INTO SessionePratica(Durata, Orario, Data, Luogo, IdCorso)  "
        		   + "VALUES (?, ?, ?, ?, ?)";
        try(Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
        {
            s.setInt(1, toSaveSessione.getDurata());
            s.setTime(2, toSaveSessione.getOrario());
            s.setDate(3, toSaveSessione.getData());
            s.setString(4, toSaveSessione.getIndirizzo());
            s.setInt(5, toSaveSessione.getCorso().getId()); 
            s.executeUpdate();
            
            try(ResultSet genKeys = s.getGeneratedKeys())
            {
            	if(genKeys.next())
            		toSaveSessione.setId(genKeys.getInt(1));
            	else
            		throw new DAOException("Creazione SessionePratica fallita, nessun ID ottenuto.");
            } 
        }
        catch(SQLException e)
        {
        	throw new DAOException("Errore DB durante salvataggio SessionePratica", e);
        }
    }
        
	@Override
    public SessionePratica getSessionePraticaById(int idSessionePratica)
    {
        String sql =
        			 "SELECT * "
        		   + "FROM SessionePratica "
        		   + "WHERE IdSessionePratica = ?";
        
        try(Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
        {
            s.setInt(1, idSessionePratica);
            ResultSet rs = s.executeQuery();

            if(rs.next())
            	return mapResultSetToSessionePratica(rs);
            else
            	throw new SessioneNotFoundException("Sessione pratica con id " + idSessionePratica + "non trovata");
        }
        catch(SQLException e)
        {
        	throw new DAOException("Errore DB durante salvataggio getSessionePraticaById", e);
        }
    }
    
	@Override
    public List<SessionePratica> getSessioniPraticheByIdCorso(int idCorso)
    {
        String sql =
        			 "SELECT * "
        		   + "FROM SessionePratica "
        		   + "WHERE IdCorso = ?";
        
        List<SessionePratica> sessions = new ArrayList<>();
        
        try(Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
        {
            s.setInt(1, idCorso);
            ResultSet rs = s.executeQuery();

            while(rs.next())
            	sessions.add(mapResultSetToSessionePratica(rs));   
        }
        catch(SQLException e)
        {
        	throw new DAOException("Errore DB durante salvataggio getSessioniPraticheByIdCorso", e);
        }
        
        return sessions;
    }

	@Override
	public void update(SessionePratica oldSessione, SessionePratica newSessione)
	{
		String sql = "UPDATE SessionePratica SET ";
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
		
		if(!oldSessione.getIndirizzo().equals(newSessione.getIndirizzo()))
		{
			sql += "Luogo = ?, ";
			param.add(newSessione.getIndirizzo());
		}

		if(!param.isEmpty())
		{
			sql = sql.substring(0, sql.length() - 2);
			sql += " WHERE IdSessionePratica = ?";
			param.add(newSessione.getId());

			try(Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
			{
				for (int i = 0; i < param.size(); i++)
					s.setObject(i + 1, param.get(i));

				s.executeUpdate();
			}
			catch (SQLException e)
			{
				throw new DAOException("Errore DB durante update SessionePratica", e);
			}
		}
	}
    
	@Override
    public void delete(int IdSessionePratica)
    {
        String sql = 
        			 "DELETE "
        		   + "FROM SessionePratica "
        		   + "WHERE IdSessionePratica = ?";
        
        try(Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
        {
            s.setInt(1, IdSessionePratica);
            s.executeUpdate();
        }
        catch(SQLException e)
        {
        	throw new DAOException("Errore DB durante delete SessionePratica", e);
        }
    }
}