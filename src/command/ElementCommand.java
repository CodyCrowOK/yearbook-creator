package command;

import writer.YearbookElement;

public class ElementCommand extends Command {
	public YearbookElement original;
	public YearbookElement modified;
	public int page;

	public ElementCommand(Commands c) {
		super(c);
	}
	
	public ElementCommand(Commands c, YearbookElement original, YearbookElement modified, int page) {
		this(c);
		this.original = original;
		this.modified = modified;
	}
	
	@Override
	public boolean isElement() {
		return true;
	}

}
