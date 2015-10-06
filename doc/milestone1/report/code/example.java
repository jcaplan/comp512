protected boolean deleteItem(int id, String key) {
	Trace.info("RM::deleteItem(" + id + ", " + key + ") called.");
	ReservableItem curObj = (ReservableItem) readData(id, key);
	// Check if there is such an item in the storage.
	if (curObj == null) {
		Trace.warn("RM::deleteItem(" + id + ", " + key + ") failed: "
				+ " item doesn't exist.");
		return false;
	} else {
		if (curObj.getReserved() == 0) {
			removeData(id, curObj.getKey());
			Trace.info("RM::deleteItem(" + id + ", " + key + ") OK.");
			return true;
		} else {
			Trace.info("RM::deleteItem(" + id + ", " + key
					+ ") failed: " + "some customers have reserved it.");
			return false;
		}
	}
}