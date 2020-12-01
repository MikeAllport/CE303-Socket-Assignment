using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;
using Server;
using System.Windows.Threading;
using System.Threading;
using Utils;

namespace ServerUI
{
    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class ServerGooey : Window
    {
        public static bool ServerIsReconnecting = false;
        public ServerGooey()
        {
            InitializeComponent();
            if (!ServerIsReconnecting)
                new Thread(Program.RunServer).Start();
            else
                new Thread(Program.RunServerBackup).Start();
            DispatcherTimer timer = new DispatcherTimer();
            timer.Interval = TimeSpan.FromSeconds(0.5);
            timer.Tick += OnUpdate;
            timer.Start();
        }

        private void OnUpdate(object sender, object e)
        {
            if (Program.IsRunning == false)
                Environment.Exit(1);
            TraderGrid.Children.Clear();
            TraderGrid.RowDefinitions.Clear();
            int i = 0;
            foreach (var trader in Market.TraderListCopy)
            {
                RowDefinition row = new RowDefinition();
                row.Height = new GridLength(20);
                TraderGrid.RowDefinitions.Add(row);
                Button traderButton = new Button();
                traderButton.Content = trader.ID;
                traderButton.Width = TraderGrid.ColumnDefinitions[0].ActualWidth;
                traderButton.Height = 20;
                TraderGrid.Children.Add(traderButton);
                Grid.SetRow(traderButton, i++);
            }
            MessageGrid.Children.Clear();
            MessageGrid.RowDefinitions.Clear();
            int j = 0;
            foreach (var message in Market.MarketMessagesCopy)
            {
                RowDefinition row = new RowDefinition();
                row.Height = new GridLength(20);
                MessageGrid.RowDefinitions.Add(row);
                TextBlock textToAdd = new TextBlock();
                textToAdd.Text = message;
                textToAdd.Height = 20;
                textToAdd.Width = MessageGrid.ColumnDefinitions[0].ActualWidth;
                MessageGrid.Children.Add(textToAdd);
                Grid.SetRow(textToAdd, j++);
            }
            MessagePane.ScrollToBottom();
            TraderPane.ScrollToBottom();
        }

        private void Window_Closing(object sender, System.ComponentModel.CancelEventArgs e)
        {
            Program.IsRunning = false;
        }

        private void Window_Closed(object sender, EventArgs e)
        {
            Program.IsRunning = false;
        }
    }
}
