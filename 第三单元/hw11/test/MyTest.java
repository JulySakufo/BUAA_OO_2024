import com.oocourse.spec3.exceptions.*;
import com.oocourse.spec3.main.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;

import static org.junit.Assert.*;

import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.*;

import java.util.*;

@RunWith(Parameterized.class)
public class MyTest {
    private MyNetwork myNetwork;
    private int limit;
    
    public MyTest(MyNetwork myNetwork, int limit) {
        this.myNetwork = myNetwork;
        this.limit = limit;
    }
    
    @Parameters
    public static Collection prepareData() {
        Random random = new Random();
        int testNum = 200;//测试次数,可根据需求调整
        
        //该二维数组的类型必须是Object类型的
        //该二维数组中的第一维代表有多少组测试数据,有多少次测试就会创造多少个PathTest对象
        //该二维数组的第二维代表PathTest构造方法中的参数，位置一一对应
        Object[][] object = new Object[testNum][];
        for (int i = 0; i < testNum; i++) {
            MyNetwork myNetwork = new MyNetwork();
            int limit = random.nextInt(4);
            try {
                myNetwork.addPerson(new MyPerson(1, "1", 1));
            } catch (EqualPersonIdException e) {
            
            }
            try {
                myNetwork.addPerson(new MyPerson(2, "2", 2));
            } catch (EqualPersonIdException e) {
            
            }
            try {
                myNetwork.addPerson(new MyPerson(3, "3", 3));
            } catch (EqualPersonIdException e) {
            
            }
            try {
                myNetwork.addRelation(1, 2, 1);
                myNetwork.addRelation(1, 3, 1);
                myNetwork.addRelation(2, 3, 1);
                MyTag myTag = new MyTag(1);
                myNetwork.addTag(1, myTag);
                myNetwork.addPersonToTag(2, 1, 1);
                myNetwork.addPersonToTag(3, 1, 1);
            } catch (Exception e) {
            
            } //初始化关系
            for (int j = 1; j <= 10; j++) {
                try {
                    myNetwork.storeEmojiId(j);
                } catch (Exception e) {
                
                }
            }
            for (int j = 1; j <= 200; j++) {
                int type = random.nextInt(4) + 1;
                int species = random.nextInt(2);
                if (type == 1) {
                    if (species == 0) {
                        MyEmojiMessage myEmojiMessage = new MyEmojiMessage(j, random.nextInt(10) + 1, myNetwork.getPerson(1), myNetwork.getPerson(random.nextInt(2) + 2));
                        try {
                            myNetwork.addMessage(myEmojiMessage);
                        } catch (Exception e) {
                        
                        }
                    } else {
                        MyEmojiMessage myEmojiMessage = new MyEmojiMessage(j, random.nextInt(10) + 1, myNetwork.getPerson(1), myNetwork.getPerson(1).getTag(1));
                        try {
                            myNetwork.addMessage(myEmojiMessage);
                        } catch (Exception e) {
                        
                        }
                    }
                } else if (type == 2) {
                    if (species == 0) {
                        MyRedEnvelopeMessage myRedEnvelopeMessage = new MyRedEnvelopeMessage(j, j, myNetwork.getPerson(1), myNetwork.getPerson(random.nextInt(2) + 2));
                        try {
                            myNetwork.addMessage(myRedEnvelopeMessage);
                        } catch (Exception e) {
                        
                        }
                    } else {
                        MyRedEnvelopeMessage myRedEnvelopeMessage = new MyRedEnvelopeMessage(j, j, myNetwork.getPerson(1), myNetwork.getPerson(1).getTag(1));
                        try {
                            myNetwork.addMessage(myRedEnvelopeMessage);
                        } catch (Exception e) {
                        
                        }
                    }
                } else if (type == 3) {
                    if (species == 0) {
                        MyNoticeMessage myNoticeMessage = new MyNoticeMessage(j, String.valueOf(j), myNetwork.getPerson(1), myNetwork.getPerson(random.nextInt(2) + 2));
                        try {
                            myNetwork.addMessage(myNoticeMessage);
                        } catch (Exception e) {
                        
                        }
                    } else {
                        MyNoticeMessage myNoticeMessage = new MyNoticeMessage(j, String.valueOf(j), myNetwork.getPerson(1), myNetwork.getPerson(1).getTag(1));
                        try {
                            myNetwork.addMessage(myNoticeMessage);
                        } catch (Exception e) {
                        
                        }
                    }
                } else {
                    if (species == 0) {
                        MyMessage myMessage = new MyMessage(j, j, myNetwork.getPerson(1), myNetwork.getPerson(random.nextInt(2) + 2));
                        try {
                            myNetwork.addMessage(myMessage);
                        } catch (Exception e) {
                        
                        }
                    } else {
                        MyMessage myMessage = new MyMessage(j, j, myNetwork.getPerson(1), myNetwork.getPerson(1).getTag(1));
                        try {
                            myNetwork.addMessage(myMessage);
                        } catch (Exception e) {
                        
                        }
                    }
                }
            }
            object[i] = new Object[]{myNetwork, limit};
        }
        return Arrays.asList(object);
    }
    
    @Test
    public void testDeleteColdEmoji() {
        int mySum = 0;
        int sum = 0;
        int len = 0;
        Message[] qiaMessages = myNetwork.getMessages();
        Message[] messages = new Message[qiaMessages.length];
        int shadow = 0;
        for (int i = 0; i < qiaMessages.length; i++) {
            Message message = qiaMessages[i];
            int msgId = message.getId();
            int type = message.getType();
            int socialValue = message.getSocialValue();
            Tag tag = message.getTag();
            Person person1 = message.getPerson1();
            Person person2 = message.getPerson2();
            if (message instanceof MyEmojiMessage) {
                if (type == 0) {
                    messages[shadow++] = new MyEmojiMessage(msgId, socialValue, person1, person2);
                } else {
                    messages[shadow++] = new MyEmojiMessage(msgId, socialValue, person1, tag);
                }
                
            } else if (message instanceof MyRedEnvelopeMessage) {
                if (type == 0) {
                    messages[shadow++] = new MyRedEnvelopeMessage(msgId, socialValue / 5, person1, person2);
                } else {
                    messages[shadow++] = new MyRedEnvelopeMessage(msgId, socialValue / 5, person1, tag);
                }
            } else if (message instanceof MyNoticeMessage) {
                String string = ((MyNoticeMessage) message).getString();
                if (type == 0) {
                    messages[shadow++] = new MyNoticeMessage(msgId, string, person1, person2);
                } else {
                    messages[shadow++] = new MyNoticeMessage(msgId, string, person1, tag);
                }
            } else if (message instanceof MyMessage) {
                if (type == 0) {
                    messages[shadow++] = new MyMessage(msgId, socialValue, person1, person2);
                } else {
                    messages[shadow++] = new MyMessage(msgId, socialValue, person1, tag);
                }
            }
        }
        int[] emojiIdList = myNetwork.getEmojiIdList();
        int[] emojiHeatList = myNetwork.getEmojiHeatList();
        int[] newEmojiIdList = new int[emojiIdList.length];
        int[] newEmojiHeatList = new int[emojiHeatList.length];
        Message[] newMessages = new Message[messages.length];
        for (int i = 0; i < emojiIdList.length; i++) {
            if (emojiHeatList[i] >= limit) {
                newEmojiIdList[sum] = emojiIdList[i];
                newEmojiHeatList[sum] = emojiHeatList[i];
                sum++;
            }
        }
        for (int i = 0; i < messages.length; i++) {
            if (!(messages[i] instanceof EmojiMessage)) {
                newMessages[len++] = messages[i];
            } else {
                int emojiId = ((EmojiMessage) messages[i]).getEmojiId();
                int index = -1;
                for (int j = 0; j < emojiIdList.length; j++) {
                    if (emojiIdList[j] == emojiId) {
                        index = j;
                        break;
                    }
                }
                if (index != -1 && emojiHeatList[index] >= limit) {
                    newMessages[len++] = messages[i];
                }
            }
        }
        mySum = myNetwork.deleteColdEmoji(limit);
        assertEquals(mySum, sum); //检查返回值是否相同
        Message[] changeMessages = myNetwork.getMessages();
        int[] changeEmojiIdList = myNetwork.getEmojiIdList();
        int[] changeEmojiHeatList = myNetwork.getEmojiHeatList();
        assertEquals(sum, changeEmojiIdList.length);
        assertEquals(sum, changeEmojiHeatList.length);
        assertEquals(len, changeMessages.length); //先判断是否发生变化，如果无变化再看顺序是否一样
        for (int i = 0; i < sum; i++) {
            assertEquals(newEmojiIdList[i], changeEmojiIdList[i]);
            assertEquals(newEmojiHeatList[i], changeEmojiHeatList[i]);
        }
        for (int i = 0; i < len; i++) {
            assertEquals(newMessages[i].getClass(), changeMessages[i].getClass());
            assertEquals(newMessages[i].getId(), changeMessages[i].getId());
            assertEquals(newMessages[i].getType(), changeMessages[i].getType());
            assertEquals(newMessages[i].getSocialValue(), changeMessages[i].getSocialValue());
            assertEquals(newMessages[i].getTag(), changeMessages[i].getTag());
            assertEquals(newMessages[i].getPerson1(), changeMessages[i].getPerson1());
            assertEquals(newMessages[i].getPerson2(), changeMessages[i].getPerson2());
            if (newMessages[i] instanceof MyEmojiMessage) {
                assertEquals(((MyEmojiMessage) (newMessages[i])).getEmojiId(), ((MyEmojiMessage) (changeMessages[i])).getEmojiId());
            } else if (newMessages[i] instanceof MyNoticeMessage) {
                assertEquals(((MyNoticeMessage) newMessages[i]).getString(), ((MyNoticeMessage) (changeMessages[i])).getString());
            } else if (newMessages[i] instanceof MyRedEnvelopeMessage) {
                assertEquals(((MyRedEnvelopeMessage) (newMessages[i])).getMoney(), ((MyRedEnvelopeMessage) (changeMessages[i])).getMoney());
            }
        } //检查改变后的这些容器内容是否相同s
    }
    
}
