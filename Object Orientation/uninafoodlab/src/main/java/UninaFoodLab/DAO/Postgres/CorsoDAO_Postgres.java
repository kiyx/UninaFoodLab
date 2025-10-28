package UninaFoodLab.DAO.Postgres;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import UninaFoodLab.DAO.CorsoDAO;
import UninaFoodLab.DTO.Argomento;
import UninaFoodLab.DTO.Corso;
import UninaFoodLab.DTO.Sessione;
import UninaFoodLab.DTO.SessioneOnline;
import UninaFoodLab.DTO.SessionePratica;
import UninaFoodLab.DTO.FrequenzaSessioni;
import UninaFoodLab.Exceptions.CorsoNotFoundException;
import UninaFoodLab.Exceptions.DAOException;
import UninaFoodLab.Exceptions.IscrizioneNotFoundException;

public class CorsoDAO_Postgres implements CorsoDAO
{	
	@Override
	public void save(Corso toSaveCorso, Connection conn)
	{
	    String sql =
	            "INSERT INTO Corso(Nome, DataInizio, NumeroSessioni, FrequenzaSessioni, Limite, Descrizione, Costo, isPratico, IdChef) " +
	            "VALUES (?, ?, ?, ?::frequenza, ?, ?, ?, ?, ?)";

	    try(PreparedStatement s = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
	    {
	        s.setString(1, toSaveCorso.getNome());
	        s.setDate(2, toSaveCorso.getDataInizio());
	        s.setInt(3, 0);
	        s.setString(4, toSaveCorso.getFrequenzaSessioni().toString());
	        
	        if(toSaveCorso.getIsPratico())
	        	s.setInt(5, toSaveCorso.getLimite());
	        else
	        	s.setNull(5, Types.INTEGER);
	        
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
	        save(corso, conn);

	        new ArgomentoDAO_Postgres().saveArgomentiCorso(corso.getId(), corso.getArgomenti(), conn);
	       
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
	            	ex.printStackTrace();
	            	throw new DAOException("Errore durante rollback in salvataggio Corso", ex); 
	            }
	        }
	        e.printStackTrace();
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
	            	ex.printStackTrace();
	            	throw new DAOException("Errore durante chiusura connessione Corso", ex);
	            }
	        }
	    }
	}
	
	@Override
	public void saveIscrizione(int idCorso, int idPartecipante)
	{
		String sql = "INSERT INTO Iscrizioni(IdCorso, IdPartecipante)"
				   + "VALUES(?, ?)";
		
		try(Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
	    {
			s.setInt(1, idCorso);
			s.setInt(2, idPartecipante);
		    s.executeUpdate();
	    }
		catch(SQLException e)
		{
		   	throw new DAOException("Errore DB durante subscribe", e);
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
    public List<Integer> getCorsiByIdChef(int idChef)
    {
        String sql =
        			 "SELECT IdCorso "
        		   + "FROM Corso "
        		   + "WHERE IdChef = ?";
        
        List<Integer> courses = new ArrayList<>();
        
        try(Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
        {
            s.setInt(1, idChef);
            ResultSet rs = s.executeQuery();

            while(rs.next())
                courses.add(rs.getInt("IdCorso"));
        }
        catch(SQLException e)
        {
        	throw new DAOException("Errore DB durante getCorsiByChef", e);
        }
          
        return courses;
    }

	@Override 
	public List<Integer> getIdCorsiIscrittiByIdPartecipante(int idPartecipante)
	{
		String sql =
		   			 "SELECT * "
		   		   + "FROM Corso C JOIN Iscrizioni I ON C.IdCorso = I.IdCorso "
		   		   + "WHERE IdPartecipante = ?";
   
	   List<Integer> courses = new ArrayList<>();
	   
	   try(Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
	   {
	       s.setInt(1, idPartecipante);
	       ResultSet rs = s.executeQuery();
	
	       while(rs.next())
	           courses.add(rs.getInt("IdCorso"));
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
	public boolean checkIscrizione(int idCorso, int idPartecipante)
	{
		String sql = "SELECT EXISTS (SELECT 1 FROM Iscrizioni WHERE IdCorso = ? AND IdPartecipante = ?)";

		try(Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
		{
		    s.setInt(1, idCorso);
		    s.setInt(2, idPartecipante);
		    ResultSet rs = s.executeQuery();
		
		    if(rs.next())
		        return rs.getBoolean(1);
		    else
		    	throw new IscrizioneNotFoundException("Iscrizione con idCorso: " + idCorso + "e idPartecipante: " + idPartecipante + " non trovato");
		}
		catch(SQLException e)
		{
			throw new DAOException("Errore DB durante checkIscrizione", e);
		}
	}
	
	@Override
	public Integer getNumeroIscrittiById(int idCorso)
	{
		String sql = 
		    			"SELECT COUNT(*) "
		    		  + "FROM Iscrizioni "
		    		  + "WHERE IdCorso = ?";
    
	    try(Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
	    {
	        s.setInt(1, idCorso);
	        ResultSet rs = s.executeQuery();
	
	        if(rs.next())
	            return rs.getInt(1);
	        else
	        	throw new CorsoNotFoundException("Corso con id " + idCorso + " non trovato");
	    }
	    catch(SQLException e)
	    {
	    	throw new DAOException("Errore DB durante getNumeroIscrittiById", e);
	    }
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
            sql += "Descrizione = ?, ";
            param.add(newCorso.getDescrizione());
        }

        if (!oldCorso.getDataInizio().equals(newCorso.getDataInizio()))
        {
            sql += "DataInizio = ?, ";
            param.add(newCorso.getDataInizio());
        }
        
        if(!oldCorso.getFrequenzaSessioni().equals(newCorso.getFrequenzaSessioni()))
        {
            sql += "FrequenzaSessioni = ?::frequenza, ";
            param.add(newCorso.getFrequenzaSessioni().toString());
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
            	throw new DAOException("Errore DB durante aggiornamento Corso: " + e.getMessage(), e);
            }
        }
    }
	
	/**
     * Aggiorna i dati base di un corso (Nome, Descrizione, DataInizio, Frequenza)
     * utilizzando una connessione esterna fornita, senza gestirne l'apertura/chiusura
     * o la transazione.
     *
     * @param oldCorso L'oggetto Corso originale (usato per ID e confronto).
     * @param newCorso L'oggetto Corso con i nuovi dati da salvare.
     * @param conn La connessione SQL esterna da utilizzare.
     * @throws SQLException Se si verifica un errore durante l'esecuzione dell'aggiornamento SQL.
     * @throws DAOException Se non vengono forniti parametri validi per l'aggiornamento.
     */
    public void update(Corso oldCorso, Corso newCorso, Connection conn) throws SQLException, DAOException
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
            sql += "Descrizione = ?, ";
            param.add(newCorso.getDescrizione());
        }

        if (!oldCorso.getDataInizio().equals(newCorso.getDataInizio()))
        {
            sql += "DataInizio = ?, ";
            param.add(newCorso.getDataInizio());
        }

        if(!oldCorso.getFrequenzaSessioni().equals(newCorso.getFrequenzaSessioni()))
        {
            sql += "FrequenzaSessioni = ?::frequenza, ";
            param.add(newCorso.getFrequenzaSessioni().toString());
        }

        if(!param.isEmpty())
        {
            if(sql.endsWith(", "))
                sql = sql.substring(0, sql.length() - 2);

            sql += " WHERE IdCorso = ?";
            param.add(oldCorso.getId());

            try(PreparedStatement s = conn.prepareStatement(sql))
            {
                for(int i = 0; i < param.size(); i++)
                    s.setObject(i + 1, param.get(i));

                s.executeUpdate();
            }
        }
        else
        {
             throw new DAOException("Nessun campo modificato per l'aggiornamento del corso ID: " + oldCorso.getId());
        }
    }
	
	@Override
	public void deleteIscrizione(int idCorso, int idPartecipante)
	{
		String sql = "DELETE "
				   + "FROM Iscrizioni "
				   + "WHERE IdCorso = ? AND IdPartecipante = ?";
		
		try(Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
        {
            s.setInt(1, idCorso);
            s.setInt(2, idPartecipante);
            s.executeUpdate();
        }
        catch(SQLException e)
        {
        	throw new DAOException("Errore DB durante eliminazione Iscrizione", e);
        }
	}
	
	@Override
    public void delete(int idCorso)
    {
        String sql = "DELETE "
        		   + "FROM Corso "
        		   + "WHERE IdCorso = ?";
        
        try(Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
        {
            s.setInt(1, idCorso);
            s.executeUpdate();
        }
        catch(SQLException e)
        {
        	throw new DAOException("Non puoi cancellare un corso che è attivo (con un numero di iscritti maggiore di 0 non ancora terminato)", e);
        }
    }
	
	private Corso mapResultSetToCorso(ResultSet rs) throws SQLException 
	{
	    int idCorso = rs.getInt("IdCorso");
	    
	    ArrayList<Sessione> sessioni = new ArrayList<>();
	    
	    sessioni.addAll(new SessioneOnlineDAO_Postgres().getSessioniOnlineByIdCorso(idCorso));
	    sessioni.addAll(new SessionePraticaDAO_Postgres().getSessioniPraticheByIdCorso(idCorso));
	    
	    Corso c = new Corso
		    				(
		    				  rs.getString("Nome"), 
		    				  rs.getDate("DataInizio").toLocalDate(), 
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
}