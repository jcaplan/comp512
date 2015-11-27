package crash;

public interface Crash {

	public void crash() throws CrashException;
	
	public void crash(String message) throws CrashException;
	
	
}
