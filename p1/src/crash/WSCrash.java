package crash;

public class WSCrash implements Crash {

	@Override
	public void crash() throws CrashException {
		System.exit(1);
	}

	@Override
	public void crash(String message) throws CrashException {
		System.out.println(message);
		System.exit(1);
	}
	
	
}
