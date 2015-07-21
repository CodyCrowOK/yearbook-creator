package command;

public class Command {
	public Commands action;
	
	public Command(Commands c) {
		this.action = c;
	}
	
	public boolean isElement() {
		return false;
	}
	
	public boolean isPage() {
		return false;
	}
}
