package bookwise.Mails;

import bookwise.DataAccess.CommonData;

public class BookReturnMail extends Mail {

    private final String subject;
    private final String body;

    public BookReturnMail(String name, String bookTitle, int noOfDaysOverdue, String dueDate) {
        double fineRate = CommonData.Rules.FINE_PER_DAY;
        double totalFine = fineRate * noOfDaysOverdue;

        String additionalInfo = noOfDaysOverdue > 0
                ? String.format("Next time, try to return it on time.</p><ul style=color:#d6e0ff;font-size:18px><li>Due Date: <span style=color:#eed1ac>%s</span><li>Overdue Since: <span style=color:#eed1ac>%d days</span><li>Fine Per Day: <span style=color:#eed1ac>Rs. %.2f</span><li>Total Fine: <span style=color:#eed1ac>Rs. %.2f</span></ul>", dueDate, noOfDaysOverdue, fineRate, totalFine)
                : "Thank you for returning it on time.</p>";

        this.subject = "Thank You for Returning the Book";
        this.body = String.format("<div style=background:#111624;color:#fff;padding:50px;width:640px;box-sizing:border-box><div style=\"border-bottom:1px solid #232839;padding-bottom:2rem;height:30px\"><img height=30 src=https://res.cloudinary.com/dphmltfrh/image/upload/v1742541018/bookwise_logo.png><h2 style=display:inline;font-size:28px;margin-left:5px>BookWise</h2></div><h3 style=font-size:1.5rem;padding-top:1rem>Thank You for Returning %s!</h3><p style=color:#d6e0ff;font-size:18px><br>Hi %s,<br><br>We've successfully received your return of <span style=color:#eed1ac>%s</span>. %s<br><br><p style=color:#d6e0ff;font-size:18px>Keep the pages turning,<br>The BookWise Team</p></div>",
                bookTitle, name, bookTitle, additionalInfo);
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
