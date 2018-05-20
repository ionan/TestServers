package ionan.github.com;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import ionan.github.com.services.MailServer;
import ionan.github.com.services.SFTPServer;
import ionan.github.com.utils.EmailWatcher;
import ionan.github.com.utils.Utils;

@SuppressWarnings("deprecation")
public class LaunchServers {

	public static void main(String args[]) throws ParseException {
		Options options = new Options();
		options.addOption("sftp", "sftp", false, "Launch SFTP server");
		options.addOption("mail", "mail", false, "Launch mail server");
		options.addOption("dir", "directory", true, "Work directory");
		options.addOption("h", "help", false, "Show help");
		options.addOption("w", "watch", false, "Watch for directory changes");

		try {
			CommandLineParser parser = new BasicParser();
			CommandLine cmd = null;
			cmd = parser.parse(options, args);

			if (cmd.hasOption("h")) {
				Utils.showHelp(options);
			}
			
			String workDirectory = System.getProperty("user.dir");
			if (cmd.hasOption("dir")) {
				workDirectory = cmd.getOptionValue("dir");
				File wd = new File(workDirectory);
				if (!wd.exists() || !wd.isDirectory()) {
					System.err.println("Directory " + workDirectory + " is not valid");
					Utils.showHelp(options);
				}
			}
			
			if (!workDirectory.endsWith("/")) workDirectory += "/";
			
			if (cmd.hasOption("mail")) {
				createDirs(workDirectory + "mail/");
				System.out.println("Launching mail server...");
				new MailServer(workDirectory + "mail/").start();
				System.out.println("Mail server started!");
			}
			
			if (cmd.hasOption("sftp")) {
				createDirs(workDirectory + "sftp/");
				System.out.println("Launching sftp server...");
				new SFTPServer(workDirectory + "sftp/").start();
				System.out.println("SFTP server started!");
			}
			
			if (cmd.hasOption("w")) {
				System.out.println("Watching file system for changes...");
				new EmailWatcher(workDirectory + "mail/").watchAndSend();
			}
		} catch(ParseException | IOException e) {
			Utils.showHelp(options);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}
	
	private static void createDirs(String rootDir) {
		File rootFolder = new File(rootDir);
		if (!rootFolder.exists()) rootFolder.mkdirs();
		
		File[] children = rootFolder.listFiles();
		boolean noChildFolder = children == null || children.length == 0;
		if (!noChildFolder) {
			for (File child : children) {
				if (child.isDirectory()) {
					noChildFolder = false;
					break;
				}
			}
		}
		if (noChildFolder) new File(rootDir + "root/").mkdir();
	}
}
