import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class MySolution {
    public static int bfs(MyPerson src, MyPerson dst) {
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
    
    public static void sendMessage1(int id) {
        HashMap<Integer, MyMessage> messageMap = MyNetwork.getMessageMap();
        HashMap<Integer, Integer> emojiMap = MyNetwork.getEmojiMap();
        MyMessage myMessage = messageMap.get(id);
        messageMap.remove(id); //先取后删
        MyPerson myPerson1 = (MyPerson) (myMessage.getPerson1());
        MyPerson myPerson2 = (MyPerson) (myMessage.getPerson2());
        myPerson1.addSocialValue(myMessage.getSocialValue());
        myPerson2.addSocialValue(myMessage.getSocialValue());
        if (myMessage instanceof MyRedEnvelopeMessage) {
            myPerson1.addMoney(-((MyRedEnvelopeMessage) myMessage).getMoney());
            myPerson2.addMoney(((MyRedEnvelopeMessage) myMessage).getMoney());
        } else if (myMessage instanceof MyEmojiMessage) {
            int value = emojiMap.get(((MyEmojiMessage) myMessage).getEmojiId());
            value++;
            emojiMap.put(((MyEmojiMessage) myMessage).getEmojiId(), value);
        } //向person2单独发消息
        myPerson2.addMessage(myMessage);
    }
    
    public static void sendMessage2(int id) {
        HashMap<Integer, MyMessage> messageMap = MyNetwork.getMessageMap();
        HashMap<Integer, Integer> emojiMap = MyNetwork.getEmojiMap();
        MyMessage myMessage = messageMap.get(id);
        messageMap.remove(id);
        MyPerson myPerson1 = (MyPerson) (myMessage.getPerson1());
        myPerson1.addSocialValue(myMessage.getSocialValue());
        MyTag myTag = (MyTag) (myMessage.getTag());
        myTag.addSocialValue(myMessage.getSocialValue());
        if (myMessage instanceof MyRedEnvelopeMessage && myTag.getSize() > 0) {
            int size = myTag.getSize();
            int i = ((MyRedEnvelopeMessage) myMessage).getMoney() / size;
            myPerson1.addMoney(-i * size);
            myTag.addMoney(i);
        } else if (myMessage instanceof MyEmojiMessage) {
            int value = emojiMap.get(((MyEmojiMessage) myMessage).getEmojiId());
            value++;
            emojiMap.put(((MyEmojiMessage) myMessage).getEmojiId(), value);
        }
    }
    
    public static void dfs(int id, HashSet<Integer> hashSet, HashMap<Integer, MyPerson> peopleMap) {
        hashSet.add(id);
        for (Integer key : peopleMap.get(id).getAcquaintance().keySet()) {
            if (!hashSet.contains(key)) {
                dfs(key, hashSet, peopleMap);
            }
        }
    }
    
    public static int find(int x, HashMap<Integer, Integer> rootMap) {
        if (rootMap.get(x) == x) {
            return x;
        }
        rootMap.put(x, find(rootMap.get(x), rootMap));
        return rootMap.get(x);
    }
    
    public static void union(int x, int y, HashMap<Integer, Integer> rootMap,
                             HashMap<Integer, Integer> rankMap) {
        int rootX = MySolution.find(x, rootMap);
        int rootY = MySolution.find(y, rootMap);
        if (rootX != rootY) {
            MyNetwork.updateBlockSum(-1);//两个孤岛联合成一个了,块的数量自然减少
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
    
    public static void rebuild(int id1, int id2, HashMap<Integer, MyPerson> peopleMap,
                               HashMap<Integer, Integer> rootMap,
                               HashMap<Integer, Integer> rankMap) {
        HashSet<Integer> hashSet = new HashSet<>();
        dfs(id1, hashSet, peopleMap);//找到和id1在同一个图的所有点
        if (!hashSet.contains(id2)) { //其他都跟id2没关系，id2成为了脱离id1的孤岛
            HashSet<Integer> myHashSet = new HashSet<>();
            dfs(id2, myHashSet, peopleMap); //找到和id2有关系的所有点，把它们都指向id2,秩无所谓随便设置就行
            for (Integer key : myHashSet) {
                rootMap.put(key, id2);
                rankMap.put(key, 1);
            }
            rankMap.put(id2, 2);
            MyNetwork.updateBlockSum(1); //新增块
        }
        for (Integer key : hashSet) { //全部以id1为根，秩全转为1
            rootMap.put(key, id1);
            rankMap.put(key, 1);
        }
        rankMap.put(id1, 2); //以id1为根，id1的高度是最高的
    }
}
