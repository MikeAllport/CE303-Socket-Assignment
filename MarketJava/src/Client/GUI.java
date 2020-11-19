package Client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

public class GUI extends Server.GUI {
    private final static String ID_PREFIX = "Your ID: ";
    private final static String STOCK_HOLDER_PREFIX = "Stock Holder: ";
    JTextField toTradeWith;
    JLabel traderID = new JLabel(ID_PREFIX);
    JLabel stockHolder = new JLabel(STOCK_HOLDER_PREFIX);

    public GUI(String title)
    {
        super(title);
        this.toTradeWith = new JTextField();
        this.toTradeWith.setPreferredSize(new Dimension(100, Server.GUI.CELL_HEIGHT));
        this.window.setSize(new Dimension(Server.GUI.WINDOW_WIDTH, Server.GUI.WINDOW_HEIGHT+100));
        initButs();
        initID();
        this.window.pack();
    }

    protected void setStockHolder(String id)
    {
        this.stockHolder.setText(STOCK_HOLDER_PREFIX + id);
    }

    protected void setTraderID(String id)
    {
        this.traderID.setText(ID_PREFIX + id);
    }

    private void initID()
    {
        JPanel idPanel = new JPanel(new GridLayout(0, 2));
        idPanel.setPreferredSize(new Dimension(Server.GUI.WINDOW_WIDTH, 50));
        Font font = new Font("", Font.BOLD, 15);
        this.traderID.setFont(font);
        this.stockHolder.setFont(font);
        this.traderID.setHorizontalAlignment(JLabel.CENTER);
        this.stockHolder.setHorizontalAlignment(JLabel.CENTER);
        idPanel.add(traderID);
        idPanel.add(stockHolder);
        this.window.add(idPanel, BorderLayout.NORTH);
    }

    private void initButs()
    {
        JPanel buts = new JPanel();
        buts.setLayout(new GridLayout(0, 2));
        JPanel innerToTrade = new JPanel();
        innerToTrade.setLayout(new GridBagLayout());
        GridBagConstraints gbc = getConst();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridx = 0;
        innerToTrade.add(new JLabel("TradeWith: "), gbc);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 1;
        innerToTrade.add(this.toTradeWith, gbc);
        buts.add(innerToTrade);
        JButton trade = new JButton("Trade");
        trade.addActionListener((ActionEvent e) ->
                ClientProgram.handler.trade(toTradeWith.getText())
        );
        buts.setBorder(new EmptyBorder(5, 0, 5, 10));
        buts.add(trade);
        buts.setPreferredSize(new Dimension(Server.GUI.WINDOW_WIDTH, 50));
        this.window.add(buts, BorderLayout.SOUTH);
    }

    public void addMessageError(String message)
    {
        JLabel messageLabel = new JLabel(message);
        messageLabel.setForeground(new Color(200, 50, 50));
        messageLabel.setPreferredSize(new Dimension(Server.GUI.BROADCAST_PANEL_WIDTH, Server.GUI.CELL_HEIGHT));
        messageLabel.setBorder(new EmptyBorder(0, 5, 0, 0));
        addMessageToPanel(messageLabel);
    }

    @Override
    protected void addButtonToList(JButton but, String id) {
        but.setFocusPainted(true);
        but.setContentAreaFilled(true);
        but.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JButton eBut = (JButton) e.getSource();
                toTradeWith.setText(eBut.getText());
            }
        });
        super.addButtonToList(but, id);
    }
}
