package pspa;

import org.eclipse.swt.graphics.Point;

/**
 * Represents a box model to lay out items in a grid.
 * @author Cody Crow
 *
 */
public class BoxModel {
	/**
	 * x coordinate
	 */
	double x;
	/**
	 * y coordinate
	 */
	double y;
	/**
	 * rows in the grid (max of rows and columns)
	 */
	int rows;
	/**
	 * columns in the grid (max of rows and columns)
	 */
	int columns;
	/**
	 * the sum of the left and right margins (expressed as a ratio of the
	 * page width)
	 */
	double xMargin;
	/**
	 * the sum of the top and bottom margins (expressed as a ratio of the
	 * page height)
	 */
	double yMargin;
	/**
	 * the sum of the left and right padding of an individual cell
	 * (expressed as a ratio of the page width)
	 */
	double xPadding;
	/**
	 * the sum of the top and bottom padding of an individual cell
	 * (expressed as a ratio of the page height)
	 */
	double yPadding;
	
	public BoxModel() {
		rows = columns = 0;
		xMargin = yMargin = xPadding = yPadding = 0;
	}
	
	public BoxModel(int rows, int columns) {
		this();
		this.rows = rows;
		this.columns = columns;
		//if (rows > columns) this.rows = this.columns = rows;
		//else this.rows = this.columns = columns;
	}
	
	public BoxModel(int rows, int columns, double xMargin, double yMargin) {
		this(rows, columns);
		this.xMargin = xMargin;
		this.yMargin = yMargin;
		this.x = xMargin / 2;
		this.y = yMargin / 2;
	}
	
	public BoxModel(int rows, int columns, double xMargin, double yMargin, double xPadding, double yPadding) {
		this(rows, columns, xMargin, yMargin);
		this.xPadding = xPadding;
		this.yPadding = yPadding;
	}
	
	/**
	 * 
	 * @param pageWidth width of the page
	 * @param pageHeight height of the page
	 * @return the position (x, y) where the container box is located
	 */
	public Point position(int pageWidth, int pageHeight) {
		int x = (int) (this.x * pageWidth);
		int y = (int) (this.y * pageHeight);
		return new Point(x, y);
	}
	
	public Point dimensions(int pageWidth, int pageHeight) {
		int width = (int) (pageWidth * (1 - this.xMargin));
		int height = (int) (pageHeight * (1 - this.yMargin));
		return new Point(width, height);
	}
	
	/**
	 * 
	 * @param pageWidth width of the page
	 * @param pageHeight height of the page
	 * @param row the row containing the cell
	 * @param column the column containing the cell
	 * @return the position (x, y) where the cell contents is located (i.e.
	 * inside the padding)
	 */
	public Point cellPosition(int pageWidth, int pageHeight, int row, int column) {
		int x, y;
		int startX = this.position(pageWidth, pageHeight).x;
		int startY = this.position(pageWidth, pageHeight).y;
		//x = (int) (startX + ((this.xPadding * pageWidth) / 2) + (column * (this.dimensions(pageWidth, pageHeight).x / this.columns)));
		//y = (int) (startY + ((this.yPadding * pageHeight) / 2) + (row * (this.dimensions(pageWidth, pageHeight).y / this.rows)));
		x = (int) (startX + ((this.xPadding * pageWidth) / 2) + (column * this.cellDimensions(pageWidth, pageHeight).x));
		y = (int) (startY + ((this.yPadding * pageHeight) / 2) + (row * this.cellDimensions(pageWidth, pageHeight).y));
		return new Point(x, y);
	}
	
	public Point cellDimensions(int pageWidth, int pageHeight) {
		Point p = this.dimensions(pageWidth, pageHeight);
		int max = columns > rows ? columns : rows;
		return new Point(p.x / max, p.y / max);
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

	public int getColumns() {
		return columns;
	}

	public void setColumns(int columns) {
		this.columns = columns;
	}

	public double getxMargin() {
		return xMargin;
	}

	public void setxMargin(double xMargin) {
		this.xMargin = xMargin;
	}

	public double getyMargin() {
		return yMargin;
	}

	public void setyMargin(double yMargin) {
		this.yMargin = yMargin;
	}

	public double getxPadding() {
		return xPadding;
	}

	public void setxPadding(double xPadding) {
		this.xPadding = xPadding;
	}

	public double getyPadding() {
		return yPadding;
	}

	public void setyPadding(double yPadding) {
		this.yPadding = yPadding;
	}
}
