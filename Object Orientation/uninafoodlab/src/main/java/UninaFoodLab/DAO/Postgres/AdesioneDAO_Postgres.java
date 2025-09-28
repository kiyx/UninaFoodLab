package UninaFoodLab.DAO.Postgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import UninaFoodLab.DAO.AdesioneDAO;
import UninaFoodLab.DTO.Adesione;
import UninaFoodLab.Exceptions.DAOException;
import UninaFoodLab.Exceptions.IscrizioneNotFoundException;

public class AdesioneDAO_Postgres implements AdesioneDAO
{
	@Override
	public void save(Adesione toSaveAdesione)
	{
		String sql = 
				 	  "INSERT INTO Adesioni(IdPartecipante, IdSessionePratica, DataAdesione) " +
				 	  "VALUES(?, ?, ?)";

	  try(Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
	  {
	      s.setInt(1, toSaveAdesione.getPartecipante().getId());
	      s.setInt(2, toSaveAdesione.getSessione().getId());
	      s.setDate(3, toSaveAdesione.getDataAdesione());
	      s.executeUpdate();
	  }
	  catch(SQLException e)
	  {
		  throw new DAOException("Errore DB durante salvataggio Adesione", e);
	  }
	}
	
	@Override
	public List<Integer> getAdesioniByIdSessionePratica(int idSessionePratica)
	{
		String sql = 
					 "SELECT * "
				   + "FROM Adesioni "
				   + "WHERE IdSessionePratica = ?";
		
		List<Integer> adesioni = new ArrayList<>();
        
        try(Connection conn = ConnectionManager.getConnection(); Statement s = conn.createStatement())
        {
            ResultSet rs = s.executeQuery(sql);

            while(rs.next())
            	 adesioni.add(rs.getInt("IdPartecipante"));
        }
        catch(SQLException e)
        {
        	throw new DAOException("Errore DB durante getAdesioniByIdSessionePratica", e);
        }
        
        return adesioni;
	}
	
	@Override
	public boolean checkAdesione(int idSessionePratica, int idPartecipante)
	{
		String sql = "SELECT EXISTS (SELECT 1 FROM Adesioni WHERE IdSessionePratica = ? AND IdPartecipante = ?)";

		try(Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
		{
		    s.setInt(1, idSessionePratica);
		    s.setInt(2, idPartecipante);
		    ResultSet rs = s.executeQuery();
		
		    if(rs.next())
		        return rs.getBoolean(1);
		    else
		    	throw new IscrizioneNotFoundException("Adesione con idCorso: " + idSessionePratica + "e idPartecipante: " + idPartecipante + " non trovato");
		}
		catch(SQLException e)
		{
			throw new DAOException("Errore DB durante checkIscrizione", e);
		}
	}

	@Override
	public void delete(int idPartecipante, int idSessionePratica)
	{
		String sql = 
		   			 "DELETE "
		   		   + "FROM Adesioni "
		   		   + "WHERE IdPartecipante = ? AND IdSessionePratica = ?";

	   try(Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
	   {
	       s.setInt(1, idPartecipante);
	       s.setInt(2, idSessionePratica);
	       s.executeUpdate();
	   }
	   catch(SQLException e)
	   {
	   		throw new DAOException("Errore DB durante delete di Adesione", e);
	   }
	}
}