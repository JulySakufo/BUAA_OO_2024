import com.oocourse.spec1.exceptions.EqualPersonIdException;
import com.oocourse.spec1.exceptions.EqualRelationException;
import com.oocourse.spec1.exceptions.PersonIdNotFoundException;
import com.oocourse.spec1.exceptions.RelationNotFoundException;
import com.oocourse.spec1.main.Network;
import com.oocourse.spec1.main.Person;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class MyNetwork implements Network {
    private final HashMap<Integer, MyPerson> peopleMap; //看这个网络总的有哪些人
    private final HashMap<Integer, Integer> rootMap;
    private final HashMap<Integer, Integer> rankMap;
    private int tripleSum;
    private int blockSum;
    
    public MyNetwork() {
        this.peopleMap = new HashMap<>();
        this.rootMap = new HashMap<>();
        this.rankMap = new HashMap<>();
        this.tripleSum = 0;
        this.blockSum = 0;
    }
    
    @Override
    public boolean containsPerson(int id) { //network中是否有这个id的person
        return peopleMap.containsKey(id);
    }
    
    @Override
    public Person getPerson(int id) { //得到对应id的myPerson，如果contains返回结果为false，return null
        if (containsPerson(id)) {
            return peopleMap.get(id);
        } else {
            return null;
        }
    }
    
    @Override
    public void addPerson(Person person) throws EqualPersonIdException {
        if (!containsPerson(person.getId())) {
            int id = person.getId();
            peopleMap.put(id, (MyPerson) person);
            blockSum++; //新增一个结点，是一座孤岛
            rootMap.put(id, id);
            rankMap.put(id, 1); //并查集中也要新增结点
        } else {
            throw new MyEqualPersonIdException(person.getId());
        }
    } //如果myNetwork已有这个person，抛出对应异常，否则将该person加入到myNetwork中
    
    @Override
    public void addRelation(int id1, int id2, int value) throws
            PersonIdNotFoundException, EqualRelationException {
        if (containsPerson(id1) && containsPerson(id2)
                && !getPerson(id1).isLinked(getPerson(id2))) {
            MyPerson myPerson1 = (MyPerson) getPerson(id1);
            MyPerson myPerson2 = (MyPerson) getPerson(id2);
            myPerson1.addRelation(myPerson2, value);
            myPerson2.addRelation(myPerson1, value);
            tripleSum += myPerson1.getDegree() < myPerson2.getDegree() ?
                    myPerson1.getTripleSum(myPerson2) : myPerson2.getTripleSum(myPerson1);
            union(id1, id2); //加边，即联合
        } else if (!containsPerson(id1)) {
            throw new MyPersonIdNotFoundException(id1);
        } else if (!containsPerson(id2)) {
            throw new MyPersonIdNotFoundException(id2);
        } else if (getPerson(id1).isLinked(getPerson(id2))) {
            throw new MyEqualRelationException(id1, id2);
        }
    } //id1和id2的person之间亲密度为value。可以理解value就是连接两个人的亲密度。只有id1，id2都存在，并且之前未建立过亲密度才能成功，否则抛出异常
    
    @Override
    public void modifyRelation(int id1, int id2, int value) throws
            PersonIdNotFoundException, EqualPersonIdException, RelationNotFoundException {
        if (containsPerson(id1) && containsPerson(id2) && id1 != id2
                && getPerson(id1).isLinked(getPerson(id2))) {
            MyPerson myPerson1 = (MyPerson) getPerson(id1);
            MyPerson myPerson2 = (MyPerson) getPerson(id2);
            if ((getPerson(id1).queryValue(getPerson(id2)) + value > 0)) {
                myPerson1.modifyRelation(myPerson2, value);
                myPerson2.modifyRelation(myPerson1, value);
            } else { //互删，先删三元环，再删关系
                tripleSum -= myPerson1.getDegree() < myPerson2.getDegree() ?
                        myPerson1.getTripleSum(myPerson2) : myPerson2.getTripleSum(myPerson1);
                myPerson1.deleteRelation(myPerson2);
                myPerson2.deleteRelation(myPerson1);
                HashSet<Integer> hashSet = new HashSet<>();
                dfs(id1, hashSet);//找到和id1在同一个图的所有点
                if (!hashSet.contains(id2)) { //其他都跟id2没关系，id2成为了脱离id1的孤岛
                    HashSet<Integer> myHashSet = new HashSet<>();
                    dfs(id2, myHashSet); //找到和id2有关系的所有点，把它们都指向id2,秩无所谓随便设置就行
                    for (Integer key : myHashSet) {
                        rootMap.put(key, id2);
                        rankMap.put(key, 1);
                    }
                    rankMap.put(id2, 2);
                    blockSum++; //新增块
                }
                for (Integer key : hashSet) { //全部以id1为根，秩全转为1
                    rootMap.put(key, id1);
                    rankMap.put(key, 1);
                }
                rankMap.put(id1, 2); //以id1为根，id1的高度是最高的
            }
        } else if (!containsPerson(id1)) {
            throw new MyPersonIdNotFoundException(id1);
        } else if (!containsPerson(id2)) {
            throw new MyPersonIdNotFoundException(id2);
        } else if (id1 == id2) {
            throw new MyEqualPersonIdException(id1);
        } else if (!getPerson(id1).isLinked(getPerson(id2))) {
            throw new MyRelationNotFoundException(id1, id2);
        }
    } //原来的value加上新的value如果大于0，则修改为新和。如果小于等于0，则解除acquaintance关系，即两个人删除了认识关系,其余抛出对应异常
    
    @Override
    public int queryValue(int id1, int id2) throws
            PersonIdNotFoundException, RelationNotFoundException {
        if (containsPerson(id1) && containsPerson(id2) && getPerson(id1).isLinked(getPerson(id2))) {
            return getPerson(id1).queryValue(getPerson(id2));
        } else if (!containsPerson(id1)) {
            throw new MyPersonIdNotFoundException(id1);
        } else if (!containsPerson(id2)) {
            throw new MyPersonIdNotFoundException(id2);
        } else {
            throw new MyRelationNotFoundException(id1, id2);
        }
    } //成功则递归下降调用person中的queryValue，否则抛出对应异常
    
    @Override
    public boolean isCircle(int id1, int id2) throws PersonIdNotFoundException {
        if (containsPerson(id1) && containsPerson(id2)) {
            return find(id1) == find(id2); //根节点是否相同判断连通性
        } else if (!containsPerson(id1)) {
            throw new MyPersonIdNotFoundException(id1);
        } else {
            throw new MyPersonIdNotFoundException(id2);
        }
    } //判断两个人是否连通，不满足条件抛出对应异常
    
    @Override
    public int queryBlockSum() {
        return blockSum;
    } //判断有几个连通子图，如果用并查集来说的话就是判断有几个不相交的集合
    
    @Override
    public int queryTripleSum() {
        return tripleSum;
    } //计算三元环的数目
    
    public int find(int x) {
        if (rootMap.get(x) == x) {
            return x;
        }
        rootMap.put(x, find(rootMap.get(x)));
        return rootMap.get(x);
    }
    
    public void union(int x, int y) {
        int rootX = find(x);
        int rootY = find(y);
        if (rootX != rootY) {
            blockSum--;//两个孤岛联合成一个了,块的数量自然减少
            if (rankMap.get(rootX) > rankMap.get(rootY)) {
                rootMap.put(rootY, rootX);
            } else {
                if (rankMap.get(rootX).equals(rankMap.get(rootY))) {
                    rankMap.put(rootY, rankMap.get(rootY) + 1); //高度相等升秩
                }
                rootMap.put(rootX, rootY);
            }
        }
    }
    
    public void dfs(int id, HashSet<Integer> hashSet) { //找到一个图中的所有点
        hashSet.add(id);
        for (Integer key : peopleMap.get(id).getAcquaintance().keySet()) {
            if (!hashSet.contains(key)) {
                dfs(key, hashSet);
            }
        }
    }
    
    //声明方法即可，内容任意，仅仅是为了通过评测时的编译，不会被调用
    public Person[] getPersons() {
        ArrayList<Person> arrayList = new ArrayList<>();
        for (Integer key : peopleMap.keySet()) {
            Person person = peopleMap.get(key);
            arrayList.add(person);
        }
        Person[] persons = new Person[arrayList.size()];
        return arrayList.toArray(persons);
    }
}
