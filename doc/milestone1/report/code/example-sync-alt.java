protected boolean deleteItem(int id, String key) {
	try {
		mutex.acquire();
	} catch (InterruptedException e) {
		e.printStackTrace();
		return false;
	}
	ReservableItem curObj = (ReservableItem) readData(id, key);
	// Check if there is such an item in the storage.
	if (curObj == null) {
		Trace.warn("RM::deleteItem(" + id + ", " + key + ") failed: "
				+ " item doesn't exist.");
		return false;
	} else {
		synchronized (curObj) { //synchronize on the curObj
			mutex.release();   //safely release the mutex, 
								//now other threads can access hash table
			//do operations on curObj
			//...
		} //end sync block
	}
}