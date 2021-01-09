package bif3.swe.if20b211.api;

import bif3.swe.if20b211.mctg.models.User;

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

    public boolean userExists(String username) throws SQLException {
        ResultSet q_result = connector.createStatement()
                .executeQuery(String.format("SELECT * FROM \"MUser\" WHERE username = '%s';",username));
        return q_result.next();
    }

    public int addUser(User user) throws SQLException {
        //The sense behind this construct (User and Password Table) should be to add User within a View or Function.
        //In this case you could Hash the password again inside the db and you would be able to deny any direct link
        //To the password from outside the database. I have choosen the short version because i am struggling with the time.
        int rows = 0;
        rows += connector.createStatement()
                .executeUpdate(String.format("INSERT INTO \"MUser\" (username) VALUES ('%s');",user.getUsername()));
        rows += connector.createStatement()
                .executeUpdate(String.format("INSERT INTO \"Password\" (password,username) VALUES ('%s','%s');"
                        ,user.getPassword(),user.getUsername()));
        if(rows >= 2){
            System.out.println("User has been added");
            return 0;
        }else{
            System.err.println("Unkwon error");
            return -1;
        }
    }
}
