package UninaFoodLab.DAO.Postgres;

import UninaFoodLab.DAO.SessioneOnlineDAO;
import UninaFoodLab.DTO.Corso;
import UninaFoodLab.DTO.SessioneOnline;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SessioneOnlineDAO_Postgres implements SessioneOnlineDAO
{
	
	
	
	@Override
    public void save(SessioneOnline toSaveSessione) throws SQLException 
    {
        String sql = "INSERT INTO SessioneOnline (durata, orario, data, link)  VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
        {
            s.setInt(1, toSaveSessione.getDurata());
            s.setTime(2, toSaveSessione.getOrario());
            s.setDate(3, toSaveSessione.getData());
            s.setString(4, toSaveSessione.getLinkRiunione());

            s.executeUpdate();
        }
    }

	
	@Override
    public SessioneOnline getSessioneOnlineById(int id) throws SQLException{
        String sql ="SELECT * FROM SessioneOnline WHERE IdCorso = ?";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
        {
            s.setInt(1, id);
            ResultSet rs = s.executeQuery();

            while(rs.next())
            {
                return new SessioneOnline(
                        rs.getInt("Durata"),
                        rs.getTime("Orario"),
                        rs.getDate("Data").toLocalDate(),
                        rs.getString("LinkRiunione")
                );
            }
        }
        return null;
    }

	@Override
    public List<SessioneOnline> getSessioniOnlineByCorso(int idCorso) throws SQLException
    {
        String sql ="SELECT * FROM SessioneOnline WHERE IdCorso = ?";
        List<SessioneOnline> ret = new ArrayList<SessioneOnline>();
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
        {
            s.setInt(1, idCorso);
            ResultSet rs = s.executeQuery();

            while(rs.next())
            {
                ret.add(new SessioneOnline(
                        rs.getInt("Durata"),
                        rs.getTime("Orario"),
                        rs.getDate("Data").toLocalDate(),
                        rs.getString("LinkRiunione")
                )) ;
            }
        }
        return ret;
    }
	
	@Override
    public void update(SessioneOnline oldSessione, SessioneOnline newSessione) throws SQLException
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

	@Override
    public void delete(int IdSessioneOnline) throws SQLException 
    {
        String sql = "DELETE FROM Corso WHERE IdSessioneOnline = ?";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
        {
            s.setInt(1, IdSessioneOnline);
            s.executeUpdate();
        }

    }
	
}
