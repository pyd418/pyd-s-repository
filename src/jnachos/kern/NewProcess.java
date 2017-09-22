////////////////////////////////////////////////////////////////////////
//		NewProcess.java
//		Author:	Yudai Pan
//		Date:	2017/9/10
//		Purpose:	create a class to start a new process and 
//					call Machine.run() in different processes
////////////////////////////////////////////////////////////////////////

package jnachos.kern;

import jnachos.filesystem.OpenFile;
import jnachos.machine.Machine;

public class NewProcess implements VoidFunctionPtr{
	private String filename;
	
	public NewProcess(String FileName) {
		filename=FileName;
		Debug.print('t', "Entering SimpleTest");
		NachosProcess p = new NachosProcess(filename);
		p.fork(this, new Integer(1));
	}

	@Override
	public void call(Object pArg) {
		// TODO Auto-generated method stub
		System.out.println("run the user program "+filename);
		new StartProcess();
	}

}
