package bif3.swe.if20b211.api;

import bif3.swe.if20b211.mctg.models.Card;
import bif3.swe.if20b211.mctg.models.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public boolean checkPassword(User user) throws SQLException {
        ResultSet db_pw = connector.createStatement()
                .executeQuery(String.format("SELECT password FROM \"Password\" WHERE username = '%s';",user.getUsername()));
        if(!db_pw.next()) return false;
        return db_pw.getString(1).equals(user.getPassword());
    }

    public int getCoins(String username) throws SQLException {
        ResultSet db_coins = connector.createStatement()
                .executeQuery(String.format("SELECT coins FROM \"MUser\" WHERE username = '%s';",username));
        if(!db_coins.next()) return -1;
        return db_coins.getInt(1);
    }

    public Card[] acquirePackages(int amount) throws SQLException {
        if(amount >= 100) return null;
        //Think about taking all cards out of the database and do the rest inside here.
        List<Card> results = new ArrayList<Card>();
        //https://stackoverflow.com/questions/8115722/generating-unique-random-numbers-in-java
        ArrayList<Integer> randomNumber = new ArrayList<Integer>();
        for (int i=0; i<1000; i++)
            randomNumber.add(i);
        Collections.shuffle(randomNumber);
        for(int i = 0; i < amount;i++){ //Loop through amount of cards to take
            ResultSet db_cards = connector.createStatement()
                    .executeQuery("SELECT * FROM \"Card\" GROUP BY 2;");
            int occ_counter = 0;
            for(int n = 0;n <= 1000;n++){ //Loop 1000*card.occurance times through all cards
                for(int y = 0; y < (amount*5);y++) //Loop each time through cards amount again
                    if(n == randomNumber.get(y)) //Compare with random number (unique)
                        results.add(new Card(
                        db_cards.getString("cardname"),
                        db_cards.getInt("damage"),
                        db_cards.getString("type"),
                        db_cards.getString("is_a"),
                        db_cards.getInt("occurance")));
                if(occ_counter <= db_cards.getInt("occurance")){
                    occ_counter++;
                    n--; //As mentioned above, loop card.occurance times more often through the same card.
                }else {
                    if(!db_cards.next())db_cards.first();
                    occ_counter = 0;
                }
            }
        }
        return results.toArray(new Card[results.size()]);
    }

    public void updateCoins(String username, int amount) {
    }

    public void addToDeck(String username, int amount, Card[] cards) {
    }
}
