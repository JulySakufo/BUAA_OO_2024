import com.oocourse.library3.LibraryBookId;

import java.util.HashMap;

public class BorrowReturnDesk {
    private HashMap<LibraryBookId, Integer> borrowReturnBooks;
    
    public BorrowReturnDesk() {
        this.borrowReturnBooks = new HashMap<>();
    }
    
    public boolean checkCanBorrowed(LibraryBookId libraryBookId, Student student) { //借还处做了所有能否借书的处理
        if (student.getCreditScore() < 0) { //用户积分为负，不能借书
            return false;
        }
        if (libraryBookId.isTypeB()) {
            return !student.getHasTypeB(); //如果student有一本B类书，不能借，否则可借
        } else if (libraryBookId.isTypeBU()) {
            return !student.getHasTypeBU(); //如果student有一本BU类书，不能借，否则可借
        } else {
            HashMap<LibraryBookId, Book> studentBooks = student.getBorrowedBooks();
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
