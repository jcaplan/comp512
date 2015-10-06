protected boolean deleteItem(int id, String key) {
	Trace.info("RM::deleteItem(" + id + ", " + key + ") called.");
	synchronized (syncLock) {
		ReservableItem curObj = (ReservableItem) readData(id, key);
		// Check if there is such an item in the storage.
		if (curObj == null) {
			Trace.warn("RM::deleteItem(" + id + ", " + key + ") failed: "
					+ " item doesn't exist.");
			return false;
		} else {
			//Actions on curObj
		}
	}
}