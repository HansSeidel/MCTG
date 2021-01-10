package bif3.swe.if20b211.api;

import bif3.swe.if20b211.http.Json_form;
import bif3.swe.if20b211.mctg.models.Card;
import bif3.swe.if20b211.mctg.models.User;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    public List<Card> acquirePackages(int amount) throws SQLException {
        if(amount >= 10) return null;
        //Think about taking all cards out of the database and do the rest inside here.
        List<Card> results = new ArrayList<Card>();
        ResultSet db_cards = connector.createStatement()
                .executeQuery("SELECT SUM(occurance) FROM \"Card\"");
        db_cards.next();
        int randomRange = db_cards.getInt(1);
        for(int i = 0; i < amount;i++){ //Loop through amount of packages to take
            db_cards = connector.createStatement()
                    .executeQuery("SELECT * FROM \"Card\";");
            db_cards.next();
            //https://stackoverflow.com/questions/8115722/generating-unique-random-numbers-in-java
            List<Integer> randomNumber = IntStream.range(0, randomRange).boxed()
                    .collect(Collectors.toCollection(ArrayList::new));
            Collections.shuffle(randomNumber);
            randomNumber = new ArrayList<>(randomNumber.subList(0, 5));
            randomNumber.forEach(num -> System.out.println("Inside randNum: " + num));
            int occ_counter = 0;
            for(int n = 0;n <= randomRange;n++){ //Loop cardCount*card.occurance times through all cards
                if(randomNumber.contains(n)){
                    //Compare with random number (unique)
                    results.add(new Card(
                            db_cards.getString("cardname"),
                            db_cards.getInt("damage"),
                            db_cards.getString("type"),
                            db_cards.getString("is_a"),
                            db_cards.getInt("occurance")));
                }
                if(occ_counter < db_cards.getInt("occurance")){
                    occ_counter++;
                }else {
                    db_cards.next();
                    occ_counter = 0;
                }
            }
        }
        return results;
    }

    public void updateCoins(String username, int amount) throws SQLException {
        connector.createStatement()
                .executeUpdate(String.format("UPDATE \"MUser\" SET coins = coins - %d WHERE username = '%s';",
                        amount*5,username));
    }

    public List<Card> manageDeck(String username, String cardname, String action) throws SQLException {
        System.out.println("ACTION: " + action);
        int i = connector.createStatement()
                .executeUpdate(String.format("UPDATE \"Stack\" SET in_deck = %s " +
                        "WHERE username = '%s' AND cardname = '%s';",
                        action.equals("add")?"true":"false",username,cardname));
        if(i != 1){
            return null;
        }
        return getDeck(username);
    }

    public List<Card> getDeck(String username) throws SQLException {
        List<Card> results = new ArrayList<Card>();
        ResultSet cards_in_deck = connector.createStatement()
                .executeQuery(String.format("SELECT * FROM \"Stack\" WHERE in_deck = true AND username = '%s';",
                        username));
        while (cards_in_deck.next()){
            ResultSet card = connector.createStatement().executeQuery(String.format("SELECT * FROM \"Card\" WHERE cardname = '%s';",
                    cards_in_deck.getString("cardname")));
            card.next();
            results.add(new Card(
                    card.getString("cardname"),
                    card.getInt("damage"),
                    card.getString("type"),
                    card.getString("is_a"),
                    card.getInt("occurance")));
        }
        return results;
    }

    public void addToStack(String username,int amount, List<Card> cards) throws SQLException {
        AtomicBoolean rollback = new AtomicBoolean(true);
        //Distinct
        cards = cards.stream().distinct().collect(Collectors.toList());
        List<String> doesOwn = new ArrayList<String>();
        //Get cards in Stack:
        ResultSet db_Stack = connector.createStatement()
                .executeQuery(String.format("SELECT * FROM \"Stack\" WHERE username = '%s';",username));
        while(db_Stack.next())
            doesOwn.add(db_Stack.getString("cardname"));
        cards.removeIf(card -> doesOwn.contains(card.getCardname()));
        cards.forEach(card -> {
            try {

                connector.createStatement()
                        .executeUpdate(String.format("INSERT INTO \"Stack\" (username,cardname)\n" +
                                "VALUES ('%s','%s');",username,card.getCardname()));
            } catch (SQLException e) {
                try {
                    if(rollback.get())updateCoins(username,amount*-1);
                    rollback.set(false);
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
                e.printStackTrace();

            }
        });
    }

    public String createCardsBody(List<Card> cards) throws JsonProcessingException {
        if(cards == null || cards.isEmpty()) return "{}";
        String finalBody = "{";
        int i = 0;
        for (Card card:cards) {
            finalBody += String.format("\n\t\"%d\":%s",i, Json_form.stringify(Json_form.toJson(card)));
            finalBody += cards.size()-1 == i?"\n}":",";
            i++;
        }
        return finalBody;
    }

    public int countDeck(String username) throws SQLException {
        ResultSet count_cards_in_dec = connector.createStatement().
                executeQuery(String.format("SELECT COUNT(*) FROM \"Stack\" " +
                        "WHERE username = '%s' AND in_deck = true;", username));
        count_cards_in_dec.next();
        return count_cards_in_dec.getInt(1);
    }
}
