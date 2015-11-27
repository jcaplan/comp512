package crash;

import lockmanager.LockManager;
import middleware.tm.TMClient;

public class TestCrash implements Crash {

	@Override
	public void crash() throws CrashException {
		throw new CrashException("RM crashed");
	}

	@Override
	public void crash(String message) throws CrashException {
		TMClient.deleteInstance();
		LockManager.reset();
		throw new CrashException(message);
		
	}

}
