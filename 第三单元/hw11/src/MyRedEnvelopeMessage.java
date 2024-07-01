import com.oocourse.spec3.main.Person;
import com.oocourse.spec3.main.RedEnvelopeMessage;
import com.oocourse.spec3.main.Tag;

public class MyRedEnvelopeMessage extends MyMessage implements RedEnvelopeMessage {
    private int money;
    
    public MyRedEnvelopeMessage(int messageId, int luckyMoney, Person person1, Person person2) {
        super(messageId, luckyMoney * 5, person1, person2);
        this.money = luckyMoney;
    }
    
    public MyRedEnvelopeMessage(int messageId, int luckyMoney, Person person1, Tag messageTag) {
        super(messageId, luckyMoney * 5, person1, messageTag);
        this.money = luckyMoney;
    }
    
    @Override
    public int getMoney() {
        return money;
    }
}
