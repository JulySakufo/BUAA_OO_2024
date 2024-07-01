import com.oocourse.library1.LibraryBookId;

import java.util.HashMap;

public class Student {
    private String id;
    private HashMap<LibraryBookId, Integer> borrowedBooks;
    private boolean hasTypeB;
    
    public Student(String id) {
        this.id = id;
        this.borrowedBooks = new HashMap<>();
        this.hasTypeB = false;
    }
    
    public void borrowBook(LibraryBookId libraryBookId) {
        if (libraryBookId.isTypeB()) {
            hasTypeB = true;
            borrowedBooks.put(libraryBookId, 1);
        } else {
            borrowedBooks.put(libraryBookId, 1);
        }
    }
    
    public void returnBook(LibraryBookId libraryBookId) {
        borrowedBooks.remove(libraryBookId); //因为同一书号的书无论什么类型都只有一本，直接remove即可
        if (libraryBookId.isTypeB()) { //不再拥有B类书
            hasTypeB = false;
        }
    }
    
    public String getId() {
        return id;
    }
    
    public HashMap<LibraryBookId, Integer> getBorrowedBooks() {
        return borrowedBooks;
    }
    
    public boolean getHasTypeB() {
        return hasTypeB;
    }
}
