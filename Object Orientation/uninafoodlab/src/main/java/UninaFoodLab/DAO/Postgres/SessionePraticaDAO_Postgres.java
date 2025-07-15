package UninaFoodLab.DAO.Postgres;

import UninaFoodLab.DAO.SessionePraticaDAO;
import UninaFoodLab.DTO.SessioneOnline;
import UninaFoodLab.DTO.SessionePratica;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SessionePraticaDAO_Postgres implements SessionePraticaDAO
{
    public SessionePratica getSessionePraticaById(int id)
    {
        String sql ="SELECT * FROM SessionePratica WHERE IdCorso = ?";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
        {
            s.setInt(1, id);
            ResultSet rs = s.executeQuery();

            while(rs.next())
            {
                return new SessionePratica(
                        rs.getInt("Durata"),
                        rs.getTime("Orario"),
                        rs.getDate("Data").toLocalDate(),
                        rs.getString("Indirizzo"),
                        null,
                        null,
                        null
                );
            }
        }
        return null;
    }

    public List<SessionePratica> getSessioniPraticheByIdCorso(int idCorso)
    {
        String sql ="SELECT * FROM SessionePratica WHERE IdCorso = ?";
        List<SessionePratica> ret =new ArrayList<SessionePratica>();
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
        {
            s.setInt(1, idCorso);
            ResultSet rs = s.executeQuery();

            while(rs.next())
            {
                ret.add(new SessionePratica(
                        rs.getInt("Durata"),
                        rs.getTime("Orario"),
                        rs.getDate("Data").toLocalDate(),
                        rs.getString("Indirizzo"),
                        null,
                        null,
                        null
                )) ;
            }
        }
        return ret;
    }

    public void save(SessionePratica toSaveSessione)
    {
        String sql = "INSERT INTO SessioneOnline (durata, orario, data, numeroPartecipanti, indirizzo)  VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
        {
            s.setInt(1, toSaveSessione.getDurata());
            s.setTime(2, toSaveSessione.getOrario());
            s.setDate(3, toSaveSessione.getData());
            s.setInt(4, toSaveSessione.getNumeroPartecipanti());
            s.setString(5, toSaveSessione.getIndirizzo());

            s.executeUpdate();
        }
    }

    

    public void update(SessioneOnline oldSessione, SessioneOnline newSessione)
    {
        if(!oldSessione.getLinkRiunione().equals(newSessione.getLinkRiunione()))
        {
            String sql = "UPDATE SessioneOnline SET LinkRiunione = ? WHERE IdSessioneOnline = ?";
            try (Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
            {
                s.setString(1, newSessione.getLinkRiunione());
                s.setInt(2, newSessione.getId());
                s.executeUpdate();
            }
        }
    }
    
    public void delete(int IdSessionePratica)
    {
        String sql = "DELETE FROM SessionePratica WHERE IdSessionePratica = ?";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
        {
            s.setInt(1, IdSessionePratica);
            s.executeUpdate();
        }

    }
}
