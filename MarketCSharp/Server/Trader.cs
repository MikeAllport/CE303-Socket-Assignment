using System;
using System.Collections.Generic;
using System.Threading;

namespace Server
{
    /*
    * Trader's purpose is to store the traders id, an outbox enabling synchronous messaging
    * of the server and client using an object lock for thread safety, and a death indicator 
    * enabling identifying if a trader has disconnected mid trade
    */
    public class Trader
    {
        private List<string> outbox; // stores traders messages from other threads
        private object outboxLock; // list lock for outbox synchronicity
        private string _ID; // trader id
        private int deathIndicator = 0; // indicates if trader disconnected
        private bool _reconnected; // indicates if trader is a reconnected one for server restoration
        public bool Reconnected { get { return _reconnected; } set { _reconnected = value; } }

        public int DeathIndicator { get { return deathIndicator; } }

        public string ID { get { return _ID; } }

        // Standard constructor iniliaizing traders id and other class members
        public Trader(string id)
        {
            this._ID = id;
            this.outbox = new List<string>();
            this.outboxLock = new object();
            this._reconnected = false;
        }

        // adds a message string to the traders inbox
        public void AddMessage(params string[] messages)
        {
            lock(outboxLock)
            {
                foreach (var message in messages)
                    outbox.Add(message);
            }
        }

        // copies the contents of the outbox, clears the outbox, and returns copy.
        // thread safe
        public List<string> GetMessages()
        {
            var outboxCopy = new List<string>();
            lock(outboxLock)
            {
                foreach (var message in outbox)
                    outboxCopy.Add(message);
                outbox.Clear();
            }
            return outboxCopy;
        }

        // increments the dead indicator variable so external classes know this instance is disconnected
        public void KillTrader()
        {
            Interlocked.Increment(ref deathIndicator);
        }

        // returns true if the trader has disconnected
        public bool IsDead()
        {
            return !(Interlocked.CompareExchange(ref deathIndicator, 0, 0) == 0);
        }

        public override bool Equals(object obj)
        {
            Trader otherTrader = obj as Trader;
            if (otherTrader == null)
                return false;
            return String.Equals(ID, otherTrader.ID);
        }

        public override int GetHashCode()
        {
            return ID.GetHashCode();
        }
    }
}
