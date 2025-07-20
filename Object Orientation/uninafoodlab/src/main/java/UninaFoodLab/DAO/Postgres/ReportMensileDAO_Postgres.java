package UninaFoodLab.DAO.Postgres;

import java.sql.*;

import UninaFoodLab.DAO.ReportMensileDAO;
import UninaFoodLab.DTO.ReportMensile;
import UninaFoodLab.Exceptions.DAOException;

public class ReportMensileDAO_Postgres implements ReportMensileDAO
{
	@Override
	public ReportMensile getMonthlyReportByIdChef(int idChef)
	{
		String sql = 
					"""
						    WITH
						    CorsiChef AS 
						    (
						        SELECT IdCorso
						        FROM Corso
						        WHERE IdChef = ?
						    ),
						    
						    SessioniPraticheRecentiChef AS 
						    (
						        SELECT
						            sp.IdSessionePratica,
						            sp.IdCorso,
						            sp.Data
						        FROM
						            SessionePratica sp
						        JOIN
						            CorsiChef cc ON sp.IdCorso = cc.IdCorso
						        WHERE
						            sp.Data >= CURRENT_DATE - INTERVAL '30 days'
						            AND sp.Data < CURRENT_DATE
						    ),
						    
						    SessioniOnlineRecentiChef AS 
						    (
						        SELECT
						            so.IdSessioneOnline,
						            so.IdCorso,
						            so.Data
						        FROM
						            SessioneOnline so
						        JOIN
						            CorsiChef cc ON so.IdCorso = cc.IdCorso
						        WHERE
						            so.Data >= CURRENT_DATE - INTERVAL '30 days'
						            AND so.Data < CURRENT_DATE
						    ),
						    
						    NumRicetteSessioniRecentiChef AS 
						    (
						        SELECT
						            sprc.IdSessionePratica,
						            COALESCE(COUNT(p.IdRicetta), 0) AS numRicette
						        FROM
						            SessioniPraticheRecentiChef sprc
						        LEFT JOIN
						            Preparazioni p ON sprc.IdSessionePratica = p.IdSessionePratica
						        GROUP BY
						            sprc.IdSessionePratica
						    )
						    
						    SELECT
						        (SELECT COUNT(DISTINCT IdCorso) FROM (SELECT IdCorso FROM SessioniPraticheRecentiChef 
						        									  UNION 
						        									  SELECT IdCorso FROM SessioniOnlineRecentiChef) AS AllRecentCourses) AS totCorsi,
						        
						        (SELECT COUNT(DISTINCT IdSessioneOnline) FROM SessioniOnlineRecentiChef) AS totOnline,
						        (SELECT COUNT(DISTINCT IdSessionePratica) FROM SessioniPraticheRecentiChef) AS totPratiche,
						        COALESCE(MIN(numRicette), 0) AS minRicette,
						        COALESCE(MAX(numRicette), 0) AS maxRicette,
						        COALESCE(ROUND(AVG(numRicette)::numeric, 2), 0.00) AS avgRicette
						    FROM
						        NumRicetteSessioniRecentiChef;
				""";

		
		try(Connection conn = ConnectionManager.getConnection(); PreparedStatement s = conn.prepareStatement(sql))
		{
			s.setInt(1, idChef);
			
			try(ResultSet rs = s.executeQuery())
			{
				return (rs.next()) ? mapResultSetToMonthlyReport(rs) : new ReportMensile(0, 0, 0, 0, 0, 0.0);
			}		
		} 
		catch (SQLException e)
		{
			throw new DAOException("Errore durante l'accesso al report mensile dello chef", e);
		}
	}
	
	private ReportMensile mapResultSetToMonthlyReport(ResultSet rs) throws SQLException
	{
		ReportMensile rm = new ReportMensile(
									           rs.getInt("totCorsi"),
									           rs.getInt("totOnline"),
									           rs.getInt("totPratiche"),
									           rs.getInt("minRicette"),
									           rs.getInt("maxRicette"),
									           rs.getDouble("avgRicette")
					    			   	 	);    
	    return rm;
	}	
}