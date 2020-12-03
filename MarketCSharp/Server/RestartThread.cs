using System;
using System.Threading;
using System.IO;
using System.Collections.Generic;
using Utils;

namespace Server
{
    class RestartThread
    {
        public static void Run()
        {
            try
            {
                Thread.Sleep(5000);
                Market.KickDisconnectedTraders();
                Market.AddMarketMessage(Message.serverRecoveredUI());
                Market.BroadCastMessage(Message.serverRecoveredBroadCast());
                Console.WriteLine("Server recovery attempt finished");
            }
            catch (Exception) { }
            finally
            {
                Program.IsRestarting = false;
            }
        }
    }
}
