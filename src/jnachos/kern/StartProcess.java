////////////////////////////////////////////////////////////////////////
//		StartProcess.java
//		Author:	Yudai Pan
//		Date:	2017/9/10
//		Purpose:	Start a new Process to run the user program
////////////////////////////////////////////////////////////////////////

package jnachos.kern;

import jnachos.filesystem.OpenFile;
import jnachos.machine.Interrupt;
import jnachos.machine.Machine;

public class StartProcess implements VoidFunctionPtr{
	
	private String filename;

	@Override
	public void call(Object pArg) {
		// TODO Auto-generated method stub
		String filename=(String)pArg;
		// The executable file to run
		OpenFile executable = JNachos.mFileSystem.open(filename);

		// If the file does not exist
		if (executable == null) {
			Debug.print('t', "Unable to open file" + filename);
			return;
		}

		// Load the file into the memory space
		AddrSpace space = new AddrSpace(executable);
		JNachos.getCurrentProcess().setSpace(space);

		// set the initial register values
		space.initRegisters();

		// load page table register
		space.restoreState();

		// jump to the user progam
		// machine->Run never returns;
		Machine.run();

		// the address space exits
		// by doing the syscall "exit"
		assert (false);
	}
	
}
