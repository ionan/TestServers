package ionan.github.com.utils;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class EmailWatcher {
	
	private String workDirectory = null;
	
	public EmailWatcher(String workDirectory) {
		this.workDirectory = workDirectory;
	}
	
	public void watchAndSend() throws IOException, InterruptedException {
		FileSystem fs = FileSystems.getDefault();
        WatchService ws = fs.newWatchService();
        List<String> mailAccounts = Utils.getSubdirectories(this.workDirectory);
        for (String mailAccount : mailAccounts) {
        	System.out.println("\tWatching folder " + workDirectory + mailAccount);
        	File currentSentDir = new File(workDirectory + mailAccount + "/.sent/");
        	if (!currentSentDir.exists()) currentSentDir.mkdir();
        	Paths.get(workDirectory + mailAccount).register(ws, new WatchEvent.Kind[] {ENTRY_CREATE});
        }
        while(true){
            WatchKey k = ws.take();
            for (WatchEvent<?> e : k.pollEvents()){
                Object c = e.context();
                Path dir = (Path)k.watchable();
                Path fullPath = dir.resolve((Path) c);
                this.sendEmail(fullPath);
                fullPath.toFile().renameTo(new File(dir.toAbsolutePath().toString() + "/.sent/" + c.toString()));
            }
            k.reset();
        }
	}

	public void sendEmail(Path path) {    		
		String to = path.getParent().getFileName() + "@localhost";
		String from = "email-watcher@localhost";
		
		Properties properties = System.getProperties();
		properties.setProperty("mail.smtp.host", "localhost");
		properties.put("mail.smtp.port", 3025);

		Session session = Session.getDefaultInstance(properties);

		try {
			MimeMessage message = new MimeMessage(session);
			String subject = path.getFileName().toString().replaceAll("^(.*?)(\\.[^\\.]*)?$", "$1");
			message.setFrom(new InternetAddress(from));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			message.setSubject(subject);
			message.setSentDate(new Date());
			if (path.toFile().isDirectory()) {
				message.setText(path.getFileName().toString());
			} else {
				Multipart multipart = new MimeMultipart();

		        MimeBodyPart textBodyPart = new MimeBodyPart();
		        textBodyPart.setText(path.getFileName().toString());

		        MimeBodyPart attachmentBodyPart= new MimeBodyPart();
		        DataSource source = new FileDataSource(path.toAbsolutePath().toString());
		        attachmentBodyPart.setDataHandler(new DataHandler(source));
		        attachmentBodyPart.setFileName(path.getFileName().toString());

		        multipart.addBodyPart(textBodyPart);
		        multipart.addBodyPart(attachmentBodyPart);

		        message.setContent(multipart);
			}

			Transport.send(message);
			System.out.println("\tMessage successfully sent to " + to);
		} catch (MessagingException mex) {
			mex.printStackTrace();
		}
	}
}
