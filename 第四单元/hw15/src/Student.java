import com.oocourse.library3.LibraryBookId;

import java.util.HashMap;
import java.util.HashSet;

public class Student {
    private String id;
    private HashMap<LibraryBookId, Book> borrowedBooks; //student对于每一本书都只能最多拥有一本，所以用book表示即可。
    private HashSet<LibraryBookId> orderedBooks;
    private boolean hasTypeB;
    private boolean hasTypeBU;
    private boolean orderTypeB;
    private int creditScore;
    
    public Student(String id) {
        this.id = id;
        this.borrowedBooks = new HashMap<>();
        this.orderedBooks = new HashSet<>();
        this.hasTypeB = false;
        this.hasTypeBU = false;
        this.orderTypeB = false;
        this.creditScore = 10;
    }
    
    public void borrowBook(LibraryBookId libraryBookId, Book book) {
        if (libraryBookId.isTypeB()) {
            hasTypeB = true;
            borrowedBooks.put(libraryBookId, book);
        } else if (libraryBookId.isTypeBU()) {
            hasTypeBU = true;
            borrowedBooks.put(libraryBookId, book);
        } else {
            borrowedBooks.put(libraryBookId, book);
        }
    }
    
    public Book returnBook(LibraryBookId libraryBookId) {
        Book book = borrowedBooks.get(libraryBookId); //因为同一书号的书无论什么类型都只有一本，直接remove即可
        borrowedBooks.remove(libraryBookId);
        if (libraryBookId.isTypeB()) { //不再拥有B类书
            hasTypeB = false;
        } else if (libraryBookId.isTypeBU()) { //不再拥有BU类书
            hasTypeBU = false;
        }
        return book; //对逾期的判断需要借助book的start属性，因此返回book
    }
    
    public Book getBook(LibraryBookId libraryBookId) {
        return borrowedBooks.get(libraryBookId);
    }
    
    public String getId() {
        return id;
    }
    
    public HashMap<LibraryBookId, Book> getBorrowedBooks() {
        return borrowedBooks;
    }
    
    public boolean getHasTypeB() {
        return hasTypeB;
    }
    
    public boolean getHasTypeBU() {
        return hasTypeBU;
    }
    
    public int getCreditScore() {
        return creditScore;
    }
    
    public boolean getOrderTypeB() {
        return orderTypeB;
    }
    
    public HashSet<LibraryBookId> getOrderedBooks() {
        return orderedBooks;
    }
    
    public void addCreditScore(int creditScore) {
        this.creditScore = Math.min(this.creditScore + creditScore, 20);
    }
    
    public void setOrderTypeB(boolean orderTypeB) {
        this.orderTypeB = orderTypeB;
    }
}
