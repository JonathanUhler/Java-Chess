import java.io.Serializable;

public class Obj implements Serializable {
	private int i;

	public Obj(int i) {
		this.i = i;
	}

	@Override
	public String toString() {
		return super.toString() + ";" + i;
	}
}
