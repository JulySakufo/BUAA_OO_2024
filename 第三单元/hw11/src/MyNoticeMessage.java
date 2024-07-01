import com.oocourse.spec3.main.NoticeMessage;
import com.oocourse.spec3.main.Person;
import com.oocourse.spec3.main.Tag;

public class MyNoticeMessage extends MyMessage implements NoticeMessage {
    private String string;
    
    public MyNoticeMessage(int messageId, String noticeString, Person person1, Person person2) {
        super(messageId, noticeString.length(), person1, person2);
        this.string = noticeString;
    }
    
    public MyNoticeMessage(int messageId, String noticeString, Person person1, Tag messageTag) {
        super(messageId, noticeString.length(), person1, messageTag);
        this.string = noticeString;
    }
    
    @Override
    public String getString() {
        return string;
    }
}
