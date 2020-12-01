using System;
using System.Collections.Generic;
using System.Text;
/**
 * Pair's purpose is as a container class taking two generic types as arguments
 * with getter functions.
 * 
 * Inspiration/Reference for class from c++ STL
 */
namespace Utils
{
    public class Pair<T, U>
    {
        private T _first;
        private U _second;
        public T First { get { return _first; } }
        public U Second {  get { return _second; } }

        public Pair(T first, U second)
        {
            _first = first;
            _second = second;
        }
    }
}
