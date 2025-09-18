package UninaFoodLab.DAO.Postgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import UninaFoodLab.DAO.AdesioneDAO;
import UninaFoodLab.DTO.Adesione;
import UninaFoodLab.DTO.Partecipante;
import UninaFoodLab.DTO.SessionePratica;
import UninaFoodLab.Exceptions.DAOException;

public class AdesioneDAO_Postgres implements AdesioneDAO
{
	private Adesione mapResultSetToAdesione(ResultSet rs) throws SQLException
	{
		Partecipante p = new Partecipante();
		p.setId(rs.getInt("IdPartecipante"));
		
		SessionePratica s = new SessionePratica();
		s.setId(rs.getInt("IdSessionePratica"));
		
		Adesione a = new Adesione(
									rs.getDate("DataAdesione").toLocalDate(),
									p,
									s
								 );
		
		a.setIdPartecipante(rs.getInt("IdPartecipante"));		
		a.setIdSessionePratica(rs.getInt("IdSessionePratica"));
		
		return a;
	}
	
	@Override
	public void save(Adesione toSaveAdesione)
	{
		String sql = 
				 	  "INSERT INTO Adesione(IdPartecipante, IdSessionePratica, DataAdesione) " +
				 	  "VALUES(?, ?, ?)";

	  try(Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
	  {
	      s.setInt(1, toSaveAdesione.getIdPartecipante());
	      s.setInt(2, toSaveAdesione.getIdSessionePratica());
	      s.setDate(3, toSaveAdesione.getDataAdesione());
	      s.executeUpdate();
	  }
	  catch(SQLException e)
	  {
		  throw new DAOException("Errore DB durante salvataggio Adesione", e);
	  }
	}
	
	@Override
	public List<Adesione> getAdesioniByIdSessionePratica(int idSessionePratica)
	{
		String sql = 
					 "SELECT *"
					 + "FROM Adesioni A JOIN";
	}
	
	@Override
	public void delete(int idPartecipante, int idSessionePratica)
	{
		String sql = 
		   			 "DELETE "
		   		   + "FROM Utilizzi "
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