package ionan.github.com.services;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.UserAuth;
import org.apache.sshd.server.auth.UserAuthPassword;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.keyprovider.AbstractGeneratorHostKeyProvider;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.sftp.SftpSubsystem;

import ionan.github.com.utils.Utils;

public class SFTPServer {
	
	private SshServer sshd = null;
	private String workDirectory = null;
	private Set<String> users = new HashSet<>();
	
	public SFTPServer(String workDirectory) {
		this.workDirectory = workDirectory;
	}
	
	public void start() throws IOException {
		this.sshd = SshServer.setUpDefaultServer();
	    sshd.setPort(2222);
	    AbstractGeneratorHostKeyProvider hostKeyProvider = new SimpleGeneratorHostKeyProvider("hostkey.ser");
	    hostKeyProvider.setAlgorithm("RSA");
	    sshd.setKeyPairProvider(hostKeyProvider);

	    sshd.setCommandFactory(new ScpCommandFactory());

	    List<NamedFactory<Command>> namedFactoryList = new ArrayList<NamedFactory<Command>>();
	    namedFactoryList.add(new SftpSubsystem.Factory());
	    sshd.setSubsystemFactories(namedFactoryList);
	    
	    List<NamedFactory<UserAuth>> userAuthFactories = new ArrayList<>();
        userAuthFactories.add(new UserAuthPassword.Factory());
        sshd.setUserAuthFactories(userAuthFactories);
        List<String> sftpUsers = Utils.getSubdirectories(this.workDirectory);
        System.out.println("\tAdding following sftp users:");
		for (String user : sftpUsers) {
        	this.users.add(user);
        	System.out.println("\t\t" + user + " (work directory is: " + this.workDirectory + user + "/" + ")");
        }
	    sshd.setPasswordAuthenticator(new PasswordAuthenticator() {
            @Override
            public boolean authenticate(String username, String password, ServerSession session) {
                if (username.equals(password) && users.contains(username)) {
                	File wd = new File(workDirectory + username + "/");
                	sshd.setFileSystemFactory(new VirtualFileSystemFactory(wd.getAbsolutePath()));
                    return true;
                }
                return false;
            }
        });

	    sshd.start();
    }
}
