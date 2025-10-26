package UninaFoodLab.DAO.Postgres;

import java.sql.*;

public class ConnectionManager
{
    private static ConnectionManager instance = null;
    private static Connection conn = null;

    private static final String url = "jdbc:postgresql://localhost:5432/UninaFoodLab";
    private static final String user = "postgres";
    private static final String password = "hello123";

    private ConnectionManager() {}

    public static ConnectionManager getInstance()
    {
        if(instance == null)
            instance = new ConnectionManager();
        return instance;
    }

     public static Connection getConnection() throws SQLException
    {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}