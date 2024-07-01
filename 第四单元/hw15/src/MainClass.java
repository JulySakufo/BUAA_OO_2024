import com.oocourse.library3.LibraryCommand;
import com.oocourse.library3.LibraryOpenCmd;
import com.oocourse.library3.LibraryCloseCmd;
import com.oocourse.library3.LibrarySystem;
import com.oocourse.library3.LibraryReqCmd;
import com.oocourse.library3.LibraryQcsCmd;
import com.oocourse.library3.LibraryRequest;

import java.util.ArrayList;

public class MainClass {
    public static void main(String[] args) {
        BookShelf bookShelf = new BookShelf(LibrarySystem.SCANNER.getInventory());
        ReservationDesk reservationDesk = new ReservationDesk();
        BorrowReturnDesk borrowReturnDesk = new BorrowReturnDesk();
        DriftCorner driftCorner = new DriftCorner();
        Library library = new Library(bookShelf, reservationDesk, borrowReturnDesk, driftCorner);
        while (true) {
            LibraryCommand command = LibrarySystem.SCANNER.nextCommand();
            if (command == null) {
                break;
            }
            if (command instanceof LibraryOpenCmd) {
                library.updateCreditScore(command); //在整理图书前判断逾期就行了
                library.moveBook(command); // 在图书馆开门之前干点什么
            } else if (command instanceof LibraryCloseCmd) {
                LibrarySystem.PRINTER.move(command.getDate(), new ArrayList<>()); // 什么都不干，操作数恒为0
            } else if (command instanceof LibraryQcsCmd) {
                library.queryCreditScore(command);
            } else { // 得到type student bookId(A-0000)
                LibraryReqCmd request = (LibraryReqCmd) command;
                if (request.getType() == LibraryRequest.Type.QUERIED) {
                    library.queryBook(command);
                } else if (request.getType() == LibraryRequest.Type.BORROWED) {
                    library.borrowBook(command);
                } else if (request.getType() == LibraryRequest.Type.ORDERED) {
                    library.orderNewBook(command);
                } else if (request.getType() == LibraryRequest.Type.PICKED) {
                    library.getOrderedBook(command);
                } else if (request.getType() == LibraryRequest.Type.RETURNED) {
                    library.returnBook(command);
                } else if (request.getType() == LibraryRequest.Type.RENEWED) {
                    library.renewBook(command);
                } else if (request.getType() == LibraryRequest.Type.DONATED) {
                    library.donateBook(command);
                }
            }
        }
    }
}