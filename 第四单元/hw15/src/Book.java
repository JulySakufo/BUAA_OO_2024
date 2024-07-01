import com.oocourse.library3.LibraryBookId;

import java.time.LocalDate;

public class Book {
    private LibraryBookId libraryBookId;
    private String studentId;
    private LocalDate reservationStart; //预约日期
    private LocalDate borrowStart; //借书日期
    private boolean flag; //true表示这本书逾期已经检查过(结合当前日期)
    
    public Book(LibraryBookId libraryBookId, String studentId, LocalDate start) { //预约书籍
        this.libraryBookId = libraryBookId;
        this.studentId = studentId;
        this.reservationStart = start;
        this.borrowStart = null;
        this.flag = false;
    }
    
    public Book(LibraryBookId libraryBookId, String studentId, LocalDate rs, LocalDate bs) { //借的书籍
        this.libraryBookId = libraryBookId;
        this.studentId = studentId;
        this.reservationStart = rs;
        this.borrowStart = bs;
        this.flag = false;
    }
    
    public LibraryBookId getLibraryBookId() {
        return libraryBookId;
    }
    
    public String getStudentId() {
        return studentId;
    }
    
    public LocalDate getReservationStart() {
        return reservationStart;
    }
    
    public LocalDate getBorrowStart() {
        return borrowStart;
    }
    
    public boolean getFlag() {
        return flag;
    }
    
    public void setBorrowStart(LocalDate borrowStart) {
        this.borrowStart = borrowStart;
    }
    
    public void setFlag(boolean flag) {
        this.flag = flag;
    }
}
