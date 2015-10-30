import static org.junit.Assert.*;

import org.junit.Test;

import pspa.BoxModel;


public class BoxModelTest {

	@Test
	public void test() {
		BoxModel box = new BoxModel(3, 3, (2.0 / 8.5), (3 / 11.0), 0, 0);
		assertEquals(235 / 2, box.position(1000, 1000).x);
		assertEquals(box.position(1000, 1000).x, box.cellPosition(1000, 1000, 0, 0).x);
		assertEquals(box.cellDimensions(1000, 1000), 0);
	}

}
