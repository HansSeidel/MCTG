

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.*;

import bif3.swe.if20b211.server.User;
import bif3.swe.if20b211.server.Card;
import bif3.swe.if20b211.server.ECardType;

import java.lang.reflect.Array;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ServerUserUnitTests {

    private final String ex_name = "Max";
    private final Card[] ex_stack = {
            new Card("goblin", 12, ECardType.MONSTER,20),
            new Card("rat", 3, ECardType.MONSTER,90),
            new Card("dragon", 45, ECardType.MONSTER,3),
            new Card("giant_spider", 15, ECardType.MONSTER,24),
            new Card("firestrike", 10, ECardType.SPELL,80)};

    @BeforeAll
    void setup() { }

    @Test
    void testUserInitialisation(){
        //Testobject
        User user;

        //act
        user = new User("Max","ganzgeheim1");

        //assert
        assertEquals(ex_name,user.getUsername());
    }

    @Test
    void testStack(){
        //arrange
        User user = new User("Max","ganzgeheim1");
        Card[] cards = {
                new Card("goblin", 12, ECardType.MONSTER,20),
                new Card("rat", 3, ECardType.MONSTER,90)};
        Card extra = new Card("dragon", 45, ECardType.MONSTER,3);


        //act
        user.setStack(cards);
        user.addCardToStack(extra);
        user.addCardToStack(new Card("giant_spider", 15, ECardType.MONSTER,24), new Card("firestrike", 10, ECardType.SPELL,80));

        //assert
        assertEquals(ex_stack,user.getStack());
    }

}
