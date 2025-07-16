package UninaFoodLab.DAO.Postgres;

import UninaFoodLab.DTO.Argomento;
import UninaFoodLab.Exceptions.DAOException;
import UninaFoodLab.DAO.ArgomentoDAO;

import java.util.List;
import java.util.ArrayList;
import java.sql.*;

public class ArgomentoDAO_Postgres implements ArgomentoDAO
{
	private Argomento mapResultSetToArgomento(ResultSet rs) throws SQLException
	{
		Argomento a = new Argomento(rs.getString("Nome"));
		a.setId(rs.getInt("IdArgomento"));
		
		return a;
	}
	
	@Override
	public void saveArgomentiCorso(int idCorso, List<Argomento> argomenti, Connection conn)
	{
	    String sql = "INSERT INTO Argomenti_Corso (IdCorso, IdArgomento) "
	    		   + "VALUES (?, ?)";

	    try(PreparedStatement s = conn.prepareStatement(sql))
	    {
	        for(Argomento a : argomenti)
	        {
	            s.setInt(1, idCorso);
	            s.setInt(2, a.getId());
	            s.addBatch();
	        }

	        s.executeBatch();
	    }
	    catch (SQLException e)
	    {
	        throw new DAOException("Errore DB durante l'inserimento degli argomenti del corso", e);
	    }
	}

	private int countArgomentiByIdCorso(int idCorso)
	{
	    String sql = 
	    			 "SELECT COUNT(*) AS Totale "
	    		   + "FROM Argomenti_Corso "
	    		   + "WHERE IdCorso = ?";
	    
	    try (Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
	    {
	        s.setInt(1, idCorso);
	        ResultSet rs = s.executeQuery();
	        
	        if(rs.next())
	            return rs.getInt("Totale");
	        else
	            return 0;
	    }
	    catch (SQLException e)
	    {
	        throw new DAOException("Errore DB durante countArgomentiByIdCorso", e);
	    }
	}

	@Override
    public List<Argomento> getAllArgomenti()
    {
        List<Argomento> argomenti = new ArrayList<>();
        
        String sql = 
        			"SELECT * "
        	      + "FROM Argomento";

        try(Connection conn = ConnectionManager.getConnection(); Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql))
        {
            while(rs.next())
                argomenti.add(mapResultSetToArgomento(rs));
        }
        catch(SQLException e)
        {
        	throw new DAOException("Errore DB durante getAllArgomenti", e);
        }
        
        return argomenti;
    }
	
	@Override
	public List<Argomento> getArgomentiByIdCorso(int idCorso)
	{
		List<Argomento> argomenti = new ArrayList<>();
		
        String sql = 
        			"SELECT * "
        		  + "FROM Argomento A JOIN Argomenti_Corso AC ON A.IdArgomento = AC.IdArgomento "
        		  + "WHERE AC.IdCorso = ?";

        try(Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
        {
        	s.setInt(1, idCorso);
        	ResultSet rs = s.executeQuery();
        	
            while(rs.next())
                argomenti.add(mapResultSetToArgomento(rs));
        }
        catch(SQLException e)
        {
        	throw new DAOException("Errore DB durante getArgomentiByCorso", e);
        }
        
        return argomenti;
	}
	
	@Override
	public void deleteArgomentoFromCorso(int idCorso, int idArgomento)
	{
	    if(countArgomentiByIdCorso(idCorso) <= 1)
	        throw new DAOException("Non puoi rimuovere l'ultimo argomento del corso.");

	    String sql = 
	    			"DELETE FROM Argomenti_Corso "
	    		  + "WHERE IdCorso = ? AND IdArgomento = ?";
	    
	    try(Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
	    {
	        s.setInt(1, idCorso);
	        s.setInt(2, idArgomento);

	        if(s.executeUpdate() == 0)
	            throw new DAOException("Argomento non trovato per il corso specificato.");
	    }
	    catch (SQLException e)
	    {
	        throw new DAOException("Errore DB durante deleteArgomentoFromCorso", e);
	    }
	}
}