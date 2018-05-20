package ionan.github.com.utils;

import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

@SuppressWarnings("deprecation")
public class POP3Checker {
	
	public static void main(String args[]) throws ParseException, MessagingException {
		Options options = new Options();
		options.addOption("h", "host", true, "POP3 Host");
		options.addOption("u", "user", true, "POP3 User");
		options.addOption("p", "password", true, "POP3 User");
		options.addOption("?", "help", false, "Show help");
		
		try {
			CommandLineParser parser = new BasicParser();
			CommandLine cmd = null;
			cmd = parser.parse(options, args);

			if (cmd.hasOption("?") || !cmd.hasOption("h") || 
				!cmd.hasOption("u") || !cmd.hasOption("p")) {
				Utils.showHelp(options);
			}
			
			new POP3Checker(cmd.getOptionValue("h"), cmd.getOptionValue("u"), cmd.getOptionValue("p")).check();
		} catch(ParseException e) {
			Utils.showHelp(options);
		}
	}
	
	private String host = null;
	private String user = null;
	private String password = null;
	private int limit = Integer.MAX_VALUE;
	
	public POP3Checker(String host, String user, String password){
		this.host = host;
		this.user = user;
		this.password = password;
	}
	
	public void check() throws MessagingException {
		Properties properties = System.getProperties();
		properties.put("mail.pop3.host", this.host);
        properties.put("mail.pop3.port", "3110");
	    Session session = Session.getDefaultInstance(properties);
	    Store store = session.getStore("pop3");
	    store.connect(this.host, this.user, this.password);
	    Folder inbox = store.getFolder("Inbox");
	    inbox.open(Folder.READ_ONLY);

	    Message[] messages = inbox.getMessages();

	    System.out.println("Number of messages found: " + messages.length);
	    System.out.println();

	    for (int i = 0; i < Math.min(messages.length, this.limit); i++) {
	      System.out.println("	Message " + (i + 1));
	      System.out.println("	From : " + messages[i].getFrom()[0]);
	      System.out.println("	Subject : " + messages[i].getSubject());
	      System.out.println("	Sent Date : " + messages[i].getSentDate());
	      System.out.println();
	    }

	    inbox.close(true);
	    store.close();
	}
}
