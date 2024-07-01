import com.oocourse.library2.LibraryBookId;

import java.util.HashMap;

public class DriftCorner {
    private HashMap<LibraryBookId, Integer> driftBooks;
    private HashMap<LibraryBookId, Integer> counts; //漂流图书借阅次数的记录
    
    public DriftCorner() {
        this.driftBooks = new HashMap<>();
        this.counts = new HashMap<>();
    }
    
    public int queryBook(LibraryBookId libraryBookId) {
        return driftBooks.get(libraryBookId);
    }
    
    public void receiveBook(LibraryBookId libraryBookId) {
        if (!driftBooks.containsKey(libraryBookId)) { //是第一次捐献的图书
            driftBooks.put(libraryBookId, 1);
            counts.put(libraryBookId, 0); //初始化这本书的被借阅次数
        } else {
            int value = driftBooks.get(libraryBookId);
            driftBooks.put(libraryBookId, value + 1);
        }
    }
    
    public void isBorrowed(LibraryBookId libraryBookId) {
        int value = driftBooks.get(libraryBookId);
        value--;
        driftBooks.put(libraryBookId, value);
    }
    
    public void addBookCount(LibraryBookId libraryBookId) { //在returnBook的时候实现增加符合题意
        int count = counts.get(libraryBookId);
        count++;
        counts.put(libraryBookId, count);
    }
    
    public int queryCount(LibraryBookId libraryBookId) {
        return counts.get(libraryBookId);
    }
}
