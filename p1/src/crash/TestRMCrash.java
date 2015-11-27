package crash;

public class TestRMCrash implements Crash{
	@Override
	public void crash() throws CrashException {
		throw new CrashException("RM crashed");
	}

	@Override
	public void crash(String message) throws CrashException {
		throw new CrashException(message);
		
	}
}
