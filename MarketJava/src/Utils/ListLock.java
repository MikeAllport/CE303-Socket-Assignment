package Utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ListLock<T>{
    private ArrayList<T> list;
    private Object lock;

    public <T> ListLock()
    {
        list = new ArrayList<>();
        lock = new Object();
    }

    public ArrayList<T> getList()
    {
        return list;
    }

    public Object getLock()
    {
        return lock;
    }

}
