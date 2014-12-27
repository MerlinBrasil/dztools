package com.jforex.dzplugin.provider;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.dukascopy.api.system.IClient;

import com.jforex.dzplugin.ZorroLogger;
import com.jforex.dzplugin.config.Configuration;

// Code from http://www.dukascopy.com/wiki/#JForex_SDK_LIVE_mode
public class MainPin {

    private final IClient client;
    private static JFrame noParentFrame = null;

    public MainPin(IClient client) {
        this.client = client;
    }

    public String getPin() {
        PinDialog pd = null;
        try {
            pd = new PinDialog();
        } catch (Exception e) {
            ZorroLogger.log("getPin exc: " + e.getMessage());
        }
        return pd.pinfield.getText();
    }

    @SuppressWarnings("serial")
    private class PinDialog extends JDialog {

        private final JTextField pinfield = new JTextField();

        public PinDialog() throws Exception {
            super(noParentFrame, "PIN Dialog", true);

            JPanel captchaPanel = new JPanel();
            captchaPanel.setLayout(new BoxLayout(captchaPanel, BoxLayout.Y_AXIS));

            final JLabel captchaImage = new JLabel();
            captchaImage.setIcon(new ImageIcon(client.getCaptchaImage(Configuration.connectURLForLIVE)));
            captchaPanel.add(captchaImage);

            captchaPanel.add(pinfield);
            getContentPane().add(captchaPanel);

            JPanel buttonPane = new JPanel();

            JButton btnLogin = new JButton("Login");
            buttonPane.add(btnLogin);
            btnLogin.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    setVisible(false);
                    dispose();
                }
            });

            JButton btnReload = new JButton("Reload");
            buttonPane.add(btnReload);
            btnReload.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        captchaImage.setIcon(new ImageIcon(client.getCaptchaImage(Configuration.connectURLForLIVE)));
                    } catch (Exception ex) {
                        ZorroLogger.log("getPin exc: " + ex.getMessage());
                    }
                }
            });
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            pack();
            setVisible(true);
        }
    }
}
