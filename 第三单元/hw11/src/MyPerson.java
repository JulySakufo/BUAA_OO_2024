import com.oocourse.spec3.main.Message;
import com.oocourse.spec3.main.Person;
import com.oocourse.spec3.main.Tag;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;

public class MyPerson implements Person {
    private final int id; //不可能重复
    private final String name;
    private final int age;
    private final HashMap<Integer, MyPerson> acquaintance;
    private final HashMap<Integer, Integer> value;
    private final HashMap<Integer, MyTag> tags; //这个人一共有几个tag
    private int bestAcquaintance;
    private int bestValue;
    private int socialValue;
    private int money;
    private final ArrayList<Message> messages;
    
    public MyPerson(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.acquaintance = new HashMap<>();
        this.value = new HashMap<>();
        this.tags = new HashMap<>();
        this.bestAcquaintance = 0;
        this.bestValue = Integer.MIN_VALUE;
        this.socialValue = 0;
        this.money = 0;
        this.messages = new ArrayList<>();
    }
    
    @Override
    public int getId() {
        return this.id;
    }
    
    @Override
    public String getName() {
        return this.name;
    }
    
    @Override
    public int getAge() {
        return this.age;
    }
    
    @Override
    public boolean containsTag(int id) {
        return tags.containsKey(id);
    }
    
    @Override
    public Tag getTag(int id) {
        return containsTag(id) ? tags.get(id) : null;
    }
    
    public int getBestAcquaintance() {
        return this.bestAcquaintance;
    }
    
    @Override
    public void addTag(Tag tag) {
        if (!containsTag(tag.getId())) {
            tags.put(tag.getId(), (MyTag) tag);
        }
    }
    
    @Override
    public void delTag(int id) {
        if (containsTag(id)) {
            tags.remove(id);
        }
    }
    
    @Override
    public boolean isLinked(Person person) {
        if (person.getId() == this.id) {
            return true;
        } else {
            return acquaintance.containsKey(person.getId());
        }
    } //如果person是acquaintance中的一个或者person是实例本身，就说明person与该实例有关系，即link
    
    @Override
    public int queryValue(Person person) {
        int id = person.getId();
        if (acquaintance.containsKey(id)) {
            return value.get(id);
        } else {
            return 0;
        }
    } //如果link，那么返回该person的value，否则直接返回0
    
    @Override
    public void addSocialValue(int num) {
        socialValue = socialValue + num;
    }
    
    @Override
    public int getSocialValue() {
        return socialValue;
    }
    
    @Override
    public List<Message> getMessages() {
        return messages;
    }
    
    @Override
    public List<Message> getReceivedMessages() {
        ArrayList<Message> arrayList = new ArrayList<>();
        if (messages.size() > 4) {
            for (int i = 0; i <= 4; i++) {
                arrayList.add(messages.get(i));
            }
            return arrayList;
        } else {
            return messages;
        }
    }
    
    public void addMessage(MyMessage myMessage) {
        messages.add(0, myMessage); //插入到头部
    }
    
    @Override
    public void addMoney(int num) {
        money = money + num;
    }
    
    @Override
    public int getMoney() {
        return money;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof MyPerson)) {
            return false;
        }
        return ((MyPerson) obj).getId() == this.id;
    }
    
    public void addRelation(MyPerson person, int val) { //与person认识了，建立了val的亲密度
        int id = person.getId();
        if (val > bestValue) { //动态维护person的best属性
            bestAcquaintance = id;
            bestValue = val;
        } else if (val == bestValue) {
            bestAcquaintance = Math.min(id, bestAcquaintance);
        }
        acquaintance.put(id, person);
        value.put(id, val);
    }
    
    public void modifyRelation(MyPerson person, int val) {
        int values = value.get(person.getId()) + val;
        value.remove(person.getId());
        value.put(person.getId(), values); //先移再寻
        if (person.getId() == bestAcquaintance) { //原来这个人是bestId
            bestAcquaintance = 0;
            bestValue = Integer.MIN_VALUE;
            for (Integer key : acquaintance.keySet()) {
                int newVal = value.get(key);
                if (newVal > bestValue) {
                    bestAcquaintance = key;
                    bestValue = newVal;
                } else if (newVal == bestValue) {
                    bestAcquaintance = Math.min(key, bestAcquaintance);
                }
            }
        } else { //原来这个人不是bestId
            if (values > bestValue) {
                bestAcquaintance = person.getId();
                bestValue = values;
            } else if (values == bestValue) {
                bestAcquaintance = Math.min(person.getId(), bestAcquaintance);
            }
        }
    }
    
    public void deleteRelation(MyPerson person) {
        int id = person.getId();
        acquaintance.remove(id);
        value.remove(id); //先移再寻
        if (id == bestAcquaintance) { //删掉的是bestAcquaintance，需要重新找一个best属性，复杂度是O(n)
            bestAcquaintance = 0;
            bestValue = Integer.MIN_VALUE;
            for (Integer key : acquaintance.keySet()) {
                int val = value.get(key);
                if (val > bestValue) {
                    bestAcquaintance = key;
                    bestValue = val;
                } else if (val == bestValue) {
                    bestAcquaintance = Math.min(key, bestAcquaintance);
                }
            }
        }
        for (Integer tag : tags.keySet()) { //从分组中删去这个人
            MyTag myTag = tags.get(tag);
            myTag.delPerson(person);
            HashSet<MyTag> hashSet = MyNetwork.getTagHashSet(person.getId());
            if (hashSet != null) {
                hashSet.remove(tags.get(tag));
            }
        }
    }
    
    public int getTripleSum(MyPerson myPerson) {
        int res = 0;
        int personId = myPerson.getId();
        for (Integer key : acquaintance.keySet()) { //看看有无公共好友
            if (myPerson.isLinked(acquaintance.get(key)) && personId != key) {
                res++;
            }
        }
        return res;
    }
    
    public int getDegree() {
        return acquaintance.size();
    }
    
    public HashMap<Integer, MyPerson> getAcquaintance() {
        return this.acquaintance;
    }
    
    //声明方法即可，内容任意，仅仅是为了通过评测时的编译，不会被调用
    public boolean strictEquals(Person person) {
        return this.id == person.getId();
    }
}
