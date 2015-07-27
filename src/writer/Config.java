package writer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Config {
	public String productKeyURL;
	public String generateProductKeyURL;
	public boolean validated;
	
	public Config() throws IOException {
		this("config.rc");
	}
	
	public Config(String rc) throws IOException {
		File f = new File(rc);
		BufferedReader br = new BufferedReader(new FileReader(f));
		
		String line;
		while (br.ready()) {
			line = br.readLine();
			if (line.startsWith("#")) continue;
			if (!line.contains("	")) continue;
			String[] tokens = line.split("	");
			switch (tokens[0]) {
			case "product_key_url":
				productKeyURL = tokens[1];
				break;
			case "generate_product_key_url":
				generateProductKeyURL = tokens[1];
				break;
			case "validated":
				validated = true;
				break;
			}
		}
	}

}
