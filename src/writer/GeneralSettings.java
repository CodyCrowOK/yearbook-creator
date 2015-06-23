package writer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.swt.widgets.Display;

public class GeneralSettings {
	public String workspace;
	
	public GeneralSettings(Display display) throws IOException {
		File file = new File(".dexp/.settings");
		if (!file.exists()) {
			file.createNewFile();
			this.workspace = "workspace";
		} else {
			BufferedReader in = new BufferedReader(new FileReader(file));
			String[] line = in.readLine().split(":");
			if (line[0] == "workspace") this.workspace = line[1];
			System.out.println(this.workspace);
		}
	}

}
