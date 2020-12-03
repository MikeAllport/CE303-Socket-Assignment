package Utils;

import java.util.ArrayList;

/**
 * ListLocks main purpose is for thread safety, any classes using lists where concurrency isVC34
 * @param <T> - The type of objects the list contains
 */
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
