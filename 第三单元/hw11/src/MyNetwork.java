import com.oocourse.spec3.exceptions.AcquaintanceNotFoundException;
import com.oocourse.spec3.exceptions.EmojiIdNotFoundException;
import com.oocourse.spec3.exceptions.EqualEmojiIdException;
import com.oocourse.spec3.exceptions.EqualMessageIdException;
import com.oocourse.spec3.exceptions.EqualPersonIdException;
import com.oocourse.spec3.exceptions.EqualRelationException;
import com.oocourse.spec3.exceptions.EqualTagIdException;
import com.oocourse.spec3.exceptions.MessageIdNotFoundException;
import com.oocourse.spec3.exceptions.PathNotFoundException;
import com.oocourse.spec3.exceptions.PersonIdNotFoundException;
import com.oocourse.spec3.exceptions.RelationNotFoundException;
import com.oocourse.spec3.exceptions.TagIdNotFoundException;
import com.oocourse.spec3.main.Message;
import com.oocourse.spec3.main.Network;
import com.oocourse.spec3.main.Person;
import com.oocourse.spec3.main.Tag;

import java.util.*;

public class MyNetwork implements Network {
    private static final HashMap<Integer, MyPerson> peopleMap = new HashMap<>(); //看这个网络总的有哪些人
    private static final HashMap<Integer, Integer> rootMap = new HashMap<>();
    private static final HashMap<Integer, Integer> rankMap = new HashMap<>();
    private static final HashMap<Integer, HashSet<MyTag>> tagMap = new HashMap<>(); //id在其他人的tag里面
    private static final HashMap<Integer, MyMessage> messageMap = new HashMap<>();
    private static final HashMap<Integer, Integer> emojiMap = new HashMap<>();
    private int tripleSum;
    private static int blockSum = 0;
    private int coupleSum;
    private static boolean coupleDirty = false; //coupleSum的脏位，判断是否修改
    
    public MyNetwork() {
        this.tripleSum = 0;
        this.coupleSum = 0;
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
            MySolution.union(id1, id2, rootMap, rankMap);
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
                MySolution.rebuild(id1, id2, peopleMap, rootMap, rankMap);
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
            return MySolution.find(id1, rootMap) == MySolution.find(id2, rootMap); //根节点是否相同判断连通性
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
            return MySolution.bfs(src, dst);
        } else if (!containsPerson(id1)) {
            throw new MyPersonIdNotFoundException(id1);
        } else if (!containsPerson(id2)) {
            throw new MyPersonIdNotFoundException(id2);
        } else {
            throw new MyPathNotFoundException(id1, id2);
        }
    }
    
    @Override
    public boolean containsMessage(int id) {
        return messageMap.containsKey(id);
    }
    
    @Override
    public void addMessage(Message message) throws EqualMessageIdException,
            EmojiIdNotFoundException, EqualPersonIdException {
        if (containsMessage(message.getId())) {
            throw new MyEqualMessageIdException(message.getId());
        } else if ((message instanceof MyEmojiMessage)
                && !containsEmojiId(((MyEmojiMessage) message).getEmojiId())) {
            throw new MyEmojiIdNotFoundException(((MyEmojiMessage) message).getEmojiId());
        } else if (message.getType() == 0 && message.getPerson1().equals(message.getPerson2())) {
            throw new MyEqualPersonIdException(message.getPerson1().getId());
        } else {
            messageMap.put(message.getId(), (MyMessage) message);
        }
    }
    
    @Override
    public Message getMessage(int id) {
        return containsMessage(id) ? messageMap.get(id) : null;
    }
    
    @Override
    public void sendMessage(int id) throws RelationNotFoundException,
            MessageIdNotFoundException, TagIdNotFoundException {
        if (!containsMessage(id)) {
            throw new MyMessageIdNotFoundException(id);
        } else if (getMessage(id).getType() == 0
                && !(getMessage(id).getPerson1().isLinked(getMessage(id).getPerson2()))) {
            throw new MyRelationNotFoundException(getMessage(id).getPerson1().getId(),
                    getMessage(id).getPerson2().getId());
        } else if (getMessage(id).getType() == 1
                && !getMessage(id).getPerson1().containsTag(getMessage(id).getTag().getId())) {
            throw new MyTagIdNotFoundException(getMessage(id).getTag().getId());
        } else if (containsMessage(id) && getMessage(id).getType() == 0
                && getMessage(id).getPerson1().isLinked(getMessage(id).getPerson2())
                && getMessage(id).getPerson1() != getMessage(id).getPerson2()) {
            MySolution.sendMessage1(id);
        } else if (containsMessage(id) && getMessage(id).getType() == 1
                && getMessage(id).getPerson1().containsTag(getMessage(id).getTag().getId())) {
            MySolution.sendMessage2(id);
        } //向tag群发消息
    }
    
    @Override
    public int querySocialValue(int id) throws PersonIdNotFoundException {
        if (containsPerson(id)) {
            return getPerson(id).getSocialValue();
        } else {
            throw new MyPersonIdNotFoundException(id);
        }
    }
    
    @Override
    public List<Message> queryReceivedMessages(int id) throws PersonIdNotFoundException {
        if (containsPerson(id)) {
            return getPerson(id).getReceivedMessages();
        } else {
            throw new MyPersonIdNotFoundException(id);
        }
    }
    
    @Override
    public boolean containsEmojiId(int id) {
        return emojiMap.containsKey(id);
    }
    
    @Override
    public void storeEmojiId(int id) throws EqualEmojiIdException {
        if (!containsEmojiId(id)) {
            emojiMap.put(id, 0);
        } else {
            throw new MyEqualEmojiIdException(id);
        }
    }
    
    @Override
    public int queryMoney(int id) throws PersonIdNotFoundException {
        if (containsPerson(id)) {
            return getPerson(id).getMoney();
        }
        throw new MyPersonIdNotFoundException(id);
    }
    
    @Override
    public int queryPopularity(int id) throws EmojiIdNotFoundException {
        if (containsEmojiId(id)) {
            return emojiMap.get(id);
        } else {
            throw new MyEmojiIdNotFoundException(id);
        }
    }
    
    @Override
    public int deleteColdEmoji(int limit) { /*TODO:是否还有优化空间*/ //O(n)操作
        messageMap.entrySet().removeIf(entry -> entry.getValue() instanceof MyEmojiMessage
                && emojiMap.get(((MyEmojiMessage) entry.getValue()).getEmojiId()) < limit);
        emojiMap.entrySet().removeIf(entry -> entry.getValue() < limit);
        return emojiMap.size();
    }
    
    @Override
    public void clearNotices(int personId) throws PersonIdNotFoundException {
        if (containsPerson(personId)) {
            List<Message> list = (getPerson(personId).getMessages());
            Iterator<Message> iterator = list.iterator();
            while (iterator.hasNext()) {
                Message message = iterator.next();
                if (message instanceof MyNoticeMessage) {
                    iterator.remove();
                }
            }
        } else {
            throw new MyPersonIdNotFoundException(personId);
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
    
    public static HashMap<Integer, MyMessage> getMessageMap() {
        return messageMap;
    }
    
    public static HashMap<Integer, Integer> getEmojiMap() {
        return emojiMap;
    }
    
    public static void setCoupleDirty(boolean flag) {
        coupleDirty = flag;
    }
    
    public static void updateBlockSum(int num) {
        blockSum = blockSum + num;
    }
    
    //声明方法即可，内容任意，仅仅是为了通过评测时的编译，不会被调用
    public Message[] getMessages() {
        ArrayList<Message> arrayList = new ArrayList<>();
        for (Integer key : messageMap.keySet()) {
            Message message = messageMap.get(key);
            arrayList.add(message);
        }
        Message[] messages = new Message[arrayList.size()];
        return arrayList.toArray(messages);
    }
    
    public int[] getEmojiIdList() {
        int size = 0;
        int[] emojiIdList = new int[emojiMap.size()];
        for (Integer key : emojiMap.keySet()) {
            emojiIdList[size++] = key;
        }
        return emojiIdList;
    }
    
    public int[] getEmojiHeatList() {
        int size = 0;
        int[] emojiHeatList = new int[emojiMap.size()];
        for (Integer val : emojiMap.values()) {
            emojiHeatList[size++] = val;
        }
        return emojiHeatList;
    }
}
