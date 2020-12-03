using System;
using System.Collections.Generic;
using System.Windows;
using System.Windows.Controls;
using Client;
using System.Windows.Threading;
using Utils;
using System.Threading;

namespace ClientUI
{
    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class ClientGooey : Window
    {
        private SortedSet<Trader> lastCheckedTraderList;
        public ClientGooey()
        {
            InitializeComponent();
            Program.Run();
            lastCheckedTraderList = Program.Market.TradersListCopy;
            DispatcherTimer timer = new DispatcherTimer();
            timer.Interval = TimeSpan.FromSeconds(0.5);
            timer.Tick += OnUpdate;
            timer.Start();
        }

        private void OnUpdate(object sender, object e)
        {
            if (!SortedSet<Trader>.CreateSetComparer().Equals(lastCheckedTraderList, Program.Market.TradersListCopy))
            {
                TraderGrid.Children.Clear();
                TraderGrid.RowDefinitions.Clear();
                int i = 0;
                foreach (var trader in Program.Market.TradersListCopy)
                {
                    RowDefinition row = new RowDefinition();
                    row.Height = new GridLength(20);
                    TraderGrid.RowDefinitions.Add(row);
                    Button traderButton = new Button();
                    traderButton.Style = (Style)Resources["TraderBut"];
                    traderButton.Content = trader.ID;
                    traderButton.Width = TraderGrid.ColumnDefinitions[0].ActualWidth;
                    TraderGrid.Children.Add(traderButton);
                    Grid.SetRow(traderButton, i++);
                }
            }
            MessageGrid.Children.Clear();
            MessageGrid.RowDefinitions.Clear();
            int j = 0;
            foreach (var message in Program.Market.UiMessageList)
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
            if(Program.Handler != null && Program.Handler.Trader != null)
                YourIDLabel.Text = Program.Handler.Trader.ID;
            if(Program.Market.StockHolder != null)
                StockHolderLabel.Text = Program.Market.StockHolder.ID;
            MessagePane.ScrollToBottom();
            TraderPane.ScrollToBottom();
        }

        private void OnClickTraderClick(object sender, RoutedEventArgs e)
        {
            Button buttonClicked = e.Source as Button;
            TradeWithTextBox.Text = buttonClicked.Content.ToString();
        }

        private void MakeTrade(object sender, RoutedEventArgs e)
        {
            Program.Handler.SendMessage(Message.makeTrade(TradeWithTextBox.Text));
        }

        private void Window_Closing(object sender, System.ComponentModel.CancelEventArgs e)
        {
            Hide();
            if (Handler.ServerReconnecting)
                Thread.Sleep(3000);
            if (Program.Handler != null)
                Program.Handler.Dispose();
            Program.applicationClosed = true;
        }

        private void Window_Closed(object sender, EventArgs e)
        {
            if (Program.Handler != null)
                Program.Handler.Dispose();
            Program.applicationClosed = true;
        }
    }
}
