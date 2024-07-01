import com.oocourse.library2.LibraryReqCmd;
import com.oocourse.library2.LibrarySystem;
import com.oocourse.library2.LibraryCommand;
import com.oocourse.library2.LibraryRequest;
import com.oocourse.library2.LibraryOpenCmd;
import com.oocourse.library2.LibraryCloseCmd;

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
                library.moveBook(command); // 在图书馆开门之前干点什么
            } else if (command instanceof LibraryCloseCmd) {
                LibrarySystem.PRINTER.move(command.getDate(), new ArrayList<>()); // 什么都不干，操作数恒为0
            } else { // 得到type student bookId(A-0000)
                LibraryReqCmd request = (LibraryReqCmd) command;
                if (request.getType() == LibraryRequest.Type.QUERIED) {
                    library.queryBook(command);
                } else if (request.getType() == LibraryRequest.Type.BORROWED) {
                    library.borrowBook(command);
                } else if (request.getType() == LibraryRequest.Type.ORDERED) {
                    library.orderBook(command);
                } else if (request.getType() == LibraryRequest.Type.PICKED) {
                    library.pickBook(command);
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