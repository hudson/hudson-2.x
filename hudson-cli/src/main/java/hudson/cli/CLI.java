package hudson.cli;

import java.io.IOException;
import java.net.URL;

/**
 * Exists for backward compatibility
 * @author Winston Prakash
 * @see org.eclipse.hudson.cli.CLI
 */
public class CLI extends org.eclipse.hudson.cli.CLI {

    public CLI(URL hudson) throws IOException, InterruptedException {
        super(hudson);
    }
}
