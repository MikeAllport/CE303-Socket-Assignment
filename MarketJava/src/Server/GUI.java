package Server;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;

public class GUI {
    private JPanel traders, broadcast;
    private JFrame window;
    private ServerProgram program;
    private HashMap<String, JLabel> traderList;

    public GUI()
    {
        this.window = new JFrame("Server");
        this.broadcast = new JPanel();
        this.traders = new JPanel();
        traderList = new HashMap<>();
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
        Dimension dimension = new Dimension(650, 500);
        JPanel container = new JPanel();
        setPanel(this.broadcast, container, "Server Messages", dimension);
        return container;
    }

    private JPanel getTradersPanel()
    {
        Dimension dimension = new Dimension(150, 500);
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
        toSetup.setLayout(new BoxLayout(toSetup, BoxLayout.Y_AXIS));
    }


    public synchronized void addTrader(String id)
    {
        JLabel trader = new JLabel(id);
        this.traders.add(trader);
        this.traderList.put(id, trader);
        this.window.repaint();
        window.pack();
    }

    public synchronized void addMessage(String message)
    {
        this.broadcast.add(new JLabel(message));
        this.window.repaint();
        window.pack();
    }

    public synchronized void removeTrader(String id)
    {
        this.traders.remove(traderList.get(id));
        this.traderList.remove(id);
        this.window.repaint();
        window.pack();
    }
}
