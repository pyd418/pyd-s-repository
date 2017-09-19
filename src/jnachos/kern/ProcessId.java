package jnachos.kern;

public class ProcessId {
	
	static private int id=0;
	
	static public int getId() {
		id=id+1;
		return id;
	}
}
