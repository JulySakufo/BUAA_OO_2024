import com.oocourse.spec2.exceptions.AcquaintanceNotFoundException;
import com.oocourse.spec2.exceptions.EqualPersonIdException;
import com.oocourse.spec2.exceptions.EqualRelationException;
import com.oocourse.spec2.exceptions.EqualTagIdException;
import com.oocourse.spec2.exceptions.PathNotFoundException;
import com.oocourse.spec2.exceptions.PersonIdNotFoundException;
import com.oocourse.spec2.exceptions.RelationNotFoundException;
import com.oocourse.spec2.exceptions.TagIdNotFoundException;
import com.oocourse.spec2.main.Network;
import com.oocourse.spec2.main.Person;
import com.oocourse.spec2.main.Tag;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;
import java.util.LinkedList;
import java.util.ArrayList;

public class MyNetwork implements Network {
    private final HashMap<Integer, MyPerson> peopleMap; //看这个网络总的有哪些人
    private final HashMap<Integer, Integer> rootMap;
    private final HashMap<Integer, Integer> rankMap;
    private static final HashMap<Integer, HashSet<MyTag>> tagMap = new HashMap<>(); //id在其他人的tag里面
    private int tripleSum;
    private int blockSum;
    private int coupleSum;
    private boolean coupleDirty; //coupleSum的脏位，判断是否修改
    
    public MyNetwork() {
        this.peopleMap = new HashMap<>();
        this.rootMap = new HashMap<>();
        this.rankMap = new HashMap<>();
        this.tripleSum = 0;
        this.blockSum = 0;
        this.coupleSum = 0;
        this.coupleDirty = false;
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
            int bestAcquaintance1 = myPerson1.getBestAcquaintance();
            int bestAcquaintance2 = myPerson2.getBestAcquaintance();
            if (bestAcquaintance1 == id2 || bestAcquaintance2 == id1) {
                coupleDirty = true; //coupleSum一定发生了变化,因为1，2关系是新增的，否则没有任何变化
            }
            updateValueSum(id1, id2, value);
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
            int bestAcquaintance1 = myPerson1.getBestAcquaintance();
            int bestAcquaintance2 = myPerson2.getBestAcquaintance(); //未修改前的best属性
            if ((getPerson(id1).queryValue(getPerson(id2)) + value > 0)) {
                myPerson1.modifyRelation(myPerson2, value);
                myPerson2.modifyRelation(myPerson1, value);
                int bestAcquaintance1new = myPerson1.getBestAcquaintance();
                int bestAcquaintance2new = myPerson2.getBestAcquaintance();
                if (bestAcquaintance1new != bestAcquaintance1
                        || bestAcquaintance2new != bestAcquaintance2) {
                    coupleDirty = true; //前后不一致，重建
                }
                updateValueSum(id1, id2, value);
            } else { //互删，先删三元环，再删关系
                tripleSum -= myPerson1.getDegree() < myPerson2.getDegree() ?
                        myPerson1.getTripleSum(myPerson2) : myPerson2.getTripleSum(myPerson1);
                int oldValue = getPerson(id1).queryValue(getPerson(id2));
                updateValueSum(id1, id2, -oldValue); //断绝关系，即把两者value清0，要在del关系之前删掉
                myPerson1.deleteRelation(myPerson2);
                myPerson2.deleteRelation(myPerson1);
                int bestAcquaintance1new = myPerson1.getBestAcquaintance();
                int bestAcquaintance2new = myPerson2.getBestAcquaintance();
                if (bestAcquaintance1new != bestAcquaintance1
                        || bestAcquaintance2new != bestAcquaintance2) {
                    coupleDirty = true; //前后不一致就重建，因为只修改了这两个人的关系
                }
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
    
    @Override
    public void addTag(int personId, Tag tag) throws
            PersonIdNotFoundException, EqualTagIdException { //personId这个人进指定的tag群
        if (containsPerson(personId) && !getPerson(personId).containsTag(tag.getId())) {
            getPerson(personId).addTag(tag);
        } else if (!containsPerson(personId)) {
            throw new MyPersonIdNotFoundException(personId);
        } else if (containsPerson(personId) && getPerson(personId).containsTag(tag.getId())) {
            throw new MyEqualTagIdException(tag.getId());
        }
    }
    
    @Override
    public void addPersonToTag(int personId1, int personId2, int tagId) throws
            PersonIdNotFoundException, RelationNotFoundException,
            TagIdNotFoundException, EqualPersonIdException {
        if (containsPerson(personId1) && containsPerson(personId2) && personId1 != personId2
                && getPerson(personId2).isLinked(getPerson(personId1))
                && getPerson(personId2).containsTag(tagId)
                && !getPerson(personId2).getTag(tagId).hasPerson(getPerson(personId1))
                && getPerson(personId2).getTag(tagId).getSize() <= 1111) {
            getPerson(personId2).getTag(tagId).addPerson(getPerson(personId1));
            if (tagMap.containsKey(personId1)) { //加入到personId2的对应tagId的tag
                HashSet<MyTag> hashSet = tagMap.get(personId1);
                hashSet.add((MyTag) (getPerson(personId2).getTag(tagId)));
            } else {
                HashSet<MyTag> hashSet = new HashSet<>();
                hashSet.add((MyTag) (getPerson(personId2).getTag(tagId)));
                tagMap.put(personId1, hashSet);
            } //将personId1所在的tag封装成hashSet
        } else if (!containsPerson(personId1)) {
            throw new MyPersonIdNotFoundException(personId1);
        } else if (!containsPerson(personId2)) {
            throw new MyPersonIdNotFoundException(personId2);
        } else if (personId1 == personId2) {
            throw new MyEqualPersonIdException(personId1);
        } else if (!getPerson(personId2).isLinked(getPerson(personId1))) {
            throw new MyRelationNotFoundException(personId1, personId2);
        } else if (!getPerson(personId2).containsTag(tagId)) {
            throw new MyTagIdNotFoundException(tagId);
        } else if (getPerson(personId2).getTag(tagId).hasPerson(getPerson(personId1))) {
            throw new MyEqualPersonIdException(personId1);
        }
    } //标签里的人数有最大上限1111
    
    @Override
    public int queryTagValueSum(int personId, int tagId) throws
            PersonIdNotFoundException, TagIdNotFoundException {
        if (containsPerson(personId) && getPerson(personId).containsTag(tagId)) {
            return getPerson(personId).getTag(tagId).getValueSum();
        } else if (!containsPerson(personId)) {
            throw new MyPersonIdNotFoundException(personId);
        } else {
            throw new MyTagIdNotFoundException(tagId);
        }
    }
    
    @Override
    public int queryTagAgeVar(int personId, int tagId) throws
            PersonIdNotFoundException, TagIdNotFoundException {
        if (containsPerson(personId) && getPerson(personId).containsTag(tagId)) {
            return getPerson(personId).getTag(tagId).getAgeVar();
        } else if (!containsPerson(personId)) {
            throw new MyPersonIdNotFoundException(personId);
        } else {
            throw new MyTagIdNotFoundException(tagId);
        }
    }
    
    @Override
    public void delPersonFromTag(int personId1, int personId2, int tagId) throws
            PersonIdNotFoundException, TagIdNotFoundException {
        if (containsPerson(personId1) && containsPerson(personId2)
                && getPerson(personId2).containsTag(tagId)
                && getPerson(personId2).getTag(tagId).hasPerson(getPerson(personId1))) {
            getPerson(personId2).getTag(tagId).delPerson(getPerson(personId1));
            HashSet<MyTag> hashSet = tagMap.get(personId1); //无需判空，因为一定有
            hashSet.remove((MyTag) (getPerson(personId2).getTag(tagId)));
            if (hashSet.isEmpty()) {
                tagMap.remove(personId1);
            } //如果删去了唯一的tag，直接清0就行
        } else if (!containsPerson(personId1)) {
            throw new MyPersonIdNotFoundException(personId1);
        } else if (!containsPerson(personId2)) {
            throw new MyPersonIdNotFoundException(personId2);
        } else if (!getPerson(personId2).containsTag(tagId)) {
            throw new MyTagIdNotFoundException(tagId);
        } else if (!getPerson(personId2).getTag(tagId).hasPerson(getPerson(personId1))) {
            throw new MyPersonIdNotFoundException(personId1);
        }
    }
    
    @Override
    public void delTag(int personId, int tagId) throws
            PersonIdNotFoundException, TagIdNotFoundException {
        if (containsPerson(personId) && getPerson(personId).containsTag(tagId)) {
            getPerson(personId).delTag(tagId);
        } else if (!containsPerson(personId)) {
            throw new MyPersonIdNotFoundException(personId);
        } else if (!getPerson(personId).containsTag(tagId)) {
            throw new MyTagIdNotFoundException(tagId);
        }
    }
    
    @Override
    public int queryBestAcquaintance(int id) throws
            PersonIdNotFoundException, AcquaintanceNotFoundException {
        if (containsPerson(id) && !(((MyPerson) getPerson(id)).getAcquaintance().isEmpty())) {
            return ((MyPerson) getPerson(id)).getBestAcquaintance();
        } else if (!containsPerson(id)) {
            throw new MyPersonIdNotFoundException(id);
        } else {
            throw new MyAcquaintanceNotFoundException(id);
        }
    }
    
    @Override
    public int queryCoupleSum() { //要么重建，要么直接返回.o(n),o(1)
        if (!coupleDirty) {
            return coupleSum;
        }
        coupleSum = 0;
        HashSet<MyPerson> visited = new HashSet<>();
        for (MyPerson myPerson : peopleMap.values()) {
            if (!visited.contains(myPerson)) {
                HashMap<Integer, MyPerson> acquaintance = myPerson.getAcquaintance();
                if (acquaintance.isEmpty()) { //还没有诞生best属性
                    continue;
                } //1有2，2必有1，因此不必判断bestPerson的acquaintance是否为空了
                MyPerson bestPerson = acquaintance.get(myPerson.getBestAcquaintance());
                if (bestPerson.getBestAcquaintance() == myPerson.getId()) {
                    coupleSum++;
                    visited.add(myPerson);
                    visited.add(bestPerson);
                } else {
                    visited.add(myPerson);
                }
            }
        } //重建完成
        coupleDirty = false;
        return coupleSum;
    }
    
    @Override
    public int queryShortestPath(int id1, int id2) throws
            PersonIdNotFoundException, PathNotFoundException {
        if (isCircle(id1, id2)) {
            MyPerson src = (MyPerson) getPerson(id1);
            MyPerson dst = (MyPerson) getPerson(id2);
            return bfs(src, dst);
        } else if (!containsPerson(id1)) {
            throw new MyPersonIdNotFoundException(id1);
        } else if (!containsPerson(id2)) {
            throw new MyPersonIdNotFoundException(id2);
        } else {
            throw new MyPathNotFoundException(id1, id2);
        }
    }
    
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
    
    public int bfs(MyPerson src, MyPerson dst) {
        if (src.getId() == dst.getId() || src.getAcquaintance().containsKey(dst.getId())) {
            return 0;
        } //没有中间点的特殊情况考虑
        Queue<MyPerson> headQueue = new LinkedList<>();
        Queue<MyPerson> tailQueue = new LinkedList<>();
        Queue<MyPerson> headCache = new LinkedList<>();
        Queue<MyPerson> tailCache = new LinkedList<>();
        final HashSet<MyPerson> headVisited = new HashSet<>();
        final HashSet<MyPerson> tailVisited = new HashSet<>();
        int headDistance = 0;
        int tailDistance = 0;
        headQueue.offer(src);
        tailQueue.offer(dst);
        headVisited.add(src);
        tailVisited.add(dst);
        while (true) {
            headQueue.addAll(headCache);
            tailQueue.addAll(tailCache);
            headCache.clear();
            tailCache.clear();
            headDistance++; //src向下一层
            while (!headQueue.isEmpty()) {
                MyPerson headPerson = headQueue.remove();
                HashMap<Integer, MyPerson> headAcquaintance = headPerson.getAcquaintance();
                for (MyPerson myPerson : headAcquaintance.values()) {
                    if (headVisited.contains(myPerson)) { //跳过上层已经遍历过的点
                        continue;
                    }
                    headCache.add(myPerson); //加入到缓存里面，尾部遍历一级后再加入
                    if (tailVisited.contains(myPerson)) { //如果下层有这个点
                        return headDistance + tailDistance - 1; //求的是经过的节点数
                    }
                    headVisited.add(myPerson); //否则上层加一个visit
                }
            }
            tailDistance++;
            while (!tailQueue.isEmpty()) {
                MyPerson tailPerson = tailQueue.remove();
                HashMap<Integer, MyPerson> tailAcquaintance = tailPerson.getAcquaintance();
                for (MyPerson myPerson : tailAcquaintance.values()) {
                    if (tailVisited.contains(myPerson)) {
                        continue;
                    }
                    tailCache.add(myPerson);
                    if (headVisited.contains(myPerson)) {
                        return headDistance + tailDistance - 1;
                    }
                    tailVisited.add(myPerson);
                }
            }
        }
    }
    
    public void updateValueSum(int id1, int id2, int value) {
        if (tagMap.containsKey(id1) && tagMap.containsKey(id2)) { //首先都要保证被加入到了tag
            HashSet<MyTag> hashSet1 = tagMap.get(id1);
            HashSet<MyTag> hashSet2 = tagMap.get(id2);
            HashSet<MyTag> hashSet = hashSet1.size() < hashSet2.size() ? hashSet1 : hashSet2;
            int yourId = hashSet1.size() < hashSet2.size() ? id2 : id1;
            for (MyTag myTag : hashSet) { //做优化，遍历数量少的tag
                if (myTag.hasPerson(getPerson(yourId))) { //2个人在同一个tag，更新value
                    myTag.updateValueSum(value);
                }
            }
        }
    }
    
    public static HashSet<MyTag> getTagHashSet(int id) {
        return tagMap.get(id);
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
