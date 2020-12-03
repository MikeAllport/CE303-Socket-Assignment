package Server;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.util.*;

/**
 * GUI's responsibility is for creating the user interface panel and containing synchronized methods
 * for client threads and ServerProgram to add traders/ui messages to interface.
 *
 * This works by containing a treemap of traders, for every ClientHandler instantiated and trader created
 * they add traders to this list with addTrader method. All threads can call addMessage to add ui related
 * messages which are stored in JLabel array. When server is reset, resetTraders() is called to empty the trader
 * list, and all traders are re-added when they have connected;
 */
public class GUI {
    private ServerProgram program;
    protected JFrame window;
    protected JPanel traders, broadcast;
    protected static final int WINDOW_HEIGHT = 550;
    protected static final int WINDOW_WIDTH = 800;
    protected static final int BROADCAST_PANEL_WIDTH = 650;
    protected static final int BUT_PANEL_WIDTH = 150;
    protected static final int BB_PANEL_HEIGHT = 550;
    protected static final int CELL_HEIGHT = 25;
    protected ArrayList<JLabel> messages;
    protected TreeMap<String, JButton> traderList;

    public GUI(String title)
    {
        this.messages = new ArrayList<>();
        this.window = new JFrame(title);
        this.window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.window.setLayout(new BorderLayout());
        this.broadcast = new JPanel();
        this.traders = new JPanel();
        traderList = new TreeMap<>();
        setWindow();
    }

    private void setWindow()
    {
        JPanel traders = getTradersPanel();
        JPanel broadcast = getBroadcastContainer();
        this.window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.window.add(traders, BorderLayout.WEST);
        this.window.add(broadcast, BorderLayout.EAST);
        Dimension windowSize = new Dimension(WINDOW_WIDTH, GUI.WINDOW_HEIGHT);
        this.window.setMinimumSize(windowSize);
        this.window.setMaximumSize(windowSize);
        this.window.setResizable(false);
        this.window.setVisible(true);
        this.window.pack();
    }

    private JPanel getBroadcastContainer()
    {
        Dimension dimension = new Dimension(GUI.BROADCAST_PANEL_WIDTH, GUI.BB_PANEL_HEIGHT);
        JPanel container = new JPanel();
        setPanel(this.broadcast, container, "Server Messages", dimension);
        return container;
    }

    private JPanel getTradersPanel()
    {
        Dimension dimension = new Dimension(GUI.BUT_PANEL_WIDTH, GUI.BB_PANEL_HEIGHT);
        JPanel container = new JPanel();
        setPanel(this.traders, container, "Traders", dimension);
        return container;
    }

    private void setPanel(JPanel toSetup, JPanel container, String labelName, Dimension dimension)
    {
        JLabel label = new JLabel(labelName);
        label.setVerticalAlignment(SwingConstants.TOP);
        label.setBorder(new EmptyBorder(5, 5, 5, 5));
        toSetup.setAlignmentX(SwingConstants.LEFT);
        toSetup.setBackground(new Color(255, 255, 255));
        toSetup.setLayout(new GridBagLayout());
        Dimension panelSize = new Dimension(dimension.width, dimension.height - label.getSize().height);
        toSetup.setSize(panelSize);
        toSetup.setMinimumSize(panelSize);

        container.setLayout(new BorderLayout());
        container.add(label, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(new EmptyBorder(0,10,0,10));
        scroll.setViewportView(toSetup);
        scroll.getVerticalScrollBar().setUnitIncrement(50);
        scroll.setPreferredSize(panelSize);
        container.add(scroll, BorderLayout.SOUTH);
    }


    public synchronized void addTrader(String id)
    {
        if (traderList.containsKey(id))
            return;
        JButton trader = new JButton(id);
        trader.setFocusPainted(false);
        trader.setMargin(new Insets(0, 0, 0, 0));
        trader.setContentAreaFilled(false);
        trader.setBorderPainted(false);
        trader.setOpaque(false);
        trader.setPreferredSize(new Dimension(GUI.BUT_PANEL_WIDTH - 20, GUI.CELL_HEIGHT));
        addButtonToList(trader, id);
    }

    protected void addButtonToList(JButton but, String id)
    {
        this.traderList.put(id, but);
        GridBagConstraints gbc = getConst();
        this.traders.removeAll();
        int i = 0;
        gbc.weighty = 0;
        for (Map.Entry<String, JButton> entry: traderList.entrySet())
        {
            gbc.gridy = i++;
            traders.add(entry.getValue(), gbc);
        }
        gbc.weighty = 1;
        gbc.gridy = i;
        this.traders.add(but, gbc);
        this.window.repaint();
        window.pack();
    }

    public synchronized void addMessage(String message)
    {
        JLabel messageLabel = new JLabel(message);
        addMessageToPanel(messageLabel);
    }

    protected void addMessageToPanel(JLabel label)
    {
        GridBagConstraints gbc = getConst();
        this.messages.add(label);
        broadcast.removeAll();
        int i;
        for (i = 0; i < messages.size(); i++)
        {
            gbc.gridy = i;
            broadcast.add(messages.get(i), gbc);
        }
        gbc.weighty = 1;
        gbc.gridy = ++i;
        this.broadcast.add(label, gbc);
        this.window.repaint();
        window.pack();
    }

    protected GridBagConstraints getConst()
    {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NORTHWEST;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.gridx = 0;
        gbc.ipadx = 0;
        gbc.ipady = 0;
        gbc.insets = new Insets(0,0,0,0);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        return gbc;
    }

    public synchronized void removeTrader(String id)
    {
        GridBagConstraints gbc = getConst();
        this.traderList.remove(id);
        this.traders.removeAll();
        int i = 0;
        gbc.weighty = 0;
        for (Map.Entry<String, JButton> entry: traderList.entrySet())
        {
            if (i == traderList.entrySet().size() - 1) {
                gbc.weighty = 1;
            }
            gbc.gridy = i++;
            traders.add(entry.getValue(), gbc);
        }
        this.window.repaint();
        window.pack();
    }

    public void resetTraders()
    {
        traderList.clear();
        traders.removeAll();
    }

    public void close()
    {
        window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
    }
}
