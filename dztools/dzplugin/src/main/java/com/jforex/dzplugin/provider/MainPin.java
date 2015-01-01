package com.jforex.dzplugin.provider;

/*
 * #%L
 * dzplugin
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 juxeii
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.system.IClient;
import com.jforex.dzplugin.ZorroLogger;
import com.jforex.dzplugin.config.DZPluginConfig;

// Code from http://www.dukascopy.com/wiki/#JForex_SDK_LIVE_mode
public class MainPin {

    private final IClient client;
    private static JFrame noParentFrame = null;
    private final DZPluginConfig pluginConfig = ConfigFactory.create(DZPluginConfig.class);

    private final static Logger logger = LogManager.getLogger(MainPin.class);

    public MainPin(IClient client) {
        this.client = client;
    }

    public String getPin() {
        PinDialog pd = null;
        try {
            pd = new PinDialog();
        } catch (Exception e) {
            ZorroLogger.indicateError(logger, "getPin exc: " + e.getMessage());
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
            captchaImage.setIcon(new ImageIcon(client.getCaptchaImage(pluginConfig.CONNECT_URL_LIVE())));
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
                        captchaImage.setIcon(new ImageIcon(client.getCaptchaImage(pluginConfig.CONNECT_URL_LIVE())));
                    } catch (Exception ex) {
                        ZorroLogger.indicateError(logger, "getPin exc: " + ex.getMessage());
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
