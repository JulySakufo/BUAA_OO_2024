import com.oocourse.spec3.main.Person;
import com.oocourse.spec3.main.Tag;

import java.util.HashMap;

public class MyTag implements Tag {
    private final int id;
    private final HashMap<Integer, MyPerson> groupMap; //微信分组
    private int ageSum;
    private int ageSquareSum;
    private int valueSum;
    
    public MyTag(int id) {
        this.id = id;
        this.groupMap = new HashMap<>();
        this.ageSum = 0;
        this.ageSquareSum = 0;
        this.valueSum = 0;
    }
    
    @Override
    public void addPerson(Person person) { //动态维护
        if (!hasPerson(person)) {
            for (Integer key : groupMap.keySet()) {
                MyPerson myPerson = groupMap.get(key);
                valueSum = valueSum + 2 * myPerson.queryValue(person);
            }
            groupMap.put(person.getId(), (MyPerson) person);
            ageSum = ageSum + person.getAge();
            ageSquareSum = ageSquareSum + person.getAge() * person.getAge();
        }
    }
    
    @Override
    public boolean hasPerson(Person person) {
        return groupMap.containsKey(person.getId());
    }
    
    @Override
    public void delPerson(Person person) { //动态维护
        if (hasPerson(person)) {
            groupMap.remove(person.getId());
            for (Integer key : groupMap.keySet()) {
                MyPerson myPerson = groupMap.get(key);
                valueSum = valueSum - 2 * myPerson.queryValue(person);
            }
            ageSum = ageSum - person.getAge();
            ageSquareSum = ageSquareSum - person.getAge() * person.getAge();
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Tag)) {
            return false;
        }
        return ((Tag) obj).getId() == this.id;
    }
    
    @Override
    public int getId() {
        return this.id;
    }
    
    @Override
    public int getValueSum() {
        return valueSum; //动态维护
    }
    
    @Override
    public int getAgeMean() {
        return groupMap.isEmpty() ? 0 : ageSum / groupMap.size();
    }
    
    @Override
    public int getAgeVar() {
        if (groupMap.isEmpty()) {
            return 0;
        } else {
            int v1 = ageSquareSum - 2 * ageSum * getAgeMean();
            int v2 = getAgeMean() * getAgeMean() * getSize();
            return (v1 + v2) / getSize();
        }
    }
    
    @Override
    public int getSize() {
        return groupMap.size();
    }
    
    public void updateValueSum(int value) { //传正负值进来
        valueSum = valueSum + 2 * value;
    }
    
    public void addSocialValue(int num) {
        for (MyPerson myPerson : groupMap.values()) {
            myPerson.addSocialValue(num);
        }
    }
    
    public void addMoney(int num) {
        for (MyPerson myPerson : groupMap.values()) {
            myPerson.addMoney(num);
        }
    }
}
