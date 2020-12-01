using System;
using System.Collections.Generic;
using System.Text;

namespace Client
{
    public class Trader: IComparable<Trader>
    {
        private string _ID;
        public string ID { get { return _ID; } set { _ID = value; } }

        public Trader()
        {
            _ID = "";
        }

        public Trader(string id)
        {
            _ID = id;
        }

        public override bool Equals(object obj)
        {
            Trader otherTrader = obj as Trader;
            if (obj == null)
                return false;
            return Equals(_ID, otherTrader.ID);
        }

        public override int GetHashCode()
        {
            return _ID.GetHashCode();
        }

        public int CompareTo(Trader other)
        {
            return string.Compare(ID, other.ID);
        }
    }
}
