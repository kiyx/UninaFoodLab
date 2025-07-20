package UninaFoodLab.DAO.Postgres;

import UninaFoodLab.DAO.IngredienteDAO;
import UninaFoodLab.DTO.Ingrediente;
import UninaFoodLab.DTO.NaturaIngrediente;
import UninaFoodLab.Exceptions.DAOException;
import UninaFoodLab.Exceptions.IngredienteNotFoundException;

import java.sql.*;
import java.util.*;

public class IngredienteDAO_Postgres implements IngredienteDAO
{
	@Override
	public void save(Ingrediente toSaveIngrediente)
    {
        String sql = 
        		     "INSERT INTO Ingrediente(Nome, Origine) " +
                     "VALUES(?, ?::naturaingrediente)";

        try(Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            s.setString(1, toSaveIngrediente.getNome());
            s.setString(2, toSaveIngrediente.getOrigine().toString());
            s.executeUpdate();
            
            try(ResultSet genKeys = s.getGeneratedKeys())
            {
            	if(genKeys.next())
            		toSaveIngrediente.setId(genKeys.getInt(1));
            	else
            		throw new DAOException("Creazione Ingrediente fallita, nessun ID ottenuto.");
            }    
        }
        catch(SQLException e)
        {
        	throw new DAOException("Errore DB durante salvataggio Ingrediente", e);
        }
    }
	
	@Override
    public List<Ingrediente> getAllIngredienti()
    {
        List<Ingrediente> ingredienti = new ArrayList<>();
        String sql = "SELECT * FROM Ingrediente ORDER BY Nome";

        try(Connection conn = ConnectionManager.getConnection(); Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql))
        {
            while(rs.next())
            	ingredienti.add(mapResultSetToIngrediente(rs));
        }
        catch(SQLException e)
        {
        	throw new DAOException("Errore DB durante getAllIngredienti", e);
        }
        
        return ingredienti;
    }
	
	@Override
	public Ingrediente getIngredienteById(int idIngrediente)
    {
        String sql =
        		     "SELECT * "
        		   + "FROM Ingrediente "
        		   + "WHERE IdIngrediente = ?";

        try(Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
        {
            s.setInt(1, idIngrediente);
            ResultSet rs = s.executeQuery();
            if(rs.next())
                return mapResultSetToIngrediente(rs);
            else
            	throw new IngredienteNotFoundException("Ingrediente con id " + idIngrediente + " non trovato");
        }
        catch(SQLException e)
        {
        	throw new DAOException("Errore DB durante getIngredienteById", e);
        }
    }
	
	@Override
    public List<Ingrediente> getIngredientiByIdRicetta(int idRicetta)
    {
        List<Ingrediente> ingredienti = new ArrayList<>();
        String sql = 
        			 "SELECT I.* "
        		   + "FROM Ingrediente I JOIN Utilizzi U ON I.IdIngrediente = U.IdIngrediente "
        		   + "WHERE U.IdRicetta = ? "
        		   + "ORDER BY I.Nome";

        try(Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
        {
            s.setInt(1, idRicetta);
            ResultSet rs = s.executeQuery();
            while(rs.next())
                ingredienti.add(mapResultSetToIngrediente(rs));
        }
        catch(SQLException e)
        {
        	throw new DAOException("Errore DB durante getIngredientiByIdRicetta", e);
        }
        
        return ingredienti;
    }
	
	private Ingrediente mapResultSetToIngrediente(ResultSet rs) throws SQLException
	{
		Ingrediente i = new Ingrediente(	
				        	  			   rs.getString("Nome"),
				        	  			   NaturaIngrediente.valueOf(rs.getString("Origine"))
	    			   	    	 	   );
	    i.setId(rs.getInt("IdIngrediente"));	 
	    return i;
	}
}