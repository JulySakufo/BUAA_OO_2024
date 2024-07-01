import com.oocourse.spec3.exceptions.AcquaintanceNotFoundException;

import java.util.HashMap;

public class MyAcquaintanceNotFoundException extends AcquaintanceNotFoundException {
    private final int id;
    private static int count = 0;
    private static final HashMap<Integer, Integer> countMap = new HashMap<>();
    
    public MyAcquaintanceNotFoundException(int id) {
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
        System.out.println(String.format("anf-%d, %d-%d", count, id, countMap.get(id)));
    }
}
