using System;
using System.IO;
using System.Net.Sockets;
namespace Client
{
    /**
     * SocketListener's purpose is to read and all communication sent from the
     * server and send to the Handler to process.
     * 
     * As socket exceptions are raised in this thread, and not main thread,
     * Server rebooting initiation goes here
     */
    class SocketListener
    {
        private StreamReader reader;
        private Handler handler;

        public SocketListener(Handler handler)
        {
            this.handler = handler;
            this.reader = handler.Reader;
        }

        public void Run()
        {
            try
            {
                while (!Program.SocketClosed)
                {
                    string line = reader.ReadLine();
                    this.handler.ProcessLine(line);
                }
            }
            catch (Exception e)
            {
                if (!Program.applicationClosed)
                    Program.RestartServer();
            }
        }
    }
}
