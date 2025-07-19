package UninaFoodLab.DAO.Postgres;

import UninaFoodLab.DAO.CorsoDAO;
import UninaFoodLab.DTO.Argomento;
import UninaFoodLab.DTO.Corso;
import UninaFoodLab.DTO.Sessione;
import UninaFoodLab.DTO.SessioneOnline;
import UninaFoodLab.DTO.SessionePratica;
import UninaFoodLab.DTO.FrequenzaSessioni;
import UninaFoodLab.Exceptions.CorsoNotFoundException;
import UninaFoodLab.Exceptions.DAOException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CorsoDAO_Postgres implements CorsoDAO
{
	private Corso mapResultSetToCorso(ResultSet rs) throws SQLException 
	{
	    int idCorso = rs.getInt("IdCorso");
	    
	    ArrayList<Sessione> sessioni = new ArrayList<>();
	    
	    sessioni.addAll(new SessioneOnlineDAO_Postgres().getSessioniOnlineByIdCorso(idCorso));
	    sessioni.addAll(new SessionePraticaDAO_Postgres().getSessioniPraticheByIdCorso(idCorso));
	    
	    Corso c = new Corso
		    				(
		    				  rs.getString("Nome"), 
		    				  rs.getDate("Data").toLocalDate(), 
		    				  rs.getInt("NumeroSessioni"), 
		    				  FrequenzaSessioni.valueOf(rs.getString("FrequenzaSessioni")), 
		    				  rs.getInt("Limite"), 
		    				  rs.getString("Descrizione"), 
		    				  rs.getBigDecimal("Costo"), 
		    				  rs.getBoolean("isPratico"), 
		    				  new ChefDAO_Postgres().getChefById(rs.getInt("IdChef")), 
		    				  new ArrayList<Argomento>(new ArgomentoDAO_Postgres().getArgomentiByIdCorso(idCorso)), 
		    				  sessioni
		    			   );
	    c.setId(rs.getInt("IdCorso"));
	    
	    return c;
	}
	
	@Override
	public void save(Corso toSaveCorso, Connection conn)
	{
	    String sql =
	            "INSERT INTO Corso(Nome, DataInizio, NumeroSessioni, FrequenzaSessioni, Limite, Descrizione, Costo, isPratico, IdChef) " +
	            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

	    try(PreparedStatement s = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
	    {
	        s.setString(1, toSaveCorso.getNome());
	        s.setDate(2, toSaveCorso.getDataInizio());
	        s.setInt(3, toSaveCorso.getNumeroSessioni());
	        s.setString(4, toSaveCorso.getFrequenzaSessioni().toString());
	        s.setInt(5, toSaveCorso.getLimite());
	        s.setString(6, toSaveCorso.getDescrizione());
	        s.setBigDecimal(7, toSaveCorso.getCosto());
	        s.setBoolean(8, toSaveCorso.getIsPratico());
	        s.setInt(9, toSaveCorso.getChef().getId());
	        s.executeUpdate();

	        try(ResultSet genKeys = s.getGeneratedKeys())
	        {
	            if(genKeys.next())
	                toSaveCorso.setId(genKeys.getInt(1));
	            else
	                throw new DAOException("Creazione Corso fallita, nessun ID ottenuto.");
	        }
	    }
	    catch (SQLException e)
	    {
	        throw new DAOException("Errore DB durante salvataggio Corso (conn esterna)", e);
	    }
	}
	
	@Override
	public void save(Corso corso) 
	{
	    Connection conn = null;

	    try
	    {
	        conn = ConnectionManager.getConnection();
	        conn.setAutoCommit(false);

	        // salva solo il corso, con connessione esterna
	        save(corso, conn);

	        // salva gli argomenti
	        new ArgomentoDAO_Postgres().saveArgomentiCorso(corso.getId(), corso.getArgomenti(), conn);
	        
	        // salva tutte le sessioni collegate, sempre con la stessa connessione
	        for(Sessione s : corso.getSessioni())
	        {
	            if(s instanceof SessioneOnline)
	                new SessioneOnlineDAO_Postgres().save((SessioneOnline) s, conn);
	            else if(s instanceof SessionePratica)
	            	new SessionePraticaDAO_Postgres().save((SessionePratica) s, conn);
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
	            	throw new DAOException("Errore durante rollback in salvataggio Corso", ex); 
	            }
	        }
	        throw new DAOException("Errore durante salvataggio transazionale Corso", e);
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
	            catch (SQLException ex) 
	            { 
	            	throw new DAOException("Errore durante chiusura connessione Corso", ex);
	            }
	        }
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
                return mapResultSetToCorso(rs);
            else
            	throw new CorsoNotFoundException("Corso con id " + idCorso + " non trovato");
        }
        catch(SQLException e)
        {
        	throw new DAOException("Errore DB durante getCorsoById", e);
        }
    }

	@Override
    public List<Corso> getCorsiByIdChef(int idChef)
    {
        String sql =
        			 "SELECT * "
        		   + "FROM Corso "
        		   + "WHERE IdChef = ?";
        
        List<Corso> courses = new ArrayList<>();
        
        try(Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
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
	public List<Corso> getCorsiByIdPartecipante(int idPartecipante)
	{
		String sql =
		   			 "SELECT * "
		   		   + "FROM Corso C JOIN Iscrizioni I ON C.IdCorso = I.IdCorso "
		   		   + "WHERE IdPartecipante = ?";
   
	   List<Corso> courses = new ArrayList<>();
	   
	   try(Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
	   {
	       s.setInt(1, idPartecipante);
	       ResultSet rs = s.executeQuery();
	
	       while(rs.next())
	           courses.add(mapResultSetToCorso(rs));
	   }
	   catch(SQLException e)
	   {
	   		throw new DAOException("Errore DB durante getCorsiByIdPartecipante", e);
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
    public List<Corso> getCorsiByArgomenti(List<Integer> argomenti)
    {
		String sql = 
					 "SELECT * "
				   + "FROM Corso C JOIN Argomenti_Corso AC ON C.IdCorso = AC.IdCorso "
				   + "WHERE IdArgomento IN( ";
		
		for(int i = 0; i < argomenti.size(); i++)
		{
			sql += "?";
			if(i < argomenti.size() - 1)
				sql += ", ";
		}
		sql += ")";
		    
		List<Corso> courses = new ArrayList<>();
        
        try(Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
        {
        	for(int i = 0; i < argomenti.size(); i++)
        		s.setInt(i + 1, argomenti.get(i).intValue());
        	
            ResultSet rs = s.executeQuery();

            while(rs.next())
            	courses.add(mapResultSetToCorso(rs));
        }
        catch(SQLException e)
        {
        	throw new DAOException("Errore DB durante getCorsiByArgomenti", e);
        }
        
        return courses;
    }
	
	@Override
    public void update(Corso oldCorso, Corso newCorso)
    {
		String sql = "UPDATE Corso SET ";
        List<Object> param = new ArrayList<>();

        if(! (oldCorso.getNome().equals(newCorso.getNome())) )
        {
            sql += "Nome = ?, ";
            param.add(newCorso.getNome());
        }
        
        if(! (oldCorso.getDescrizione().equals(newCorso.getDescrizione())) )
        {
            sql += "Descrizione = ? ";
            param.add(newCorso.getDescrizione());
        }
        
        if(!param.isEmpty())
        {
        	if(sql.endsWith(", ")) 
        		sql = sql.substring(0, sql.length() - 2);
        	
            sql += " WHERE IdCorso = ?";
            param.add(oldCorso.getId());

            try(Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
            {
                for(int i = 0; i < param.size(); i++)
                    s.setObject(i + 1, param.get(i));

                s.executeUpdate();
            }
            catch(SQLException e)
            {
            	throw new DAOException("Errore DB durante aggiornamento Corso", e);
            }
        }
    }
	
	@Override
    public void delete(int IdCorso)
    {
        String sql = "DELETE "
        		   + "FROM Corso "
        		   + "WHERE IdCorso = ?";
        
        try(Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
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