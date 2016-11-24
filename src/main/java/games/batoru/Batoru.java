/*
 * Created on Sep 30, 2003
 */
package games.batoru;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.List;

import javax.swing.*;

import games.batoru.server.Server;
import games.batoru.client.ClientView3d;

/**
 * @author Tako
 */
public class Batoru {
	private static JFrame m_frame;
	private static Server m_server;
	private static List<ClientView3d> m_clients;

	public static void main(String[] args) {
		m_clients = new LinkedList<ClientView3d>();
		
		m_frame = new JFrame("Batoru");
		Container pane = m_frame.getContentPane();
		pane.setLayout(new GridLayout(3, 1));

		JButton serverButton = new JButton("Start Server");
		pane.add(serverButton);
		serverButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				toggleServer((JButton)e.getSource());
			}
		});

		JButton clientButton = new JButton("Start Client");
		pane.add(clientButton);
		clientButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startClient();
			}
		});

		JButton exitButton = new JButton("Exit");
		pane.add(exitButton);
		exitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exitApplication();
			}
		});

		m_frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				exitApplication();
			}
		});

		m_frame.setSize(100, 150);
		m_frame.setVisible(true);
	}
	
	protected static void toggleServer(JButton _button) {
		if (m_server == null) {
			m_server = new Server();
			m_server.start();
			_button.setText("Stop server");
		} else {
			m_server.stop();
			m_server = null;
			_button.setText("Start server");
		}
	}
	
	protected static void startClient() {
		ClientView3d client = new ClientView3d("Batoru Client", false);
		client.start();
		m_clients.add(client);
	}
	
	protected static void exitApplication() {
		for (ClientView3d client : m_clients) {
			client.stop();
		}
		if (m_server != null) {
			m_server.stop();
			m_server = null;
		}
		m_frame.dispose();
		m_frame = null;
		System.exit(0);
	}
}
