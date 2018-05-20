package ionan.github.com.services;

import java.util.List;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;

import ionan.github.com.utils.Utils;

/**
 * PORTS:
 * 	SMTP    	3025
 * 	SMTPS    	3465
 * 	POP3    	3110
 * 	POP3S    	3995
 * 	IMAP    	3143
 * 	IMAPS    	3993
 */
public class MailServer {
	
	private GreenMail greenMail = null;
	private String workDirectory = null;
	
	public MailServer(String workDirectory) {
		this.workDirectory = workDirectory;
	}
	
	public void start() {
		this.greenMail = new GreenMail(ServerSetupTest.ALL);
		this.greenMail.start();
		System.out.println("\tAdding following mail users:");
		List<String> users = Utils.getSubdirectories(this.workDirectory);
		for (String user : users) {
			this.greenMail.setUser(user + "@localhost", user, user);
			System.out.println("\t\t" + user + "@localhost");
		}
	}
	
	public void stop() {
		this.greenMail.stop();
	}
}

