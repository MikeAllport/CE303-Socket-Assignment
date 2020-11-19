package Utils;

import java.util.ArrayList;

public class ListLock<T> {
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
