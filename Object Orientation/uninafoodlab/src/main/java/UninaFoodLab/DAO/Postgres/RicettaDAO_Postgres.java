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
	
	@Override
    public void save(Ricetta toSaveRicetta, int idChef)
    {
        String sql = 
        		     "INSERT INTO Ricetta(Nome, Provenienza, Tempo, Calorie, Difficolta, Allergeni, IdChef) " +
                     "VALUES(?, ?, ?, ?, ?, ?, ?)";

        try(Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
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
        catch(SQLException e)
        {
        	throw new DAOException("Errore DB durante salvataggio Ricetta", e);
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
    public void update(Ricetta previousRicetta, Ricetta updatedRicetta)
    {
        String sql = "UPDATE Ricetta SET ";
        List<Object> param = new ArrayList<>();

        if(! (previousRicetta.getNome().equals(updatedRicetta.getNome())) )
        {
            sql += "Nome = ?, ";
            param.add(updatedRicetta.getNome());
        }

        if(! (previousRicetta.getProvenienza().equals(updatedRicetta.getProvenienza())) )
        {
            sql += "Provenienza = ?, ";
            param.add(updatedRicetta.getProvenienza());
        }

        if(previousRicetta.getTempo() != updatedRicetta.getTempo())
        {
            sql += "Tempo = ?, ";
            param.add(updatedRicetta.getTempo());
        }

        if(previousRicetta.getCalorie() != updatedRicetta.getCalorie())
        {
            sql += "Calorie = ?, ";
            param.add(updatedRicetta.getCalorie());
        }

        if(! (previousRicetta.getDifficolta().equals(updatedRicetta.getDifficolta())) )
        {
            sql += "Difficolta = ?, ";
            param.add(updatedRicetta.getDifficolta());
        }

        if(! (previousRicetta.getAllergeni().equals(updatedRicetta.getAllergeni())) )
        {
            sql += "Allergeni = ? ";
            param.add(updatedRicetta.getAllergeni());
        }

        if(!param.isEmpty())
        {
        	if(sql.endsWith(", ")) 
        		sql = sql.substring(0, sql.length() - 2);
        	
            sql += " WHERE IdRicetta = ?";
            param.add(previousRicetta.getId());

            try(Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
            {
                for(int i = 0; i < param.size(); i++)
                    s.setObject(i + 1, param.get(i));

                s.executeUpdate();
            }
            catch(SQLException e)
            {
            	throw new DAOException("Errore DB durante update Ricetta", e);
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
}