package jnachos.kern;

import java.util.HashMap;
import java.util.Map;

public class ProcessId {
	
	static private int id=100;
	
	static public HashMap IdMap = new HashMap ();
	
	static public int getId() {
		id=id+1;
		return id;
	}
}
