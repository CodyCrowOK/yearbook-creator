package writer;

/**
 * Represents the settings for the editor application.
 * @author Cody Crow
 *
 */
public class UserSettings {
	public CursorMode cursorMode;
	public boolean showGrid;
	public boolean showNavbarText;
	public boolean autosave;

	public UserSettings() {
		super();
		cursorMode = CursorMode.MOVE;
		autosave = true;
	}
	
	
}
