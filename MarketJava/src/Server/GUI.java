package Server;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.util.*;

public class GUI {
    private ServerProgram program;
    protected JFrame window;
    protected JPanel traders, broadcast;
    protected static final int BROADCAST_PANEL_WIDTH = 650;
    protected static final int BUT_PANEL_WIDTH = 150;
    protected static final int CELL_HEIGHT = 25;
    protected ArrayList<JLabel> messages;
    protected TreeMap<String, JButton> traderList;

    public GUI(String title)
    {
        this.messages = new ArrayList<>();
        this.window = new JFrame(title);
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
        this.window.setSize(new Dimension(800, 550));
        this.window.setResizable(false);
        this.window.setVisible(true);
        this.window.pack();
    }

    private JPanel getBroadcastContainer()
    {
        Dimension dimension = new Dimension(BROADCAST_PANEL_WIDTH, 500);
        JPanel container = new JPanel();
        setPanel(this.broadcast, container, "Server Messages", dimension);
        return container;
    }

    private JPanel getTradersPanel()
    {
        Dimension dimension = new Dimension(BUT_PANEL_WIDTH, 500);
        JPanel container = new JPanel();
        setPanel(this.traders, container, "Traders", dimension);
        return container;
    }

    private void setPanel(JPanel toSetup, JPanel container, String labelName, Dimension dimension)
    {
        JLabel label = new JLabel(labelName);
        label.setVerticalAlignment(SwingConstants.TOP);
        label.setBorder(new EmptyBorder(5, 5, 5, 5));
        container.setLayout(new BorderLayout());
        container.add(label, BorderLayout.NORTH);
        container.add(new JScrollPane(toSetup, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.SOUTH);
        toSetup.setAlignmentX(SwingConstants.LEFT);
        toSetup.setBackground(new Color(255, 255, 255));
        toSetup.setBackground(new Color(255, 255, 255));
        toSetup.setPreferredSize(new Dimension(dimension.width, dimension.height - label.getSize().height));
        toSetup.setLayout(new GridBagLayout());
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
        trader.setPreferredSize(new Dimension(GUI.BUT_PANEL_WIDTH, GUI.CELL_HEIGHT));
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
        GridBagConstraints gbc = getConst();
        JLabel messageLabel = new JLabel(message);
        messageLabel.setPreferredSize(new Dimension(GUI.BROADCAST_PANEL_WIDTH, GUI.CELL_HEIGHT));
        messageLabel.setBorder(new EmptyBorder(0, 5, 0, 0));
        this.messages.add(messageLabel);
        broadcast.removeAll();
        int i;
        for (i = 0; i < messages.size(); i++)
        {
            gbc.gridy = i;
            broadcast.add(messages.get(i), gbc);
        }
        gbc.weighty = 1;
        gbc.gridy = ++i;
        this.broadcast.add(messageLabel, gbc);
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

    public void close()
    {
        window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
    }
}
