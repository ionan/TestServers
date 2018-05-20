package ionan.github.com.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

public class Utils {
	
	public static List<String> getSubdirectories(String rootFolder) {
		List<String> dirs = new ArrayList<>();
		File[] children = new File(rootFolder).listFiles();
		for (File child : children) {
			if (child.isDirectory()) dirs.add(child.getName());
		}
		return dirs;
	}
	
	public static void showHelp(Options options) {
		HelpFormatter formater = new HelpFormatter();
		formater.printHelp("Main", options);
		System.exit(0);
	}
}
