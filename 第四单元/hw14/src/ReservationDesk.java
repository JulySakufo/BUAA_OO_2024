import com.oocourse.library2.LibraryBookId;

import java.util.ArrayList;
import java.util.HashMap;

public class ReservationDesk {
    private HashMap<String, ArrayList<LibraryBookId>> requests;//当天收到的请求，在闭馆整理后要根据请求更新keepBooks
    private HashMap<String, ArrayList<Book>> keepBooks; //studentId-它预约的书(已经在这儿了)
    private HashMap<LibraryBookId, Integer> outDateBooks; //预约过期的书
    
    public ReservationDesk() {
        this.requests = new HashMap<>();
        this.keepBooks = new HashMap<>();
        this.outDateBooks = new HashMap<>();
    }
    
    public void orderedBook(String studentId, LibraryBookId libraryBookId) {
        if (!requests.containsKey(studentId)) {
            ArrayList<LibraryBookId> arrayList = new ArrayList<>();
            arrayList.add(libraryBookId);
            requests.put(studentId, arrayList);
        } else {
            ArrayList<LibraryBookId> arrayList = requests.get(studentId);
            arrayList.add(libraryBookId);
        }
    }
    
    public boolean checkCanPicked(LibraryBookId libraryBookId, Student student) { //检查student能否借书
        if (libraryBookId.isTypeB()) {
            return !student.getHasTypeB(); //如果student有一本B类书，不能借，否则可借
        } else {
            HashMap<LibraryBookId, Book> studentBooks = student.getBorrowedBooks();
            return !studentBooks.containsKey(libraryBookId); //如果有这本书的副本，不能借
        }
    }
    
    public HashMap<LibraryBookId, Integer> getOutDateBooks() {
        return outDateBooks;
    }
    
    public HashMap<String, ArrayList<LibraryBookId>> getRequests() {
        return requests;
    }
    
    public HashMap<String, ArrayList<Book>> getKeepBooks() {
        return keepBooks;
    }
}
