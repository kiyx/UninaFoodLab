package UninaFoodLab.DAO.Postgres;

import UninaFoodLab.DAO.UtilizzoDAO;
import UninaFoodLab.DTO.Ingrediente;
import UninaFoodLab.DTO.NaturaIngrediente;
import UninaFoodLab.DTO.UnitaDiMisura;
import UninaFoodLab.DTO.Utilizzo;
import UninaFoodLab.Exceptions.DAOException;

import java.sql.*;
import java.util.*;

public class UtilizzoDAO_Postgres implements UtilizzoDAO
{
	private Utilizzo mapResultSetToUtilizzo(ResultSet rs) throws SQLException
	{
		Ingrediente i = new Ingrediente(rs.getString("Nome"), NaturaIngrediente.valueOf(rs.getString("Origine")));
		i.setId(rs.getInt("IdIngrediente"));
		
	    Utilizzo u = new Utilizzo(	
				        	  		rs.getDouble("Quantita"),
				        	  		UnitaDiMisura.valueOf(rs.getString("UDM")),
				        	  		i
	    			   	    	 );
	    
	    u.setIdRicetta(rs.getInt("IdRicetta"));	 
	    u.setIdIngrediente(rs.getInt("IdIngrediente"));	 
	    return u;
	}
	
	@Override
    public void save(Utilizzo toSaveUtilizzo, Connection conn)
    {
		 String sql = 
				 	  "INSERT INTO Utilizzi(IdRicetta, IdIngrediente, Quantita, UDM) " +
                 	  "VALUES(?, ?, ?, ?::unitadimisura)";

	    try(PreparedStatement s = conn.prepareStatement(sql))
	    {
	        s.setInt(1, toSaveUtilizzo.getIdRicetta());
	        s.setInt(2, toSaveUtilizzo.getIdIngrediente());
	        s.setDouble(3, toSaveUtilizzo.getQuantita());
	        s.setString(4, toSaveUtilizzo.getUdm().toString());
	        s.executeUpdate();
	    }
	    catch(SQLException e)
	    {
	    	throw new DAOException("Errore DB durante salvataggio Utilizzo", e);
	    }
    }
	
	@Override
    public List<Utilizzo> getUtilizziByIdRicetta(int idRicetta)
    {
        List<Utilizzo> utilizzi = new ArrayList<>();
        String sql = 
        			"SELECT * "
        		  + "FROM Utilizzi U JOIN Ingrediente I ON U.IdIngrediente = I.IdIngrediente "
        		  + "WHERE IdRicetta = ?";

        try(Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
        {
            s.setInt(1, idRicetta);
            ResultSet rs = s.executeQuery();
            while(rs.next())
                utilizzi.add(mapResultSetToUtilizzo(rs));
        }
        catch(SQLException e)
        {
        	throw new DAOException("Errore DB durante getUtilizziByIdRicetta", e);
        }
        
        return utilizzi;
    }

	@Override
	public void update(Utilizzo previousUtilizzo, Utilizzo updatedUtilizzo)
	{
	    String sql = "UPDATE Utilizzi SET ";
	    List<Object> param = new ArrayList<>();

	    if (previousUtilizzo.getQuantita() != updatedUtilizzo.getQuantita())
	    {
	        sql += "Quantita = ?, ";
	        param.add(updatedUtilizzo.getQuantita());
	    }

	    if (!previousUtilizzo.getUdm().equals(updatedUtilizzo.getUdm()))
	    {
	        sql += "UDM = ?";
	        param.add(updatedUtilizzo.getUdm().toString());
	    }

	    if (!param.isEmpty())
	    {
	        if (sql.endsWith(", "))
	            sql = sql.substring(0, sql.length() - 2);

	        sql += " WHERE IdRicetta = ? AND IdIngrediente = ?";
	        param.add(previousUtilizzo.getIdRicetta());
	        param.add(previousUtilizzo.getIdIngrediente());

	        try(Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
	        {
	            for (int i = 0; i < param.size(); i++)
	                s.setObject(i + 1, param.get(i));

	            s.executeUpdate();
	        }
	        catch (SQLException e)
	        {
	            throw new DAOException("Errore DB durante aggiornamento Utilizzo", e);
	        }
	    }
	}

    @Override
    public void delete(int idRicetta, int idIngrediente)
    {
        String sql = 
        			 "DELETE "
        		   + "FROM Utilizzi "
        		   + "WHERE IdRicetta = ? AND IdIngrediente = ?";

        try(Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
        {
            s.setInt(1, idRicetta);
            s.setInt(2, idIngrediente);
            s.executeUpdate();
        }
        catch(SQLException e)
        {
        	throw new DAOException("Errore DB durante delete di Utilizzo", e);
        }
    }
}