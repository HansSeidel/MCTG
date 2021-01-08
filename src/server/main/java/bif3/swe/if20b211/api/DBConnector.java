package bif3.swe.if20b211.api;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBConnector {
    private String url;
    private String un;
    private String pw;
    private Connection connector;
    public DBConnector(String url, String un, String pw) throws SQLException {
        this.url = url;
        this.un = un;
        this.pw = pw;

        this.connector = DriverManager.getConnection(url,un,pw);
        System.out.println("Established connection to database");
        if(this.testConnection()) System.out.printf("Query test passed");
    }

    public boolean testConnection(){
        return this.testConnection(false);
    }

    public boolean testConnection(boolean printResult) {
        try {
            ResultSet q_result = connector.createStatement()
                    .executeQuery("SELECT * FROM information_schema.tables;");
            if(!q_result.next()) return false;
            if (printResult)
                System.out.printf("Test result: %s %s %s",q_result.getString(1),q_result.getString(2),q_result.getString(3));
            if(q_result.wasNull()) return false;
        } catch (SQLException throwables) {
            System.err.println("Connection Error!");
            throwables.printStackTrace();
            return false;
        }
        return true;
    }
}
