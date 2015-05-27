import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.FileChannel;
import java.util.Date;

/**
 * Represents a video object on a yearbook page.
 * @author Cody Crow
 *
 */
public class Video implements Serializable {
	private static final long serialVersionUID = 5104869062313246983L;
	
	private String src; //Where the video is stored by this program
	public String name;
	
	public Video(String fileName) throws IOException {
		File source = new File(fileName);
		this.src = "media/" + Long.toString(new Date().getTime()) + source.getName();
		File destination = new File(this.src);
		copyFile(source, destination);
	}
	
	public Video(String fileName, String name) throws IOException {
		this(fileName);
		this.name = name;
	}
	
	public static void copyFile(File sourceFile, File destFile) throws IOException {
		destFile.createNewFile();

		FileChannel source = null;
		FileChannel destination = null;

		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		}
		finally {
			if(source != null) {
				source.close();
			}
			if(destination != null) {
				destination.close();
			}
		}
	}

	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
