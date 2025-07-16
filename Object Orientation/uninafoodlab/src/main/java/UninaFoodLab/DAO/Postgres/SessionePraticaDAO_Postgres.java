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
		    				  rs.getString("Luogo"),
		    				  new ArrayList<Ricetta>(new RicettaDAO_Postgres().getRicettaByIdSessionePratica(rs.getInt("IdSessionePratica")))
		    			   );
	    sp.setId(rs.getInt("IdSessionePratica"));
	    
	    return sp;
	}
	
	private void savePreparazioni(SessionePratica sp, Connection conn) throws SQLException
	{
	    String sql = 
	    			"INSERT INTO Preparazioni (IdSessionePratica, IdRicetta) "
	    		  + "VALUES (?, ?)";

	    try(PreparedStatement ps = conn.prepareStatement(sql))
	    {
	        for(Ricetta r : sp.getRicette())
	        {
	            ps.setInt(1, sp.getId());
	            ps.setInt(2, r.getId());
	            ps.addBatch();
	        }
	        ps.executeBatch();
	    }
	}
	
	@Override
	public void save(SessionePratica sp)
	{
	    try(Connection conn = ConnectionManager.getConnection())
	    {
	        conn.setAutoCommit(false);
	        save(sp, conn);
	        conn.commit();
	    }
	    catch (SQLException e)
	    {
	        throw new DAOException("Errore durante salvataggio completo e transazionale SessionePratica", e);
	    }
	}
	
	@Override
	public void save(SessionePratica sp, Connection conn)
	{
	    String sql =
	                 "INSERT INTO SessionePratica (Durata, Orario, Data, Luogo, IdCorso) " +
	                 "VALUES (?, ?, ?, ?, ?)";

	    try(PreparedStatement s = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
	    {
	        s.setInt(1, sp.getDurata());
	        s.setTime(2, sp.getOrario());
	        s.setDate(3, sp.getData());
	        s.setString(4, sp.getIndirizzo());
	        s.setInt(5, sp.getCorso().getId());
	        s.executeUpdate();

	        try (ResultSet rs = s.getGeneratedKeys())
	        {
	            if (rs.next())
	                sp.setId(rs.getInt(1));
	            else
	                throw new DAOException("Creazione SessionePratica fallita, nessun ID ottenuto.");
	        }
	    }
	    catch(SQLException e)
	    {
	        throw new DAOException("Errore durante save SessionePratica (conn esterna)", e);
	    }

	    try
	    {
	        savePreparazioni(sp, conn);
	    }
	    catch(SQLException e)
	    {
	        throw new DAOException("Errore durante savePreparazioni", e);
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
	public void update(SessionePratica oldS, SessionePratica newS)
	{
	    String sql = "UPDATE SessionePratica SET ";
	    List<Object> parametri = new ArrayList<>();

	    if(oldS.getDurata() != newS.getDurata())
	    {
	        sql += "Durata = ?, ";
	        parametri.add(newS.getDurata());
	    }
	    
	    if(!oldS.getOrario().equals(newS.getOrario()))
	    {
	        sql += "Orario = ?, ";
	        parametri.add(newS.getOrario());
	    }
	    
	    if(!oldS.getData().equals(newS.getData()))
	    {
	        sql += "Data = ?, ";
	        parametri.add(newS.getData());
	    }
	    
	    if(!oldS.getIndirizzo().equals(newS.getIndirizzo()))
	    {
	        sql += "Luogo = ?, ";
	        parametri.add(newS.getIndirizzo());
	    }

	    if(!parametri.isEmpty())
	    {
	    	Connection conn = null;

		    try
		    {
		        conn = ConnectionManager.getConnection();
		        conn.setAutoCommit(false);

		        if(!parametri.isEmpty())
		        {
		            // Rimuove l’ultima virgola
		            if(sql.endsWith(", "))
		               sql = sql.substring(0, sql.length() - 2);

		            sql += " WHERE IdSessionePratica = ?";
		            parametri.add(newS.getId());

		            try(PreparedStatement ps = conn.prepareStatement(sql))
		            {
		                for(int i = 0; i < parametri.size(); i++)
		                    ps.setObject(i + 1, parametri.get(i));

		                ps.executeUpdate();
		            }
		        }

		        if(!oldS.getRicette().equals(newS.getRicette()))
		            aggiornaRicette(newS, conn);

		        conn.commit();
		    }
		    catch (Exception e)
		    {
		        if(conn != null)
		        {
		            try
		            {
		                conn.rollback();
		            }
		            catch (SQLException ex)
		            {
		                throw new DAOException("Errore durante rollback update SessionePratica", ex);
		            }
		        }

		        throw new DAOException("Errore durante update SessionePratica", e);
		    }
		    finally
		    {
		        if(conn != null)
		        {
		            try
		            {
		                conn.setAutoCommit(true);
		                conn.close();
		            }
		            catch(SQLException ex)
		            {
		                throw new DAOException("Errore durante chiusura connessione in update sessionepratica", ex);
		            }
		        }
		    }
	    } 
	}

	private void aggiornaRicette(SessionePratica sp, Connection conn) throws SQLException
	{
	    try(PreparedStatement delete = conn.prepareStatement("DELETE FROM Preparazioni WHERE IdSessionePratica = ?"))
	    {
	        delete.setInt(1, sp.getId());
	        delete.executeUpdate();
	    }

	    try(PreparedStatement insert = conn.prepareStatement("INSERT INTO Preparazioni (IdSessionePratica, IdRicetta) VALUES (?, ?)"))
	    {
	        for(Ricetta r : sp.getRicette())
	        {
	            insert.setInt(1, sp.getId());
	            insert.setInt(2, r.getId());
	            insert.addBatch();
	        }
	        insert.executeBatch();
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