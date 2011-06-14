package hudson.cli;

import java.io.IOException;
import java.net.URL;

/**
 * Exists for backward compatibility
 * @author Winston Prakash
 * @see org.eclipse.hudson.cli.FullDuplexHttpStream
 */
public class FullDuplexHttpStream extends org.eclipse.hudson.cli.FullDuplexHttpStream{

	public FullDuplexHttpStream(URL target) throws IOException {
		super(target);
	}
     
}
