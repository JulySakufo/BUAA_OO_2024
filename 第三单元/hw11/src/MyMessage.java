import com.oocourse.spec3.main.Message;
import com.oocourse.spec3.main.Person;
import com.oocourse.spec3.main.Tag;

public class MyMessage implements Message {
    private int id;
    private int socialValue;
    private int type;
    private MyPerson myPerson1;
    private MyPerson myPerson2;
    private MyTag myTag;
    
    public MyMessage(int messageId, int messageSocialValue, Person person1, Person person2) {
        this.id = messageId;
        this.socialValue = messageSocialValue;
        this.myPerson1 = (MyPerson) person1;
        this.myPerson2 = (MyPerson) person2;
        this.type = 0;
        this.myTag = null;
    }
    
    public MyMessage(int messageId, int messageSocialValue, Person messagePerson1, Tag messageTag) {
        this.id = messageId;
        this.socialValue = messageSocialValue;
        this.myPerson1 = (MyPerson) messagePerson1;
        this.myTag = (MyTag) messageTag;
        this.type = 1;
        this.myPerson2 = null;
    }
    
    @Override
    public int getType() {
        return this.type;
    }
    
    @Override
    public int getId() {
        return this.id;
    }
    
    @Override
    public int getSocialValue() {
        return this.socialValue;
    }
    
    @Override
    public Person getPerson1() {
        return this.myPerson1;
    }
    
    @Override
    public Person getPerson2() {
        return this.myPerson2;
    }
    
    @Override
    public Tag getTag() {
        return this.myTag;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof MyMessage)) {
            return false;
        }
        return ((MyMessage) obj).getId() == this.id;
    }
}
