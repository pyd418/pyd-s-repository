/**
 * Copyright (c) 1992-1993 The Regents of the University of California.
 * All rights reserved.  See copyright.h for copyright notice and limitation 
 * of liability and disclaimer of warranty provisions.
 *  
 *  Created by Patrick McSweeney on 12/5/08.
 */
package jnachos.kern;

import java.util.ArrayList;
import java.util.List;

import jnachos.filesystem.OpenFile;
import jnachos.machine.*;

/** The class handles System calls made from user programs. */
public class SystemCallHandler {
	
	/** The System call index for halting. */
	public static final int SC_Halt = 0;

	/** The System call index for exiting a program. */
	public static final int SC_Exit = 1;

	/** The System call index for executing program. */
	public static final int SC_Exec = 2;

	/** The System call index for joining with a process. */
	public static final int SC_Join = 3;

	/** The System call index for creating a file. */
	public static final int SC_Create = 4;

	/** The System call index for opening a file. */
	public static final int SC_Open = 5;

	/** The System call index for reading a file. */
	public static final int SC_Read = 6;

	/** The System call index for writting a file. */
	public static final int SC_Write = 7;

	/** The System call index for closing a file. */
	public static final int SC_Close = 8;

	/** The System call index for forking a forking a new process. */
	public static final int SC_Fork = 9;

	/** The System call index for yielding a program. */
	public static final int SC_Yield = 10;

	/**
	 * Entry point into the Nachos kernel. Called when a user program is
	 * executing, and either does a syscall, or generates an addressing or
	 * arithmetic exception.
	 * 
	 * For system calls, the following is the calling convention:
	 * 
	 * system call code -- r2 arg1 -- r4 arg2 -- r5 arg3 -- r6 arg4 -- r7
	 * 
	 * The result of the system call, if any, must be put back into r2.
	 * 
	 * And don't forget to increment the pc before returning. (Or else you'll
	 * loop making the same system call forever!
	 * 
	 * @pWhich is the kind of exception. The list of possible exceptions are in
	 *         Machine.java
	 **/
	public static void handleSystemCall(int pWhichSysCall) {
		

		Debug.print('a', "!!!!" + Machine.read1 + "," + Machine.read2 + "," + Machine.read4 + "," + Machine.write1 + ","
				+ Machine.write2 + "," + Machine.write4);
		
		System.out.println("SysCall:" + pWhichSysCall);

		switch (pWhichSysCall) {
		// If halt is received shut down
		case SC_Halt:
			Debug.print('a', "Shutdown, initiated by user program.");
			Interrupt.halt();
			break;

		case SC_Exit:
			// Read in any arguments from the 4th register
			System.out.println("Current process:"+JNachos.getCurrentProcess().getName()+" called SC_Exit.");
			int arg = Machine.readRegister(4);

			System.out
					.println("Current Process " + JNachos.getCurrentProcess().getName() + " exiting with code " + arg);
			
			Object waitingProcess=ProcessId.IdMap.get(JNachos.getCurrentProcess().getId());
			if(waitingProcess != null) {
				
				Machine.writeRegister(Machine.PCReg,Machine.readRegister(Machine.NextPCReg));
				// Finish the invoking process
				Scheduler.readyToRun((NachosProcess)ProcessId.IdMap.get(JNachos.getCurrentProcess().getId()));
				JNachos.getCurrentProcess().finish();
			}
			else {
				
				Machine.writeRegister(Machine.PCReg,Machine.readRegister(Machine.NextPCReg));
				JNachos.getCurrentProcess().finish();
			}
			
			break;

		case SC_Fork:
			Interrupt.setLevel(false);	
			System.out.println("Current process:"+JNachos.getCurrentProcess().getName()+" called SC_Fork.");
			
			Machine.writeRegister(2, 0);
			
			NachosProcess child=new NachosProcess("The child of "+JNachos.getCurrentProcess().getName());
			
			
			
			AddrSpace ChildAddrSpace=new AddrSpace(JNachos.getCurrentProcess().getSpace());
			child.setSpace(ChildAddrSpace);
			
			int pc_counter=Machine.readRegister(Machine.NextPCReg);
			Machine.writeRegister(Machine.PCReg,pc_counter);
			
			//pc+4 before the save user state
			child.saveUserState();
			
			Machine.writeRegister(2, child.getId());
			
			child.fork(new ForkProcess(), child);
			System.out.println("The Reg is:"+Machine.readRegister(4));
			
//			int pc_counter= Machine.readRegister(Machine.PCReg);
//			Machine.writeRegister(Machine.PCReg, pc_counter+4);
			
			Interrupt.setLevel(true);
			
			break;
			
		case SC_Join:
			Interrupt.setLevel(false);
			System.out.println("Current process:"+JNachos.getCurrentProcess().getName()+" called SC_Join.");
			
			int SpecificId=Machine.readRegister(4);
			System.out.println("The SpecificId is:"+ SpecificId);		
						
			if(ProcessId.IdMap.containsKey(SpecificId)!=true) {
				System.out.println("Break the join");
				pc_counter=Machine.readRegister(Machine.NextPCReg);
				Machine.writeRegister(Machine.PCReg,pc_counter);
				break;
			}
			else
			{
				ProcessId.IdMap.put(SpecificId,JNachos.getCurrentProcess());
			
				int JoinReturn=JNachos.getCurrentProcess().getId();
				Machine.writeRegister(2, JoinReturn);
				pc_counter=Machine.readRegister(Machine.NextPCReg);
				Machine.writeRegister(Machine.PCReg,pc_counter);
				
				JNachos.getCurrentProcess().sleep();
			
			}
//			NachosProcess invoke=JNachos.getCurrentProcess();
			Interrupt.setLevel(true);			
			break;
			
		case SC_Exec:
			Interrupt.setLevel(false);
			
			System.out.println("Current process:"+JNachos.getCurrentProcess().getName()+" called SC_Exec.");
			pc_counter=Machine.readRegister(Machine.NextPCReg);
			Machine.writeRegister(Machine.PCReg,pc_counter);
			
			int FileAddr=Machine.readRegister(4);
			int getMem=1;
			List<Integer> FileList=new ArrayList<>();
			String execFile=new String();
			while ( (char)getMem!='\0' ) {
				getMem=Machine.readMem(FileAddr, 1);
				
				if((char)getMem!='\0')
					execFile=execFile+(char)getMem;
				
				FileAddr++;
				FileList.add(getMem);
			}
			
//			execFile="/eclipse-workspace/JNachos_Pro1/"+execFile;
			
			OpenFile executable = JNachos.mFileSystem.open(execFile);

			// If the file does not exist
			if (executable == null) {
				System.out.println("Unable to open file: " + execFile);	
				break;
			}

			// Load the file into the memory space
			AddrSpace space = new AddrSpace(executable);
			JNachos.getCurrentProcess().setSpace(space);

			// set the initial register values
			space.initRegisters();

			// load page table register
			space.restoreState();

			System.out.println("Run the user program: "+execFile);
			
			// jump to the user progam
			// machine->Run never returns;
			Machine.run();
			
			
			Interrupt.setLevel(true);
			// the address space exits
			// by doing the syscall "exit"
		
//			JNachos.getCurrentProcess().finish();
			break;
		default:
			Interrupt.halt();
			break;
		}
	}
}
