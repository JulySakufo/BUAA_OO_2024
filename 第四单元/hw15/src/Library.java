import com.oocourse.library3.LibrarySystem;
import com.oocourse.library3.LibraryQcsCmd;
import com.oocourse.library3.LibraryBookId;
import com.oocourse.library3.LibraryCommand;
import com.oocourse.library3.LibraryMoveInfo;
import com.oocourse.library3.LibraryReqCmd;
import com.oocourse.library3.annotation.Trigger;
import com.oocourse.library3.annotation.SendMessage;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class Library {
    private BookShelf bookShelf;
    private ReservationDesk reservationDesk;
    private BorrowReturnDesk borrowReturnDesk;
    private DriftCorner driftCorner;
    private HashMap<String, Student> students; //有哪些学生,防止new一个已有的student
    private HashMap<LibraryBookId, Integer> checkMap;
    
    //预约bookId的书时，value++，成功pick后value--,如果为0说明当前这本书没有预约请求
    public Library(BookShelf bs, ReservationDesk rd, BorrowReturnDesk brd, DriftCorner dc) {
        this.bookShelf = bs;
        this.reservationDesk = rd;
        this.borrowReturnDesk = brd;
        this.driftCorner = dc;
        this.students = new HashMap<>();
        this.checkMap = new HashMap<>();
    }
    
    public void queryBook(LibraryCommand command) {
        LibraryReqCmd request = (LibraryReqCmd) command;
        LibraryBookId libraryBookId = request.getBookId();
        if (libraryBookId.isFormal()) {
            int count = bookShelf.queryBook(libraryBookId);
            LibrarySystem.PRINTER.info(command, count);
        } else {
            int count = driftCorner.queryBook(libraryBookId);
            LibrarySystem.PRINTER.info(command, count);
        }
    }
    
    @Trigger(from = "BookShelf", to = "BorrowReturnDesk")
    @Trigger(from = "DriftCorner", to = "BorrowReturnDesk")
    @Trigger(from = "BorrowReturnDesk", to = {"BorrowReturnDesk", "Student"})
    public void borrowBook(LibraryCommand command) {
        LibraryReqCmd request = (LibraryReqCmd) command;
        LibraryBookId libraryBookId = request.getBookId();
        String studentId = request.getStudentId();
        if (libraryBookId.isFormal()) {
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
        } else {
            int count = driftCorner.queryBook(libraryBookId);
            if (count == 0 || request.getBookId().isTypeAU()) { //漂流角无余本或者要借的是AU类书，借阅失败
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
    }
    
    @Trigger(from = "Student", to = "BorrowReturnDesk")
    public void returnBook(LibraryCommand command) { //新增借书逾期的判断，因为借书的时候用book封装了，还书的时候输出overdue即可
        LibraryReqCmd request = (LibraryReqCmd) command;
        LibraryBookId libraryBookId = request.getBookId();
        String studentId = request.getStudentId();
        Student student = students.get(studentId);
        Book book = student.returnBook(libraryBookId); //封装处理，各干各的事
        borrowReturnDesk.receiveBook(libraryBookId);
        if (book.getLibraryBookId().isTypeB()) {
            if (ChronoUnit.DAYS.between(book.getBorrowStart(), command.getDate()) <= 30) {
                student.addCreditScore(1); //按时还书分数+1
                LibrarySystem.PRINTER.accept(command, "not overdue");
            } else {
                LibrarySystem.PRINTER.accept(command, "overdue");
            }
        } else if (book.getLibraryBookId().isTypeC()) {
            if (ChronoUnit.DAYS.between(book.getBorrowStart(), command.getDate()) <= 60) {
                student.addCreditScore(1);
                LibrarySystem.PRINTER.accept(command, "not overdue");
            } else {
                LibrarySystem.PRINTER.accept(command, "overdue");
            }
        } else if (book.getLibraryBookId().isTypeBU()) {
            //用户借阅了一本漂流角的书后，需要前往借还处归还，归还成功后，记录该书在漂流角被完整借还一次。
            driftCorner.addBookCount(libraryBookId);
            if (ChronoUnit.DAYS.between(book.getBorrowStart(), command.getDate()) <= 7) {
                student.addCreditScore(1);
                LibrarySystem.PRINTER.accept(command, "not overdue");
            } else {
                LibrarySystem.PRINTER.accept(command, "overdue");
            }
        } else if (book.getLibraryBookId().isTypeCU()) {
            driftCorner.addBookCount(libraryBookId);
            if (ChronoUnit.DAYS.between(book.getBorrowStart(), command.getDate()) <= 14) {
                student.addCreditScore(1);
                LibrarySystem.PRINTER.accept(command, "not overdue");
            } else {
                LibrarySystem.PRINTER.accept(command, "overdue");
            }
        }
    }
    
    @SendMessage(from = "Student", to = "Library")
    public void orderNewBook(LibraryCommand command) {
        LibraryReqCmd request = (LibraryReqCmd) command;
        LibraryBookId libraryBookId = request.getBookId();
        String studentId = request.getStudentId();
        if (!students.containsKey(studentId)) { //之前没来过，但是想预约书，先new一个对象，然后再进行预约行为
            students.put(studentId, new Student(studentId));
        }
        Student student = students.get(studentId);
        HashMap<LibraryBookId, Book> studentBooks = student.getBorrowedBooks();
        if (students.get(studentId).getCreditScore() < 0) {
            LibrarySystem.PRINTER.reject(command);
        } else if (!libraryBookId.isFormal()) { //漂流角书籍不能被预约
            LibrarySystem.PRINTER.reject(command);
        } else if (libraryBookId.isTypeA() || (student.getHasTypeB() && libraryBookId.isTypeB())
                || (studentBooks.containsKey(libraryBookId) && libraryBookId.isTypeC())) {
            LibrarySystem.PRINTER.reject(command);
        } else { //满足预约要求，直接开始预约
            if (reservationDesk.orderedBook(studentId, libraryBookId, student)) {
                if (!checkMap.containsKey(libraryBookId)) {
                    checkMap.put(libraryBookId, 1);
                } else {
                    int count = checkMap.get(libraryBookId);
                    count++;
                    checkMap.put(libraryBookId, count);
                }
                LibrarySystem.PRINTER.accept(command);
            } else {
                LibrarySystem.PRINTER.reject(command);
            }
        }
    }
    
    @SendMessage(from = "Library", to = "Student")
    @Trigger(from = "ReservationDesk", to = {"Student", "ReservationDesk"})
    public void getOrderedBook(LibraryCommand command) {
        LibraryReqCmd request = (LibraryReqCmd) command;
        LibraryBookId libraryBookId = request.getBookId();
        String studentId = request.getStudentId();
        Student student = students.get(studentId);
        checkCanPicked(libraryBookId, student, command);
    }
    
    @Trigger(from = "BorrowReturnDesk", to = {"BookShelf", "DriftCorner"})
    @Trigger(from = "ReservationDesk", to = "BookShelf")
    @Trigger(from = "BookShelf", to = "ReservationDesk")
    public void moveBook(LibraryCommand command) { //borrowReturnDesk->bookShelf->reservationDesk
        HashMap<LibraryBookId, Integer> brBooks = borrowReturnDesk.getBorrowReturnBooks();
        HashMap<LibraryBookId, Integer> outDateBooks = reservationDesk.getOutDateBooks();
        final HashMap<String, ArrayList<LibraryBookId>> requests = reservationDesk.getRequests();
        HashMap<String, ArrayList<Book>> keepBooks = reservationDesk.getKeepBooks(); //放在预约处的保留的书
        ArrayList<LibraryMoveInfo> print = new ArrayList<>(); //先存起来最后再打印
        for (String string : keepBooks.keySet()) { //移除预约处keepBooks已经逾期的书，将它们全部放进outDateBooks里面，准备移动
            ArrayList<Book> arrayList = keepBooks.get(string);
            Iterator<Book> iterator = arrayList.iterator();
            while (iterator.hasNext()) {
                Book book = iterator.next();
                Student student = students.get(book.getStudentId());
                HashSet<LibraryBookId> orderedBooks = student.getOrderedBooks();
                if (ChronoUnit.DAYS.between(book.getReservationStart(), command.getDate()) >= 5) {
                    if (book.getLibraryBookId().isTypeB()) {
                        student.setOrderTypeB(false);
                    }
                    orderedBooks.remove(book.getLibraryBookId()); //该用户一直未取书，该书在预约处逾期时，预约视作不再有效。
                    students.get(book.getStudentId()).addCreditScore(-3); //该用户未在规定日期取走书，扣3分
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
        outDateBooks2BookShelf(outDateBooks, print);
        borrowReturnDesk2Place(brBooks, print); //从借还处将书还到各自该在的地方
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
    
    public void renewBook(LibraryCommand command) {
        LibraryReqCmd request = (LibraryReqCmd) command;
        LibraryBookId libraryBookId = request.getBookId();
        String studentId = request.getStudentId();
        Student student = students.get(studentId);
        Book book = student.getBook(libraryBookId); //指出想要renew的是哪本书
        LocalDate start = book.getBorrowStart();
        long diff = ChronoUnit.DAYS.between(start, command.getDate());
        HashMap<String, ArrayList<LibraryBookId>> requests = reservationDesk.getRequests();
        boolean flag = false;
        if (checkMap.containsKey(libraryBookId)) {
            if (checkMap.get(libraryBookId) != 0) { //当前有对这本书的预约请求
                if (libraryBookId.isFormal() && bookShelf.queryBook(libraryBookId) == 0) {
                    flag = true;
                } else if (!libraryBookId.isFormal() && driftCorner.queryBook(libraryBookId) == 0) {
                    flag = true;
                }
            }
        }
        if (student.getCreditScore() < 0) { //信用积分为负，续借必定失败
            LibrarySystem.PRINTER.reject(command);
            return;
        }
        if (book.getLibraryBookId().isTypeB()) {
            if (diff < 26 || diff > 30 || flag) { //提前或逾期或存在任意一位用户对该书正在生效的预约且该书无在架余本
                LibrarySystem.PRINTER.reject(command);
            } else { //续借成功，该书的借阅期限延长30天，重新设置start即可
                book.setBorrowStart(start.plusDays(30)); //延长30天在我的实现中直接设置start往后30天即可了
                LibrarySystem.PRINTER.accept(command); //即使between是负数也不会影响流程的实现
            }
        } else if (book.getLibraryBookId().isTypeC()) {
            if (diff < 56 || diff > 60 || flag) {
                LibrarySystem.PRINTER.reject(command);
            } else {
                book.setBorrowStart(start.plusDays(30));
                LibrarySystem.PRINTER.accept(command);
            }
        } else if (book.getLibraryBookId().isTypeBU() || book.getLibraryBookId().isTypeCU()) {
            LibrarySystem.PRINTER.reject(command);
        }
    }
    
    @Trigger(from = "InitState", to = "DriftCorner")
    public void donateBook(LibraryCommand command) {
        LibraryReqCmd request = (LibraryReqCmd) command;
        LibraryBookId libraryBookId = request.getBookId();
        String studentId = request.getStudentId();
        if (!students.containsKey(studentId)) { //考虑一上来就捐书的情况
            Student student = new Student(studentId);
            students.put(studentId, student);
        }
        driftCorner.receiveBook(libraryBookId, studentId);
        students.get(studentId).addCreditScore(2); //捐书成功积分+2
        LibrarySystem.PRINTER.accept(command);
    }
    
    public void queryCreditScore(LibraryCommand command) {
        LibraryQcsCmd request = (LibraryQcsCmd) command;
        String studentId = request.getStudentId();
        if (!students.containsKey(studentId)) {
            Student student = new Student(studentId);
            students.put(studentId, student);
        }
        int creditScore = students.get(studentId).getCreditScore();
        LibrarySystem.PRINTER.info(command, creditScore);
    }
    
    public void checkCanBorrowed(LibraryBookId libraryBookId, Student stu, LibraryCommand cmd) {
        if (libraryBookId.isFormal()) {
            bookShelf.isBorrowed(libraryBookId); //从书架上取书，送到借还处
        } else {
            driftCorner.isBorrowed(libraryBookId); //从漂流角取书，送到借还处
        } //两者后续行为一样，提取出来
        borrowReturnDesk.receiveBook(libraryBookId);
        if (borrowReturnDesk.checkCanBorrowed(libraryBookId, stu)) {
            borrowReturnDesk.sendBook(libraryBookId);
            Book book = new Book(libraryBookId, stu.getId(), null, cmd.getDate());
            stu.borrowBook(libraryBookId, book);
            LibrarySystem.PRINTER.accept(cmd);
        } else {
            LibrarySystem.PRINTER.reject(cmd);
        }
    }
    
    public void checkCanPicked(LibraryBookId libraryBookId, Student stu, LibraryCommand cmd) {
        HashMap<String, ArrayList<Book>> keepBooks = reservationDesk.getKeepBooks();
        if (!keepBooks.containsKey(stu.getId())) { //没有为这个人保留的书
            LibrarySystem.PRINTER.reject(cmd);
        } else {
            ArrayList<Book> arrayList = keepBooks.get(stu.getId());
            HashSet<LibraryBookId> orderedBooks = stu.getOrderedBooks();
            for (Book book : arrayList) {
                if (book.getLibraryBookId().equals(libraryBookId)) { //有这本书，做出相应成功或失败打印信息后结束循环
                    if (reservationDesk.checkCanPicked(libraryBookId, stu)) { //可以取书
                        arrayList.remove(book);
                        book.setBorrowStart(cmd.getDate()); //取书日期就是借书日期的开始
                        stu.borrowBook(libraryBookId, book); //取书还是相当于借书行为
                        if (libraryBookId.isTypeB()) {
                            stu.setOrderTypeB(false); //当前没有对B的预约了
                        }
                        orderedBooks.remove(libraryBookId); //发起该预约的用户取书成功时，预约视作完成不再有效。
                        int count = checkMap.get(libraryBookId);
                        count--;
                        checkMap.put(libraryBookId, count);
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
    
    public void borrowReturnDesk2Place(HashMap<LibraryBookId, Integer> brBooks,
                                       ArrayList<LibraryMoveInfo> print) {
        for (LibraryBookId libraryBookId : brBooks.keySet()) {
            if (libraryBookId.isFormal()) { //正式书籍的处理
                int value = brBooks.get(libraryBookId);
                for (int i = 0; i < value; i++) {
                    bookShelf.receiveBook(libraryBookId); //书架获得从借还处的书
                    LibraryMoveInfo libraryMoveInfo = new LibraryMoveInfo(libraryBookId,
                            "bro", "bs");
                    print.add(libraryMoveInfo);
                }
            } else { //漂流书籍的处理，相同书号的书数据已保证只有一本，value最多为1
                int count = driftCorner.queryCount(libraryBookId);
                for (int i = 0; i < brBooks.get(libraryBookId); i++) {
                    if (count == 2) { //成为正式书籍，送往书架
                        LibraryBookId newLibraryBookId = libraryBookId.toFormal(); //给予正式编号
                        bookShelf.addNewBook(newLibraryBookId);/*注意我并未去除driftCorner对应的旧书如有新增需要修改!*/
                        LibraryMoveInfo libraryMoveInfo = new LibraryMoveInfo(libraryBookId,
                                "bro", "bs");
                        print.add(libraryMoveInfo); //但在本次move输出时，仍输出原类别号（[A|B|C]U）
                        String studentId = driftCorner.queryFrom(libraryBookId);
                        students.get(studentId).addCreditScore(2);
                    } else { //还回漂流处
                        driftCorner.receiveBook(libraryBookId, null); //只有donate的时候需要用studentId参数
                        LibraryMoveInfo libraryMoveInfo = new LibraryMoveInfo(libraryBookId,
                                "bro", "bdc");
                        print.add(libraryMoveInfo);
                    }
                }
            }
        }
        brBooks.clear(); //都从借还处回到各自该在的地方了
    }
    
    public void outDateBooks2BookShelf(HashMap<LibraryBookId, Integer> outDateBooks,
                                       ArrayList<LibraryMoveInfo> print) {
        for (LibraryBookId libraryBookId : outDateBooks.keySet()) { //将过期的书搬回书架
            int value = outDateBooks.get(libraryBookId);
            for (int i = 0; i < value; i++) {
                bookShelf.receiveBook(libraryBookId);
                if (checkMap.containsKey(libraryBookId)) {
                    int count = checkMap.get(libraryBookId);
                    count--;
                    checkMap.put(libraryBookId, count); //对这本书的预约失效了
                }
                LibraryMoveInfo libraryMoveInfo = new LibraryMoveInfo(libraryBookId, "ao", "bs");
                print.add(libraryMoveInfo);
            }
        }
        outDateBooks.clear(); //清空map
    }
    
    public void updateCreditScore(LibraryCommand command) {
        for (Student student : students.values()) {
            HashMap<LibraryBookId, Book> borrowedBooks = student.getBorrowedBooks();
            for (Book book : borrowedBooks.values()) { //检查这个人所拥有的所有book
                if (!book.getFlag()) { //可能过期了但是未被check过，flag为true说明这本书一定过期了且积分变动过了
                    if (book.getLibraryBookId().isTypeB()) {
                        if (ChronoUnit.DAYS.between(book.getBorrowStart(),
                                command.getDate()) > 30) {
                            book.setFlag(true);//表示过期的已经check过了，不会再更新积分第二次
                            student.addCreditScore(-2);
                        }
                    } else if (book.getLibraryBookId().isTypeC()) {
                        if (ChronoUnit.DAYS.between(book.getBorrowStart(),
                                command.getDate()) > 60) {
                            book.setFlag(true);
                            student.addCreditScore(-2);
                        }
                    } else if (book.getLibraryBookId().isTypeBU()) {
                        if (ChronoUnit.DAYS.between(book.getBorrowStart(),
                                command.getDate()) > 7) {
                            book.setFlag(true);
                            student.addCreditScore(-2);
                        }
                    } else if (book.getLibraryBookId().isTypeCU()) {
                        if (ChronoUnit.DAYS.between(book.getBorrowStart(),
                                command.getDate()) > 14) {
                            book.setFlag(true);
                            student.addCreditScore(-2);
                        }
                    }
                }
            }
        }
    }
}
