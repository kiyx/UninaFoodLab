package UninaFoodLab.DAO.Postgres;

import UninaFoodLab.DAO.UtilizzoDAO;
import UninaFoodLab.DTO.Ingrediente;
import UninaFoodLab.DTO.NaturaIngrediente;
import UninaFoodLab.DTO.Ricetta;
import UninaFoodLab.DTO.UnitaDiMisura;
import UninaFoodLab.DTO.Utilizzo;
import UninaFoodLab.Exceptions.DAOException;

import java.sql.*;
import java.util.*;

public class UtilizzoDAO_Postgres implements UtilizzoDAO
{
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
	public void update(Utilizzo updatedUtilizzo, Connection conn) throws DAOException 
	{
	    // Aggiungiamo il cast esplicito ::unitadimisura alla placeholder UDM = ?
	    String sql = "UPDATE Utilizzi SET Quantita = ?, UDM = ?::unitadimisura WHERE IdRicetta = ? AND IdIngrediente = ?";
	    
	    try(PreparedStatement s = conn.prepareStatement(sql))
	    {
	        // 1. Quantita (Double)
	        s.setDouble(1, updatedUtilizzo.getQuantita());
	        
	        // 2. UDM (ENUM): Passiamo la Stringa del valore ENUM Java
	        s.setString(2, updatedUtilizzo.getUdm().toString()); 
	        
	        // 3. IdRicetta (per la clausola WHERE)
	        s.setInt(3, updatedUtilizzo.getIdRicetta()); 
	        
	        // 4. IdIngrediente (per la clausola WHERE)
	        s.setInt(4, updatedUtilizzo.getIngrediente().getId()); 

	        int rowsAffected = s.executeUpdate();
	        
	        // Aggiungi un controllo per debug/log:
	        if (rowsAffected == 0) {
	            // Questo potrebbe accadere se la riga non esiste o se gli ID sono errati.
	            System.err.println("AVVISO: Aggiornamento Utilizzo non ha modificato alcuna riga. ID Ricetta: " + updatedUtilizzo.getIdRicetta());
	        }
	    }
	    catch (SQLException e)
	    {
	        // L'eccezione ora dovrebbe riguardare solo problemi di connessione o integrità dei dati.
	        throw new DAOException("Errore DB durante aggiornamento Utilizzo", e);
	    }
	}

	@Override
	public void delete(int idRicetta, int idIngrediente, Connection conn) throws DAOException 
	{
	    String sql = 
	                 "DELETE "
	               + "FROM Utilizzi "
	               + "WHERE IdRicetta = ? AND IdIngrediente = ?";

	    // NOTA: Usiamo la connessione 'conn' passata e non apriamo/chiudiamo nulla
	    try(PreparedStatement s = conn.prepareStatement(sql))
	    {
	        s.setInt(1, idRicetta);
	        s.setInt(2, idIngrediente);
	        s.executeUpdate();
	    }
	    catch(SQLException e)
	    {
	        // Se si verifica un errore SQL qui, l'eccezione viene catturata dal RicettaDAO,
	        // che eseguirà il rollback per l'intera transazione.
	        throw new DAOException("Errore DB durante delete di Utilizzo", e);
	    }
	}
    
    public void deleteUtilizziRicetta(Ricetta toDeleteRicetta, Connection conn)
    {
        String sql = 
        			 "DELETE "
        		   + "FROM Utilizzi "
        		   + "WHERE IdRicetta = ? ";

        try(PreparedStatement s = conn.prepareStatement(sql))
        {
            s.setInt(1, toDeleteRicetta.getId());
            s.executeUpdate();
        }
        catch(SQLException e)
        {
        	throw new DAOException("Errore DB durante delete di Utilizzo", e);
        }
    }
    
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
}