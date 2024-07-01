import java.util.ArrayList;

public class MySet implements IntSet {
    private ArrayList<Integer> arr;
    private int count;
    
    public MySet() {
        arr = new ArrayList<>();
        count = 0;
    }
    
    @Override
    public Boolean contains(int x) { //看arr是否含有x这个数
        for (int i = 0; i < arr.size(); i++) {
            if (arr.get(i) == x) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public int getNum(int x) throws IndexOutOfBoundsException { //得到arr的第x个数
        if (x >= 0 && x < count) {
            return arr.get(x);
        } else {
            throw new IndexOutOfBoundsException();
        }
    }
    
    @Override
    public void insert(int x) { //二分插入
        int left = 0;
        int right = count - 1;
        int pos = -1;
        while (left <= right) {
            int mid = (left + right) / 2;
            if (arr.get(mid) >= x) {
                pos = mid;
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }
        if (pos == -1) {
            count++;
            arr.add(x);
        } else {
            arr.add(pos, x);
            count++;
        }
    }
    
    @Override
    public void delete(int x) {
        int left = 0;
        int right = count - 1;
        int pos = -1;
        while (left <= right) {
            int mid = (left + right) / 2;
            if (arr.get(mid) >= x) {
                pos = mid;
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }
        if (pos != -1 && arr.get(pos) == x) {
            arr.remove(pos);
            count--;
        }
    }
    
    @Override
    public int size() {
        return count;
    }
    
    @Override
    public void elementSwap(IntSet a) {
        //TODO
        IntSet temp = new MySet();
        for (Integer key : arr) {
            temp.insert(key);
        } //创建一个中间变量
        arr.clear();
        for (int i = 0; i < a.size(); i++) {
            arr.add(a.getNum(i));
        }
        for (Integer key : arr) {
            a.delete(key); //清空a这个set
        }
        for (int i = 0; i < temp.size(); i++) {
            a.insert(temp.getNum(i));
        }
    }
    
    @Override
    public IntSet symmetricDifference(IntSet a) throws NullPointerException {
        //TODO
        if (a == null) {
            throw new NullPointerException();
        } else {
            IntSet resultSet = new MySet();
            for (Integer key : arr) {
                if (!a.contains(key)) {
                    resultSet.insert(key);
                }
            }
            for (int i = 0; i < a.size(); i++) {
                if (!arr.contains(a.getNum(i))) {
                    resultSet.insert(a.getNum(i));
                }
            }
            return resultSet;
        }
    }
    
    @Override
    public boolean repOK() {
        //TODO
        for (int i = 0; i < arr.size() - 1; i++) {
            if (arr.get(i) >= arr.get(i + 1)) {
                return false;
            }
        }
        return true;
    }
}
