package command;

import writer.YearbookPage;

public class PageCommand extends Command {
	public YearbookPage page;
	public int source, destination;

	public PageCommand(Commands c) {
		super(c);
	}
	
	public PageCommand(Commands c, YearbookPage page, int source, int destination) {
		this(c);
		this.page = page;
		this.source = source;
		this.destination = destination;
	}

	@Override
	public boolean isPage() {
		return true;
	}
}
