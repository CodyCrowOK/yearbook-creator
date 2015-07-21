package command;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Represents the command stacks for undo/redo functionality.
 * @author Cody Crow
 *
 */
public class Stack {
	private Deque<Command> undoStack;
	private Deque<Command> redoStack;
	
	public Stack() {
		undoStack = new ArrayDeque<Command>();
		redoStack = new ArrayDeque<Command>();
	}
	
	public Command undo() {
		if (undoStack.isEmpty()) return new Command(Commands.DO_NOTHING);
		Command c = undoStack.pop();
		redoStack.push(c);
		return c;
	}
	
	public Command redo() {
		if (redoStack.isEmpty()) return new Command(Commands.DO_NOTHING);
		Command c = redoStack.pop();
		undoStack.push(c);
		return c;
	}
	
	public void push(Command c) {
		undoStack.push(c);
		redoStack.clear();
	}
}
