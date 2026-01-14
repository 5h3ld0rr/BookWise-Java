package bookwise.Mails;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BookBorrowMail extends Mail {

    private final String subject;
    private final String body;

    public BookBorrowMail(String name, String bookTitle, String dueDate) {
        String borrowDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        this.subject = "You've Borrowed a Book ";
        this.body = String.format("<div style=background:#111624;color:#fff;padding:50px;width:640px;box-sizing:border-box><div style=\"border-bottom:1px solid #232839;padding-bottom:2rem;height:30px\"><img height=30 src=https://res.cloudinary.com/dphmltfrh/image/upload/v1742541018/bookwise_logo.png><h2 style=display:inline;font-size:28px;margin-left:5px>BookWise</h2></div><h3 style=font-size:1.5rem;padding-top:1rem>You’ve Borrowed a Book!</h3><p style=color:#d6e0ff;font-size:18px><br>Hi %s,<br><br>You’ve successfully borrowed <span style=color:#eed1ac>%s</span>. Here are the details:.<ul style=color:#d6e0ff;font-size:18px><li>Borrowed On: <span style=color:#eed1ac>%s</span><li>Due Date: <span style=color:#eed1ac>%s</span></ul><p style=color:#d6e0ff;font-size:18px>Enjoy your reading, and don't forget to return the book on time!<br><br><br>Happy reading,<br>The BookWise Team</div>",
                name, bookTitle, borrowDate, dueDate);
    }

    @Override
    protected String getSubject() {
        return subject;
    }

    @Override
    protected String getBody() {
        return body;
    }
}
