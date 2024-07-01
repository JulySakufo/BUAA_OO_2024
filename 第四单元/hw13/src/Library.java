import com.oocourse.library1.LibrarySystem;
import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryCommand;
import com.oocourse.library1.LibraryRequest;
import com.oocourse.library1.LibraryMoveInfo;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Library {
    private BookShelf bookShelf;
    private ReservationDesk reservationDesk;
    private BorrowReturnDesk borrowReturnDesk;
    private HashMap<String, Student> students; //有哪些学生,防止new一个已有的student
    
    public Library(BookShelf bs, ReservationDesk rd, BorrowReturnDesk brd) {
        this.bookShelf = bs;
        this.reservationDesk = rd;
        this.borrowReturnDesk = brd;
        this.students = new HashMap<>();
    }
    
    public void queryBook(LibraryCommand<?> command) {
        LibraryRequest request = (LibraryRequest) command.getCmd();
        LibraryBookId libraryBookId = request.getBookId();
        int count = bookShelf.queryBook(libraryBookId);
        LibrarySystem.PRINTER.info(command, count);
    }
    
    public void borrowBook(LibraryCommand<?> command) {
        LibraryRequest request = (LibraryRequest) command.getCmd();
        LibraryBookId libraryBookId = request.getBookId();
        String studentId = request.getStudentId();
        int count = bookShelf.queryBook(libraryBookId);
        if (count == 0 || request.getBookId().isTypeA()) { //书架上无余本或者要借的是A类书，借阅失败
            LibrarySystem.PRINTER.reject(command);
        } else {
            if (!students.containsKey(studentId)) {
                Student student = new Student(studentId);
                students.put(studentId, student);
                checkCanBorrowed(libraryBookId, student, command); //职责分离
            } else {
                Student student = students.get(studentId);
                checkCanBorrowed(libraryBookId, student, command);
            }
        }
    }
    
    public void returnBook(LibraryCommand<?> command) { //学生还书，借还处收到还的这本书
        LibraryRequest request = (LibraryRequest) command.getCmd();
        LibraryBookId libraryBookId = request.getBookId();
        String studentId = request.getStudentId();
        Student student = students.get(studentId);
        student.returnBook(libraryBookId); //封装处理，各干各的事
        borrowReturnDesk.receiveBook(libraryBookId);
        LibrarySystem.PRINTER.accept(command);
    }
    
    public void orderBook(LibraryCommand<?> command) {
        LibraryRequest request = (LibraryRequest) command.getCmd();
        LibraryBookId libraryBookId = request.getBookId();
        String studentId = request.getStudentId();
        if (!students.containsKey(studentId)) { //之前没来过，但是想预约书，先new一个对象，然后再进行预约行为
            students.put(studentId, new Student(studentId));
        }
        Student student = students.get(studentId);
        HashMap<LibraryBookId, Integer> studentBooks = student.getBorrowedBooks();
        if (libraryBookId.isTypeA() || (student.getHasTypeB() && libraryBookId.isTypeB())
                || (studentBooks.containsKey(libraryBookId) && libraryBookId.isTypeC())) {
            LibrarySystem.PRINTER.reject(command);
        } else { //满足预约要求，直接开始预约
            reservationDesk.orderedBook(studentId, libraryBookId);
            LibrarySystem.PRINTER.accept(command);
        }
    }
    
    public void pickBook(LibraryCommand<?> command) {
        LibraryRequest request = (LibraryRequest) command.getCmd();
        LibraryBookId libraryBookId = request.getBookId();
        String studentId = request.getStudentId();
        Student student = students.get(studentId);
        checkCanPicked(libraryBookId, student, command);
    }
    
    public void moveBook(LibraryCommand<?> command) { //borrowReturnDesk->bookShelf->reservationDesk
        HashMap<LibraryBookId, Integer> brBooks = borrowReturnDesk.getBorrowReturnBooks();
        HashMap<LibraryBookId, Integer> outDateBooks = reservationDesk.getOutDateBooks();
        final HashMap<String, ArrayList<LibraryBookId>> requests = reservationDesk.getRequests();
        HashMap<String, ArrayList<Book>> keepBooks = reservationDesk.getKeepBooks(); //放在预约处的保留的书
        ArrayList<LibraryMoveInfo> print = new ArrayList<>(); //先存起来最后再打印
        borrowReturnDesk2BookShelf(brBooks, print); //从借还处搬回书架
        for (String string : keepBooks.keySet()) { //移除预约处keepBooks已经逾期的书，将它们全部放进outDateBooks里面，准备移动
            ArrayList<Book> arrayList = keepBooks.get(string);
            Iterator<Book> iterator = arrayList.iterator();
            while (iterator.hasNext()) {
                Book book = iterator.next();
                if (ChronoUnit.DAYS.between(book.getStart(), command.getDate()) >= 5) { //开馆后整理
                    if (outDateBooks.containsKey(book.getLibraryBookId())) {
                        int newValue = outDateBooks.get(book.getLibraryBookId()) + 1;
                        outDateBooks.put(book.getLibraryBookId(), newValue);
                    } else {
                        outDateBooks.put(book.getLibraryBookId(), 1);
                    }
                    iterator.remove();
                }
            }
        }
        for (LibraryBookId libraryBookId : outDateBooks.keySet()) { //将过期的书搬回书架
            int value = outDateBooks.get(libraryBookId);
            for (int i = 0; i < value; i++) {
                bookShelf.receiveBook(libraryBookId);
                LibraryMoveInfo libraryMoveInfo = new LibraryMoveInfo(libraryBookId, "ao", "bs");
                print.add(libraryMoveInfo);
            }
        }
        outDateBooks.clear(); //清空map
        for (String string : requests.keySet()) { //从书架上取书
            ArrayList<LibraryBookId> libraryBookIds = requests.get(string);
            Iterator<LibraryBookId> iterator = libraryBookIds.iterator();
            while (iterator.hasNext()) {
                LibraryBookId libraryBookId = iterator.next();
                if (bookShelf.queryBook(libraryBookId) != 0) { //响应预约请求
                    bookShelf.isBorrowed(libraryBookId); //减少书架上的数量
                    Book book = new Book(libraryBookId, string, command.getDate()); //为这个student而留
                    if (!keepBooks.containsKey(string)) { //将书架上的书拿到预约处，保证这里都是没有过期的书
                        ArrayList<Book> arrayList = new ArrayList<>();
                        arrayList.add(book);
                        keepBooks.put(string, arrayList);
                    } else {
                        ArrayList<Book> arrayList = keepBooks.get(string);
                        arrayList.add(book); //天然按时间顺序往后排列
                    }
                    LibraryMoveInfo libraryMoveInfo = new LibraryMoveInfo(libraryBookId,
                            "bs", "ao", string);
                    print.add(libraryMoveInfo);
                    iterator.remove(); //完成预约请求，已经将书送到预约处，去掉requests，使得只保留未响应的预约请求
                }
            }
        }
        LibrarySystem.PRINTER.move(command.getDate(), print);
    }
    
    public void checkCanBorrowed(LibraryBookId libraryBookId, Student stu, LibraryCommand<?> cmd) {
        bookShelf.isBorrowed(libraryBookId); //从书架上取书，送到借还处
        borrowReturnDesk.receiveBook(libraryBookId); //从书架上收到书，开始检查
        if (borrowReturnDesk.checkCanBorrowed(libraryBookId, stu)) {
            borrowReturnDesk.sendBook(libraryBookId); //借还处将书交给student
            stu.borrowBook(libraryBookId);
            LibrarySystem.PRINTER.accept(cmd);
        } else {
            LibrarySystem.PRINTER.reject(cmd);
        }
    }
    
    public void checkCanPicked(LibraryBookId libraryBookId, Student stu, LibraryCommand<?> cmd) {
        HashMap<String, ArrayList<Book>> keepBooks = reservationDesk.getKeepBooks();
        if (!keepBooks.containsKey(stu.getId())) { //没有为这个人保留的书
            LibrarySystem.PRINTER.reject(cmd);
        } else {
            ArrayList<Book> arrayList = keepBooks.get(stu.getId());
            for (Book book : arrayList) {
                if (book.getLibraryBookId().equals(libraryBookId)) { //有这本书，做出相应成功或失败打印信息后结束循环
                    if (reservationDesk.checkCanPicked(libraryBookId, stu)) { //可以取书
                        arrayList.remove(book);
                        stu.borrowBook(libraryBookId); //取书还是相当于借书行为
                        LibrarySystem.PRINTER.accept(cmd);
                    } else {
                        LibrarySystem.PRINTER.reject(cmd);
                    }
                    return;
                }
            }
            LibrarySystem.PRINTER.reject(cmd); //为他保留的书中没有这一本，打印失败信息，结束
        }
    }
    
    public void borrowReturnDesk2BookShelf(HashMap<LibraryBookId, Integer> brBooks,
                                           ArrayList<LibraryMoveInfo> print) {
        for (LibraryBookId libraryBookId : brBooks.keySet()) {
            int value = brBooks.get(libraryBookId);
            for (int i = 0; i < value; i++) {
                bookShelf.receiveBook(libraryBookId); //书架获得从借还处的书
                LibraryMoveInfo libraryMoveInfo = new LibraryMoveInfo(libraryBookId, "bro", "bs");
                print.add(libraryMoveInfo);
            }
        }
        brBooks.clear(); //都从借还处搬回书架了,清空map
    }
}
