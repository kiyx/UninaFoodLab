package UninaFoodLab.DAO.Postgres;

import UninaFoodLab.DAO.CorsoDAO;
import UninaFoodLab.DTO.Argomento;
import UninaFoodLab.DTO.Chef;
import UninaFoodLab.DTO.Corso;
import UninaFoodLab.DTO.FrequenzaSessioni;
import UninaFoodLab.Exceptions.CorsoNotFoundException;
import UninaFoodLab.Exceptions.DAOException;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CorsoDAO_Postgres implements CorsoDAO
{
	private Corso mapResultSetToCorso(ResultSet rs) throws SQLException 
	{
	    int idCorso = rs.getInt("IdCorso");
	    
	    return new Corso(
	    				 rs.getString("Nome"), 
	    				 rs.getDate("Data").toLocalDate(), 
	    				 rs.getInt("NumeroSessioni"), 
	    				 FrequenzaSessioni.valueOf(rs.getString("FrequenzaSessioni")), 
	    				 rs.getInt("Limite"), 
	    				 rs.getString("Descrizione"), 
	    				 rs.getBigDecimal("Costo"), 
	    				 rs.getBoolean("isPratico"), 
	    				 new ChefDAO_Postgres().getChefById(rs.getInt("IdChef")), 
	    				 new ArgomentoDAO_Postgres().getArgomentiByIdCorso(idCorso), 
	    				 new SessioneDAO_Postgres().getSessioniByCorso(idCorso));
	}
	
	@Override
	public void save(Corso toSaveCorso)
	{
        String sql =
        		     "INSERT INTO Corso (nome, data, frequenzaSessioni, limite, descrizione, costo, isPratico, idChef) "
        		   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            s.setString(1, toSaveCorso.getNome());
            s.setDate(2, toSaveCorso.getDataInizio());
            s.setString(3, toSaveCorso.getFrequenzaSessioni().toString());
            s.setInt(4, toSaveCorso.getLimite());
            s.setString(5, toSaveCorso.getDescrizione());
            s.setBigDecimal(6, toSaveCorso.getCosto());
            s.setBoolean(7, toSaveCorso.getIsPratico());
            s.setInt(8, toSaveCorso.getChef().getId());
            s.executeUpdate();
            
            try(ResultSet genKeys = s.getGeneratedKeys())
            {
            	if(genKeys.next())
            		toSaveCorso.setId(genKeys.getInt(1));
            	else
            		throw new DAOException("Creazione Corso fallita, nessun ID ottenuto.");
            }   
        }
        catch(SQLException e)
        {
        	throw new DAOException("Errore DB durante salvataggio Corso", e);
        }
    }
	
	@Override
    public Corso getCorsoById(int idCorso)
    {
        String sql = 
        			"SELECT * "
        		  + "FROM Corso "
        		  + "WHERE IdCorso = ?";
        
        try(Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
        {
            s.setInt(1, idCorso);
            ResultSet rs = s.executeQuery();

            if(rs.next())
            {
                return mapResultSetToCorso(rs);
            }
            else
            	throw new CorsoNotFoundException("Corso con id " + idCorso + " non trovato");
        }
        catch(SQLException e)
        {
        	throw new DAOException("Errore DB durante getCorsoById", e);
        }
    }

	@Override
    public List<Corso> getCorsiByChef(int idChef)
    {
        String sql =
        			 "SELECT * "
        		   + "FROM Corso "
        		   + "WHERE IdChef = ?";
        
        List<Corso> courses = new ArrayList<Corso>();
        
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
        {
            s.setInt(1, idChef);
            ResultSet rs = s.executeQuery();

            while(rs.next())
                courses.add(mapResultSetToCorso(rs));
        }
        catch(SQLException e)
        {
        	throw new DAOException("Errore DB durante getCorsiByChef", e);
        }
          
        return courses;
    }

	@Override
    public List<Corso> getAllCorsi()
    {
        String sql =
        			 "SELECT * "
        		   + "FROM Corso";
        
        List<Corso> courses = new ArrayList<>();
        
        try(Connection conn = ConnectionManager.getConnection(); Statement s = conn.createStatement())
        {
            ResultSet rs = s.executeQuery(sql);

            while(rs.next())
            	courses.add(mapResultSetToCorso(rs));
        }
        catch(SQLException e)
        {
        	throw new DAOException("Errore DB durante getAllCorsi", e);
        }
        
        return courses;
    }

	@Override
    public List<Corso> getCorsiByArgomenti(List<Argomento> argomenti)
    {

    }

	@Override
    public void update(Corso oldCorso, Corso newCorso)
    {
        
    }
	
	@Override
    public void delete(int IdCorso)
    {
        String sql = "DELETE "
        		   + "FROM Corso "
        		   + "WHERE IdCorso = ?";
        
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
        {
            s.setInt(1, IdCorso);
            s.executeUpdate();
        }
        catch(SQLException e)
        {
        	throw new DAOException("Errore DB durante eliminazione Corso", e);
        }
    }
}