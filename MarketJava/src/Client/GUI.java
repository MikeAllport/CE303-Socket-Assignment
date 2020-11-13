package Client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

public class GUI extends Server.GUI {
    JButton activeButton;

    public GUI(String title)
    {
        super(title);
    }

    public void addMessageError(String message)
    {
        GridBagConstraints gbc = getConst();
        JLabel messageLabel = new JLabel(message);
        messageLabel.setForeground(new Color(200, 50, 50));
        messageLabel.setPreferredSize(new Dimension(Server.GUI.BROADCAST_PANEL_WIDTH, Server.GUI.CELL_HEIGHT));
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

    @Override
    protected void addButtonToList(JButton but, String id) {
        but.setFocusPainted(true);
        but.setContentAreaFilled(true);
        but.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (Map.Entry<String, JButton> entry: traderList.entrySet())
                    entry.getValue().setBackground(new Color(255, 255, 255));
                but.setBackground(Color.RED);
                System.out.println("clicked");
            }
        });
        super.addButtonToList(but, id);
    }
}
