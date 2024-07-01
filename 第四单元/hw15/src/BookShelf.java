import com.oocourse.library3.LibraryBookId;

import java.util.HashMap;
import java.util.Map;

public class BookShelf {
    private HashMap<LibraryBookId, Integer> books;
    
    public BookShelf(Map<LibraryBookId, Integer> books) {
        this.books = new HashMap<>(books);
    }
    
    public int queryBook(LibraryBookId libraryBookId) {
        return books.get(libraryBookId);
    }
    
    public void isBorrowed(LibraryBookId libraryBookId) {
        int count = books.get(libraryBookId);
        count--;
        books.put(libraryBookId, count);
    }
    
    public void receiveBook(LibraryBookId libraryBookId) {
        int count = books.get(libraryBookId);
        count++;
        books.put(libraryBookId, count);
    }
    
    public void addNewBook(LibraryBookId libraryBookId) {
        books.put(libraryBookId, 1);
    }
}
