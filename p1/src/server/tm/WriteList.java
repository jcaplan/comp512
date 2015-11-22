package server.tm;

import server.RMItem;

import java.io.Serializable;
import java.util.HashMap;

public class WriteList implements Serializable{

    HashMap<String, RMItem> writeList;

    public WriteList(){
        writeList = new HashMap<>();
    }

    public boolean writeItem(String key, RMItem value){
        if(!writeList.containsKey(key)){
            writeList.put(key, value);
            return true;
        } else {
            return false;
        }
    }

    public RMItem getItem(String key){
        return writeList.get(key);
    }
}
