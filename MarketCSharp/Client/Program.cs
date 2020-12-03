using System;
using System.IO;
using System.Threading;
using Server;
using Utils;

namespace Client
{
    /**
     * Programs purpose is to store the main logic in creating a handler, contain
     * the main run loop for console apps, and the logic required for restarting
     * the server.
     */
    public static class Program
    {
        public static readonly int PORT = Server.Program.PORT;
        public static readonly string SERVER_ADDRESS = "localhost";
        private static Handler _handler;
        public static Handler Handler { get { return _handler; } }
        private static Market _market;
        public static Market Market { get { return _market; } }

        // control variables for deciding if console app is run, if ui has closed app
        // and whether the socket encountered an error
        public static bool SocketClosed = false;
        public static bool applicationClosed = false;

        // Server file path is from a given compiled directory i.e 
        // {ProjectDIR}\Client\bin\Release\netcoreapp3.0\compiled now points
        // to {ProjectDIR}\ServerUI\bin\Release\netcoreapp3.0\ServerUi.exe
        private static readonly string SERVER_LOCATION_FROM_COMPILED_PATH =
            Path.GetFullPath(Path.Combine(AppContext.BaseDirectory, "..\\..\\..\\..\\ServerUI\\bin\\" +
                "Debug\\netcoreapp3.0\\ServerUI.exe"));

        // instantiates marker and handler
        public static void Run()
        {
            if (Market == null)
                _market = new Market();
            else
                Market.ClearMarket();
            SocketClosed = false;
            try
            {
                _handler = new Handler(_market);
            } catch (Exception ignore) {
                Console.WriteLine("Server not running, restarting server");
                RestartServer();
            }
        }

        // main code for restarting the server
        public static void RestartServer()
        {
            try
            {
                System.Diagnostics.Process.Start(SERVER_LOCATION_FROM_COMPILED_PATH, "Restore");
                SocketClosed = false;
                Handler.ServerReconnecting = true;
                Thread.Sleep(1000);
                Run();
            } catch (System.ComponentModel.Win32Exception e)
            {
                Console.WriteLine("Could not find server executable to restore");
            }
        }

        public static void Main(string[] args)
        {
            Program.Run();
        }
    }
}
