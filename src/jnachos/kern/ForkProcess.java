package jnachos.kern;

import jnachos.machine.Machine;

public class ForkProcess implements VoidFunctionPtr{

	@Override
	public void call(Object pArg) {
		// TODO Auto-generated method stub
		JNachos.getCurrentProcess().getSpace().restoreState();
		JNachos.getCurrentProcess().restoreUserState();
		
		Machine.run();
		
		assert (false);
	}

}
