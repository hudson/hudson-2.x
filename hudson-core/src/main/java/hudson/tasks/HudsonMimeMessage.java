package hudson.tasks;

import java.io.InputStream;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

/**
 * Class extends MimeMessage from javax.mail, because parent doesn't provide with appropriate getters.
 * <p/>
 * Date: 7/25/11
 *
 * @author Nikita Levyankov
 */
public class HudsonMimeMessage extends MimeMessage {
    public HudsonMimeMessage(Session session) {
        super(session);
    }

    public HudsonMimeMessage(Session session, InputStream is) throws MessagingException {
        super(session, is);
    }

    /**
     * Returns {@link Session} instance
     * @return session.
     */
    public Session getSession() {
        return session;
    }
}
