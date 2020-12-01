using System;
using System.Net;
using System.Net.Sockets;
using System.Threading;
using System.IO;
using System.Collections.Generic;
using Utils;

namespace Server
{
    public class Program
    {
        public const int PORT = 8888;
        private static bool _isRunning = true;
        // bool used for shutting down the program
        public static bool IsRunning { get { return _isRunning; } set { _isRunning = value; } }
        // bol used for server restarting indication
        private static bool _isRestarting = false;
        public static bool IsRestarting { get { return _isRestarting; } set { _isRestarting = value; } }
        private static TcpListener _tcpListener;
        private static List<ClientHandler> handlers = new List<ClientHandler>();

        // backup file path is from a given compiled directory i.e 
        // {AssignmentDIR}MarketCSharp\Server\bin\Release\netcoreapp3.0\compiled now points
        // to {AssignmentDIR}
        private static readonly string BACKUP_FILE_PATH = 
            Path.GetFullPath(Path.Combine(AppContext.BaseDirectory, "..\\..\\..\\..\\..\\MarketBackup.json"));

        // Main TcpListening method
        public static void RunServer()
        {
            AppDomain.CurrentDomain.ProcessExit += Cleanup;
            _tcpListener = null;
            // initiates tcp listener or fails and exits
            try
            {
                _tcpListener = new TcpListener(IPAddress.Loopback, PORT);
                _tcpListener.Start();
                Console.WriteLine("Waiting for incoming connections...");
            } catch (SocketException e)
            {
                _isRunning = false;
                Console.WriteLine("Failed to start server, socket in use");
                return;
            }
            // main listener loop
            StartSocketShutdownThread();
            while (IsRunning)
            {
                try
                {
                    TcpClient tcpClient = _tcpListener.AcceptTcpClient();
                    ClientHandler handler = new ClientHandler();
                    handlers.Add(handler);
                    new Thread(handler.HandleIncomingMessages).Start(tcpClient);
                } catch (System.Net.Sockets.SocketException e) 
                {
                    _isRunning = false;
                }
            }
        }

        private static void Cleanup(object obj, EventArgs args)
        {
            // backup market
            CreateEmptyBackup();
            try
            {
                string marketJson = Market.Serialize();
                using (StreamWriter writer = File.AppendText(BACKUP_FILE_PATH))
                {
                    writer.Write(marketJson);
                    writer.Flush();
                }
            }
            catch (Exception e) 
            {
                Console.WriteLine($"Faile to create market backup\n{e.Message}");
            }
            foreach (var handler in handlers)
                handler.Dispose();
            _tcpListener.Stop();
        }

        // starts a thread that monitors whether or not the program is still running to
        // stop the TcpClient when down. Mainly used for UI as ui runs on independent
        // thread and closes the program when exited
        private static void StartSocketShutdownThread()
        {
            new Thread(() =>
            {
                while (true)
                {
                    if (!IsRunning)
                    {
                        Environment.Exit(1);
                        break;
                    }
                    Thread.Sleep(500);
                }
            }).Start();
        }

        // Main function for when server is restarting
        // deserializes market backup and starts thread to allow reconnnects for 5 seconds
        // before kicking traders who havent reconnected
        public static void RunServerBackup()
        {
            RestoreMarket();
            Console.WriteLine("Server is attempting recovery");
            PrintTradersFromBackup();
            Market.AddMarketMessage(Message.serverRebookUI());
            _isRestarting = true;
            new Thread(() =>
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
                    _isRestarting = false;
                }
            }).Start();
            RunServer();
        }

        // simply console prints any traders in temporary buffer waiting to be reconnected
        public static void PrintTradersFromBackup()
        {
            foreach (var trader in Market.TraderListCopy)
            {
                Console.WriteLine($"Temporary trader awaiting reconnection: {trader.ID}");
                Market.AddMarketMessage($"Temporary trader awaiting reconnection: {trader.ID}");
            }
            if (Market.StockHolder != null)
            {
                Console.WriteLine($"Temprorary trader with stock: {Market.StockHolder.ID}");
                Market.AddMarketMessage($"Temprorary trader with stock: {Market.StockHolder.ID}");
            }
        }

        // Deserializes the server backup json file, usable with csharp or java
        private static void RestoreMarket()
        {
            try
            {
                string backupJson = File.ReadAllText(BACKUP_FILE_PATH);
                Market.Deserialize(backupJson);
            } catch (Exception e)
            {
                Console.WriteLine($"Server backup failed\n{e.Message}");
            }
        }

        // creates an empty backup file
        private static void CreateEmptyBackup()
        {
            try
            {
                File.Create(BACKUP_FILE_PATH).Dispose();
            } catch (Exception e)
            {
                Console.WriteLine("Creating backup file failed! Is this being executed from compiled directory?" +
                    $"\n{e.Message}");
            }
        }

        public static void Main(string[] args)
        {
            RunServerBackup();
        }
    }
}
