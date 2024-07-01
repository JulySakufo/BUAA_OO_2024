import com.oocourse.spec1.main.Person;

import java.util.HashMap;

public class MyPerson implements Person {
    private final int id; //不可能重复
    private final String name;
    private final int age;
    private final HashMap<Integer, MyPerson> acquaintance;
    private final HashMap<Integer, Integer> value;
    
    public MyPerson(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.acquaintance = new HashMap<>();
        this.value = new HashMap<>();
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
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof MyPerson)) {
            return false;
        }
        return ((MyPerson) obj).getId() == this.id;
    }
    
    public void addRelation(MyPerson person, int val) { //与person认识了，建立了val的亲密度
        int id = person.getId();
        acquaintance.put(id, person);
        value.put(id, val);
    }
    
    public void modifyRelation(MyPerson person, int val) {
        int values = value.get(person.getId()) + val;
        value.remove(person.getId());
        value.put(person.getId(), values);
    }
    
    public void deleteRelation(MyPerson person) {
        int id = person.getId();
        acquaintance.remove(id);
        value.remove(id);
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
