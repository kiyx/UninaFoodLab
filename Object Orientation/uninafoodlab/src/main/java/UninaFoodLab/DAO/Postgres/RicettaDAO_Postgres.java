package UninaFoodLab.DAO.Postgres;

import UninaFoodLab.DAO.RicettaDAO;
import UninaFoodLab.DTO.LivelloDifficolta;
import UninaFoodLab.DTO.Ricetta;
import UninaFoodLab.DTO.Utilizzo;
import UninaFoodLab.Exceptions.DAOException;
import UninaFoodLab.Exceptions.RicettaNotFoundException;

import java.sql.*;
import java.util.*;

public class RicettaDAO_Postgres implements RicettaDAO
{
	@Override
    public void save(Ricetta toSaveRicetta, int idChef)
    {
        String sql = 
        		     "INSERT INTO Ricetta(Nome, Provenienza, Tempo, Calorie, Difficolta, Allergeni, IdChef) " +
                     "VALUES(?, ?, ?, ?, ?::livellodifficolta, ?, ?)";
        Connection conn = null;

        try
        {
        	conn = ConnectionManager.getConnection();
        	conn.setAutoCommit(false);
        	
        	try(PreparedStatement s = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        	{
                s.setString(1, toSaveRicetta.getNome());
                s.setString(2, toSaveRicetta.getProvenienza());
                s.setInt(3, toSaveRicetta.getTempo());
                s.setInt(4, toSaveRicetta.getCalorie());
                s.setString(5, toSaveRicetta.getDifficolta().toString());
                s.setString(6, toSaveRicetta.getAllergeni());
                s.setInt(7, idChef);
                s.executeUpdate();
                
                try(ResultSet genKeys = s.getGeneratedKeys())
                {
                	if(genKeys.next())
                		toSaveRicetta.setId(genKeys.getInt(1));
                	else
                		throw new DAOException("Creazione Ricetta fallita, nessun ID ottenuto.");
                }           		
        	}

        	for(Utilizzo util: toSaveRicetta.getUtilizzi())
        	{
        		util.setIdRicetta(toSaveRicetta.getId());
        		new UtilizzoDAO_Postgres().save(util, conn);
        	}
	        conn.commit();
        }
        catch(Exception e)
	    {
	        if(conn != null)
	        {
	            try
	            {
	                conn.rollback();
	            }
	            catch(SQLException ex)
	            {
	            	 throw new DAOException("Errore durante rollback transazionale Utilizzi", ex);
	            }
	        }

	        throw new DAOException("Errore durante salvataggio transazionale Utilizzi", e);
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
	            	 throw new DAOException("Errore chiusura connessione Utilizzi", ex);
	            }
	        }
	    }
    }
	
	@Override
	public Ricetta getRicettaById(int idRicetta)
    {
        String sql = 
        			"SELECT * "
        		  + "FROM Ricetta "
        		  + "WHERE IdRicetta = ?";

        try(Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
        {
            s.setInt(1, idRicetta);
            ResultSet rs = s.executeQuery();
            if(rs.next())
                return mapResultSetToRicetta(rs);
            else
            	throw new RicettaNotFoundException("Ricetta con id " + idRicetta + " non trovato");
        }
        catch(SQLException e)
        {
        	throw new DAOException("Errore DB durante getRicettaById", e);
        }
    }
	
	@Override
	public Ricetta getRicettaByNome(Ricetta toSaveRicetta, int idChef)
	{
		String sql = "SELECT *FROM Ricetta WHERE Nome = ? AND IdChef = ?";

        try(Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
        {
            s.setString(1, toSaveRicetta.getNome());
            s.setInt(2, idChef);
            ResultSet rs = s.executeQuery();

            if(rs.next())
            	return mapResultSetToRicetta(rs);
            else
            	return null;
            
        }
        catch(SQLException e)
        {
        	throw new DAOException("Errore DB durante getRicettaByNome", e);
        }
	}
	
	@Override
    public List<Ricetta> getRicetteByIdChef(int idChef)
    {
        List<Ricetta> ricette = new ArrayList<>();
        String sql = 
        		     "SELECT * "
        		   + "FROM Ricetta "
        		   + "WHERE IdChef = ?";

        try(Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
        {
            s.setInt(1, idChef);
            ResultSet rs = s.executeQuery();
            while(rs.next())
                ricette.add(mapResultSetToRicetta(rs));
        }
        catch(SQLException e)
        {
        	throw new DAOException("Errore DB durante getRicetteByIdchef", e);
        }
        
        return ricette;
    }
	
	@Override
	public boolean existsRicettaByNome(Ricetta toSaveRicetta, int idChef)
	{
		String sql = "SELECT EXISTS (SELECT 1 FROM Ricetta WHERE Nome = ? AND IdChef = ?)";

        try(Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
        {
            s.setString(1, toSaveRicetta.getNome());
            s.setInt(2, idChef);
            ResultSet rs = s.executeQuery();

            return rs.next() && rs.getBoolean(1);
        }
        catch(SQLException e)
        {
        	throw new DAOException("Errore DB durante ricerca di Partecipante per codice fiscale", e);
        }
	}
	
	@Override
    public List<Ricetta> getRicettaByIdSessionePratica(int idSessionePratica)
    {
        List<Ricetta> ricette = new ArrayList<>();
        String sql = 
        			"SELECT * "
        		  + "FROM Ricetta R JOIN Preparazioni P ON R.IdRicetta = P.IdRicetta "
        		  + "WHERE P.IdSessionePratica = ?";

        try(Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
        {
            s.setInt(1, idSessionePratica);
            ResultSet rs = s.executeQuery();
            while(rs.next())
            	 ricette.add(mapResultSetToRicetta(rs));
        }
        catch(SQLException e)
        {
        	throw new DAOException("Errore DB durante getRicettaByIdSessione", e);
        }
        
        return ricette;
    }

	@Override
    public boolean isRicettaUsataInSessioni(int idRicetta)
	{

        String sql = "SELECT EXISTS (" +
                     "  SELECT 1 " +
                     "  FROM Preparazioni p " +
                     "  WHERE p.IdRicetta = ? " +
                     ")";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement s = conn.prepareStatement(sql)) 
        {

            s.setInt(1, idRicetta);
            ResultSet rs = s.executeQuery();

            return rs.next() && rs.getBoolean(1);
        } 
        catch (SQLException e) 
        {
            throw new DAOException("Errore DB durante check isRicettaUsataInSessioni", e);
        }
    }
	
	@Override
	public void update(Ricetta previousRicetta, Ricetta updatedRicetta, 
	                   ArrayList<Utilizzo> toAddUtilizzi, 
	                   ArrayList<Utilizzo> toUpdateUtilizzi, 
	                   ArrayList<Utilizzo> toDeleteUtilizzi) throws DAOException {
	    
	    StringBuilder sql = new StringBuilder("UPDATE Ricetta SET ");
	    List<Object> param = new ArrayList<>();
	    Connection conn = null;

	    if (!java.util.Objects.equals(previousRicetta.getNome(), updatedRicetta.getNome())) 
	    {
	        sql.append("Nome = ?, ");
	        param.add(updatedRicetta.getNome());
	    }

	    if (!java.util.Objects.equals(previousRicetta.getProvenienza(), updatedRicetta.getProvenienza())) 
	    {
	        sql.append("Provenienza = ?, ");
	        param.add(updatedRicetta.getProvenienza());
	    }

	    if (previousRicetta.getTempo() != updatedRicetta.getTempo()) 
	    {
	        sql.append("Tempo = ?, ");
	        param.add(updatedRicetta.getTempo());
	    }

	    if (previousRicetta.getCalorie() != updatedRicetta.getCalorie()) 
	    {
	        sql.append("Calorie = ?, ");
	        param.add(updatedRicetta.getCalorie());
	    }

	    if(!java.util.Objects.equals(previousRicetta.getDifficolta(), updatedRicetta.getDifficolta())) 
	    {
	        sql.append("Difficolta = ?::livellodifficolta, ");
	        param.add(updatedRicetta.getDifficolta().toString()); 
	    }

	    if(!java.util.Objects.equals(previousRicetta.getAllergeni(), updatedRicetta.getAllergeni())) 
	    {
	        sql.append("Allergeni = ? ");
	        param.add(updatedRicetta.getAllergeni());
	    }
	    
	    if(param.isEmpty()) 
	    {

	    } 
	    else
	    {	        
	        if(sql.charAt(sql.length() - 2) == ',')
	             sql.setLength(sql.length() - 2);

	        sql.append(" WHERE IdRicetta = ?");
	        param.add(previousRicetta.getId());
	    }

	    try 
	    {
	        conn = ConnectionManager.getConnection();
	        conn.setAutoCommit(false);


	        if(!param.isEmpty()) 
	        {
	            try(PreparedStatement s = conn.prepareStatement(sql.toString())) 
	            {
	                for(int i = 0; i < param.size(); i++) 
	                    s.setObject(i + 1, param.get(i));

	                s.executeUpdate();
	            }
	        }
	        
	        
	        for(Utilizzo util : toDeleteUtilizzi) 
	             new UtilizzoDAO_Postgres().delete(util.getIdRicetta(), util.getIngrediente().getId(), conn);
	        
	        for(Utilizzo util : toUpdateUtilizzi) 
	        	new UtilizzoDAO_Postgres().update(util, conn); 
	        
	        for(Utilizzo util : toAddUtilizzi) 
	        {
	            util.setIdRicetta(previousRicetta.getId());
	            new UtilizzoDAO_Postgres().save(util, conn); 
	        }

	        conn.commit();

	    } 
	    catch(SQLException e) 
	    {
	        if(conn != null) 
	        {
	            try 
	            {
	                conn.rollback();
	            } 
	            catch (SQLException ex) 
	            {
	                throw new DAOException("Errore durante rollback transazionale Ricetta/Utilizzi", ex);
	            }
	        }
	        throw new DAOException("Errore DB durante update Ricetta/Utilizzi", e);
	    } 
	    catch(DAOException e) 
	    {     
	        if(conn != null) 
	        {
	            try 
	            {
	                conn.rollback(); 
	            } 
	            catch(SQLException ex) 
	            {
	                throw new DAOException("Errore durante rollback transazionale Ricetta/Utilizzi", ex);
	            }
	        }
	        throw e; 
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
	                throw new DAOException("Errore chiusura connessione Ricetta/Utilizzi", ex);
	            }
	        }
	    }
	}

	@Override
    public void delete(int idRicetta)
    {
        String sql =
        		     "DELETE "
        		   + "FROM Ricetta "
        		   + "WHERE IdRicetta = ?";

        try(Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
        {
            s.setInt(1, idRicetta);
            s.executeUpdate();
        }
        catch(SQLException e)
        {
        	throw new DAOException("Errore DB durante eliminazione Ricetta", e);
        }
    }
	
	private Ricetta mapResultSetToRicetta(ResultSet rs) throws SQLException
	{
	    Ricetta r = new Ricetta(
						    	   rs.getString("Nome"),
					               rs.getString("Provenienza"),
					               rs.getInt("Tempo"),
					               rs.getInt("Calorie"),
					               LivelloDifficolta.valueOf(rs.getString("Difficolta")),
					               rs.getString("Allergeni"),
					               new ChefDAO_Postgres().getChefById(rs.getInt("IdChef")),
					               new ArrayList<Utilizzo>(new UtilizzoDAO_Postgres().getUtilizziByIdRicetta(rs.getInt("IdRicetta")))
	    			   	    	);
	    r.setId(rs.getInt("IdRicetta"));	    
	    return r;
	}
}