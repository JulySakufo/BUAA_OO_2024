import com.oocourse.spec3.exceptions.EqualMessageIdException;

import java.util.HashMap;

public class MyEqualMessageIdException extends EqualMessageIdException {
    private final int id;
    private static int count = 0;
    private static final HashMap<Integer, Integer> countMap = new HashMap<>();
    
    public MyEqualMessageIdException(int id) {
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
        System.out.println(String.format("emi-%d, %d-%d", count, id, countMap.get(id)));
    }
}
