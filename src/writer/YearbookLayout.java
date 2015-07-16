package writer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Date;
import java.util.Deque;

public class YearbookLayout implements Serializable {
	private static final long serialVersionUID = -7491785451364133319L;
	String name;
	Deque<YearbookElementPrototype> elements;

	public YearbookLayout(String name, Collection<YearbookElement> list) {
		elements = new ArrayDeque<YearbookElementPrototype>();
		this.name = name;
		for (YearbookElement e : list) {
			YearbookElementPrototype yep = new YearbookElementPrototype();
			yep.x = e.x;
			yep.y = e.y;
			yep.rotation = e.rotation;
			elements.add(yep);
		}
	}
	
	public void save() throws IOException {
		String fileName = Creator.LAYOUTS_DIR + File.separator + name + "_" + Long.toString(new Date().getTime()) + ".ctcl";
		File file = new File(fileName);
		if (!file.exists()) {
			file.createNewFile();
		}
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fos);

		oos.writeObject(this);
		oos.flush();
		oos.close();
	}
	
	public static YearbookLayout read(String fileName) throws IOException, ClassNotFoundException {
		FileInputStream fis = new FileInputStream(fileName);
		ObjectInputStream ois = new ObjectInputStream(fis);
		
		YearbookLayout layout = (YearbookLayout) ois.readObject();
		ois.close();
		return layout;
	}
	
	public boolean isPrototypeAtPoint(int x, int y, int pageWidth, int pageHeight) {
		for (YearbookElementPrototype yep : elements) {
			if (yep.isAtPoint(x, y, pageWidth, pageHeight)) return true;
		}
		return false;
	}

	public YearbookElementPrototype getPrototypeAtPoint(int x, int y, int pageWidth, int pageHeight) {
		for (YearbookElementPrototype yep : elements) {
			if (yep.isAtPoint(x, y, pageWidth, pageHeight)) return yep;
		}
		return null;
	}
}
