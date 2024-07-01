import com.oocourse.library1.LibraryBookId;

import java.time.LocalDate;

public class Book {
    private LibraryBookId libraryBookId;
    private String studentId;
    private LocalDate start; //有效期
    
    public Book(LibraryBookId libraryBookId, String studentId, LocalDate start) {
        this.libraryBookId = libraryBookId;
        this.studentId = studentId;
        this.start = start;
    }
    
    public LibraryBookId getLibraryBookId() {
        return libraryBookId;
    }
    
    public String getStudentId() {
        return studentId;
    }
    
    public LocalDate getStart() {
        return start;
    }
}
