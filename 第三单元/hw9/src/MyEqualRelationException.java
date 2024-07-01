import com.oocourse.spec1.exceptions.EqualRelationException;

import java.util.HashMap;

public class MyEqualRelationException extends EqualRelationException {
    private final int id1;
    private final int id2;
    private static int count;
    private static final HashMap<Integer, Integer> countMap = new HashMap<>();
    
    public MyEqualRelationException(int id1, int id2) {
        this.id1 = Math.min(id1, id2);
        this.id2 = Math.max(id1, id2);
        count++;
        if (id1 == id2) {
            if (countMap.containsKey(id1)) {
                int value = countMap.get(id1) + 1;
                countMap.remove(id1);
                countMap.put(id1, value);
            } else {
                countMap.put(id1, 1);
            }
        } else {
            if (countMap.containsKey(id1)) {
                int value = countMap.get(id1) + 1;
                countMap.remove(id1);
                countMap.put(id1, value);
            } else {
                countMap.put(id1, 1);
            }
            if (countMap.containsKey(id2)) {
                int value = countMap.get(id2) + 1;
                countMap.remove(id2);
                countMap.put(id2, value);
            } else {
                countMap.put(id2, 1);
            }
        }
    }
    
    @Override
    public void print() {
        int v1 = countMap.get(id1);
        int v2 = countMap.get(id2);
        System.out.println(String.format("er-%d, %d-%d, %d-%d", count, id1, v1, id2, v2));
    }
}
