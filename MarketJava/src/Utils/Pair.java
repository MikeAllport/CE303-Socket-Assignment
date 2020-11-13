package Utils;

/**
 * Simple pair container to store two objects
 * inspiration taken from class provided in C++ STL & package javafx
 * did not want to use javafx as unknown if this is on lab computers
 *
 * @param <T>
 *     Any type of object
 * @param <S>
 *     Any type of object
 */
public class Pair<T, S>{
    T t;
    S s;
    public Pair(T t, S s)
    {
        this.t = t;
        this.s = s;
    }

    public T first()
    {
        return this.t;
    }

    public S second()
    {
        return this.s;
    }

}
