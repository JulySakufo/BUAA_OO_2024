import com.oocourse.spec3.exceptions.PersonIdNotFoundException;

import java.util.HashMap;

public class MyPersonIdNotFoundException extends PersonIdNotFoundException {
    private final int id;
    private static int count = 0; //此类异常发生的总次数
    private static final HashMap<Integer, Integer> countMap = new HashMap<>();
    
    public MyPersonIdNotFoundException(int id) {
        this.id = id;
        count++;
        if (countMap.containsKey(id)) {
            int value = countMap.get(id) + 1;
            countMap.remove(id);
            countMap.put(id, value);
        } else {
            countMap.put(id, 1);
        }
    }
    
    @Override
    public void print() {
        System.out.println(String.format("pinf-%d, %d-%d", count, id, countMap.get(id)));
    }
}
