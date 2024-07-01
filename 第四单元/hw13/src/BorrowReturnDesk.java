import com.oocourse.library1.LibraryBookId;

import java.util.HashMap;

public class BorrowReturnDesk {
    private HashMap<LibraryBookId, Integer> borrowReturnBooks;
    
    public BorrowReturnDesk() {
        this.borrowReturnBooks = new HashMap<>();
    }
    
    public boolean checkCanBorrowed(LibraryBookId libraryBookId, Student student) { //检查student能否借书
        if (libraryBookId.isTypeB()) {
            return !student.getHasTypeB(); //如果student有一本B类书，不能借，否则可借
        } else {
            HashMap<LibraryBookId, Integer> studentBooks = student.getBorrowedBooks();
            return !studentBooks.containsKey(libraryBookId); //如果有这本书的副本，不能借
        }
    }
    
    public void receiveBook(LibraryBookId libraryBookId) {
        if (!borrowReturnBooks.containsKey(libraryBookId)) {
            borrowReturnBooks.put(libraryBookId, 1);
        } else {
            int count = borrowReturnBooks.get(libraryBookId);
            count++;
            borrowReturnBooks.put(libraryBookId, count);
        }
    }
    
    public void sendBook(LibraryBookId libraryBookId) {
        int count = borrowReturnBooks.get(libraryBookId);
        count--;
        borrowReturnBooks.put(libraryBookId, count);
    }
    
    public HashMap<LibraryBookId, Integer> getBorrowReturnBooks() {
        return borrowReturnBooks;
    }
}
