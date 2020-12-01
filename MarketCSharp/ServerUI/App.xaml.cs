using System;
using System.Collections.Generic;
using System.Configuration;
using System.Data;
using System.Linq;
using System.Threading.Tasks;
using System.Windows;

namespace ServerUI
{
    /// <summary>
    /// Interaction logic for App.xaml
    /// </summary>
    public partial class App : Application
    {
        private void Application_Startup(object sender, StartupEventArgs e)
        {
            if (e.Args.Length != 0 && Equals(e.Args[0], "Restore"))
                ServerGooey.ServerIsReconnecting = true;
            ServerGooey gui = new ServerGooey();
            gui.Title = "Server GUI";
            gui.Show();
        }
    }
}
