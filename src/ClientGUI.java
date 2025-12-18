import javax.media.Manager;
import javax.media.Player;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URL;
import java.util.Scanner;


public class ClientGUI {

    private JPanel message_log;
    private JScrollPane message_log_scroll;
    final File log_file = new File("log.txt");
    private client client;

    private JFrame pinnedMessagesFrame;
    private JPanel pinnedMessagesPanel;
    private JScrollPane pinnedScrollPane;

    private JFrame logMessagesFrame;
    private JPanel logMessagesPanel;
    private JScrollPane logScrollPane;

    // Store message components for later access
    private java.util.HashMap<String, JButton> pinButtons = new java.util.HashMap<>();

    public ClientGUI() {

        JFrame frame = new JFrame();
        JPanel panel = new JPanel(new GridLayout());

        JLabel port_label = new JLabel("Port:");
        JTextField port_field = new JTextField("5555");
        JLabel host_label = new JLabel("Host:");
        JTextField host_field = new JTextField("localhost");
        JLabel username_label = new JLabel("User:");
        JTextField username_field = new JTextField("a");
        JButton connect_button = new JButton("Connect");

        panel.add(port_label);
        panel.add(port_field);
        panel.add(host_label);
        panel.add(host_field);
        panel.add(username_label);
        panel.add(username_field);
        panel.add(connect_button);

        frame.add(panel);
        frame.pack();
        frame.setVisible(true);

        connect_button.addActionListener(e -> {

            try{
                int port = Integer.parseInt(port_field.getText());
                if(host_field.getText().isEmpty() || username_field.getText().isEmpty() || port_field.getText().isEmpty()) {
                    throw new Exception("not every thing is filled");
                }
                else{
                    frame.setVisible(false);
                    create_main_frame(Integer.parseInt(port_field.getText()), host_field.getText(), username_field.getText());
                    frame.dispose();
                }
            }
            catch(Exception ex){
                JDialog error_dialog = new JDialog();
                error_dialog.setTitle("Error");
                JLabel error_label = new JLabel("Please fill in everything");
                error_dialog.add(error_label);
                error_dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
                error_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                error_dialog.setLocationRelativeTo(null);
                error_dialog.setVisible(true);

            }


        });

    }

    public void create_main_frame(int port, String host, String username) {
        JFrame frame = new JFrame("Chat Application");
        JPanel main_panel = new JPanel(new BorderLayout());
        frame.setSize(500, 300);

        //side panel
        JPanel side_panel = new JPanel();
        side_panel.setLayout(new BoxLayout(side_panel, BoxLayout.Y_AXIS));

        JButton pinned = new JButton("Pinned Messages");
        side_panel.add(pinned);
        main_panel.add(side_panel, BorderLayout.WEST);

        JButton groupChatsButton = new JButton("Group Chats");
        JButton returnToGlobalButton = new JButton("Return to global chatroom");
        JButton videoStreamButton = new JButton("Test Camera ");
        JButton cameraStreamButton = new JButton("Host Camera Stream");
        JButton cameraReceive = new JButton("Join Camera Receive");
        JButton get_ip = new JButton("Get IP Address");
        JButton logButton = new JButton("Log");
       
        side_panel.add(groupChatsButton);
        side_panel.add(returnToGlobalButton);
        side_panel.add(videoStreamButton);
        side_panel.add(cameraStreamButton);
        side_panel.add(cameraReceive);
        side_panel.add(get_ip);
        side_panel.add(logButton);

        logButton.addActionListener(e -> {
            client.getMessage("/log");
        });


        returnToGlobalButton.addActionListener(e -> {
            client.getMessage("/leave");
        });

        get_ip.addActionListener(e -> {
            try (final DatagramSocket datagramSocket = new DatagramSocket()) {
                datagramSocket.connect(InetAddress.getByName("8.8.8.8"), 5777);
                JDialog ip_popup = new JDialog();
                ip_popup.setSize(150, 100);
                ip_popup.setTitle("IP Address");
                ip_popup.add(new Label(datagramSocket.getLocalAddress().getHostAddress()), BorderLayout.CENTER);
                ip_popup.setVisible(true);
                ip_popup.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                datagramSocket.close();
            } catch (Exception ex) {
                System.out.println(ex);
            }

        });

       videoStreamButton.addActionListener(e -> {
           Thread thread = new Thread(() -> {
               CameraStreaming cameraStreaming = new CameraStreaming();
           });
           thread.start();
       });
       cameraStreamButton.addActionListener(e -> {
           JFrame window = new JFrame();
           JPanel jpanel = new JPanel();
           jpanel.setLayout(new BoxLayout(jpanel, BoxLayout.Y_AXIS));
           JTextField ip_input = new JTextField("127.0.0.1");
           JButton connect_button = new JButton("Connect");
           jpanel.add(ip_input);
           jpanel.add(connect_button);
           window.add(jpanel);
           window.pack();
           window.setVisible(true);

           connect_button.addActionListener(ev -> {
               window.setVisible(false);
               CameraStreamClient cameraStreamClient = new CameraStreamClient(5777, ip_input.getText());
               Thread thread = new Thread(cameraStreamClient);
               thread.start();

               CameraStreamServer cameraStreamServer = new CameraStreamServer(5778);
               Thread thread2 = new Thread(cameraStreamServer);
               thread2.start();
               window.dispose();

           });


       });

       cameraReceive.addActionListener(e -> {
           JFrame window = new JFrame();
           JPanel jPanel = new JPanel();
           jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));
           JTextField ip_input = new JTextField("127.0.0.1");
           JButton connect_button = new JButton("Connect");
           jPanel.add(ip_input);
           jPanel.add(connect_button);
           window.add(jPanel);
           window.pack();
           window.setVisible(true);
           connect_button.addActionListener(ev -> {
               window.setVisible(false);
               CameraStreamClient cameraStreamClient = new CameraStreamClient(5778, ip_input.getText());
               Thread thread = new Thread(cameraStreamClient);
               thread.start();

               CameraStreamServer cameraStreamServer = new CameraStreamServer(5777);
               Thread thread2 = new Thread(cameraStreamServer);
               thread2.start();
               window.dispose();
           });
       });

        //messages
        message_log = new JPanel();
        message_log.setLayout(new BoxLayout(message_log, BoxLayout.Y_AXIS));
        message_log_scroll = new JScrollPane(message_log);

        //read_from_log();

        main_panel.add(message_log_scroll, BorderLayout.CENTER);

        //input
        JPanel input_panel = new JPanel(new BorderLayout());

        JTextField inputBox = new JTextField();
        input_panel.add(inputBox, BorderLayout.CENTER);

        JButton send_button = new JButton("Send");
        input_panel.add(send_button, BorderLayout.EAST);
        main_panel.add(input_panel, BorderLayout.SOUTH);

        JButton uploadButton = new JButton("Send File");
        input_panel.add(uploadButton, BorderLayout.WEST);

        frame.add(main_panel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        client = new client(host, port, username, this);
        Thread thread = new Thread(client);
        thread.start();

        send_button.addActionListener(e -> {
            if (!inputBox.getText().isEmpty()) {
                String message = username + ": " + inputBox.getText();
                new_message_in(message);
                new_message_out(message);
                inputBox.setText("");
            }
        });

        pinned.addActionListener(e -> {
            client.getMessage("SHOW_PINNED");
        });

        uploadButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select a file to send");
            int result = fileChooser.showOpenDialog(frame);

            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    client.sendFile(fileChooser.getSelectedFile());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame,
                            "Error sending file: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        
        groupChatsButton.addActionListener(e -> {

	
            //List<String> groupChats = client.sendMessage("/list");
            JFrame groupChatFrame = new JFrame("Group Chattery");
            JPanel groupChatPanel = new JPanel();
            groupChatPanel.setLayout(new BoxLayout(groupChatPanel, BoxLayout.Y_AXIS));
            JLabel label = new JLabel("Available groupies: ");
            groupChatPanel.add(label);
            DefaultListModel<String> chatListModel = new DefaultListModel<>();
            chatListModel.addElement("Group 1");
            chatListModel.addElement("Group 2");
            JList<String> chatList = new JList<>(chatListModel);
            JScrollPane chatScrollPane = new JScrollPane(chatList);
            groupChatPanel.add(chatScrollPane);
            JButton joinButton = new JButton("Join selected chat");
            groupChatPanel.add(joinButton);
            groupChatFrame.add(groupChatPanel);
            groupChatFrame.setSize(400,300);

            joinButton.addActionListener(v -> {
                String selectedChat = chatList.getSelectedValue();
                if (selectedChat != null) {
                    String joinCommand = "/join " + selectedChat;
                    client.getMessage(joinCommand);
                    groupChatFrame.dispose();
                }
            });

            groupChatFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

	

            groupChatFrame.setLocationRelativeTo(null);

	

            groupChatFrame.setVisible(true);

        });


        
    }


    public void read_from_log() {
        synchronized (log_file){
            try {
                Scanner scanner = new Scanner(log_file);
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    JLabel message = new JLabel(line);
                    message_log.add(message);
                }
                scanner.close();
            }
            catch (FileNotFoundException e){
                System.out.println("File not found");
            }
        }

    }
    public void new_message_in(String message) {

        try {
            if (message.startsWith("PINNED_LIST:")) {
                // Handle pinned messages list
                String[] pinnedMessages = message.substring("PINNED_LIST:".length()).split("\\|");
                showPinnedMessages(pinnedMessages);
                return;
            }

            if (message.startsWith("LOG_LIST:")) {
                String[] logMessages = message.substring("LOG_LIST:".length()).split("\\|");
                showLogMessages(logMessages);
                return;
            }

            if (message.startsWith("DISABLE_PIN:")) {
                // Handle disable pin message
                String originalMessage = message.substring("DISABLE_PIN:".length());
                JButton pinButton = pinButtons.get(originalMessage);
                if (pinButton != null) {
                    pinButton.setEnabled(false);
                }
                return;
            }

            if (message.startsWith("File available:")) {
                // Extract filename from "File available: filename from username"
                String[] parts = message.split(" from ");
                String fileName = parts[0].substring("File available: ".length());

                JPanel filePanel = new JPanel();
                filePanel.setLayout(new BorderLayout());

                JLabel fileLabel = new JLabel("File: " + fileName);
                JButton downloadButton = new JButton("Download");

                downloadButton.addActionListener(e -> {
                    try {
                        client.receiveFile(fileName);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null,
                                "Error initiating download: " + ex.getMessage(),
                                "Download Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });

                filePanel.add(fileLabel, BorderLayout.WEST);
                filePanel.add(downloadButton, BorderLayout.EAST);
                message_log.add(filePanel);
            } else {
                // Create a panel for the message and pin button
                JPanel messagePanel = new JPanel(new BorderLayout());
                messagePanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

                // Add the message label
                JLabel messageLabel = new JLabel(message);
                messagePanel.add(messageLabel, BorderLayout.CENTER);

                // Don't add pin button for "Pinned:" messages
                if (!message.startsWith("Pinned:")) {
                    // Add the pin button
                    JButton pinButton = new JButton("Pin");
                    pinButton.addActionListener(e -> {
                        client.getMessage("PIN:" + message);
                        // send message to disable pin buttons on all clients
                        client.getMessage("DISABLE_PIN:" + message);
                        pinButton.setEnabled(false);
                    });
                    messagePanel.add(pinButton, BorderLayout.EAST);

                    // Store the pin button for later access
                    pinButtons.put(message, pinButton);
                }

                message_log.add(messagePanel);

                // Add download button if it's a file notification
                if (message.contains("has shared a file:")) {
                    String fileName = message.substring(message.lastIndexOf(":") + 2);
                    JButton downloadButton = new JButton("Download");
                    downloadButton.addActionListener(e -> client.receiveFile(fileName));
                    message_log.add(downloadButton);
                }

                message_log.revalidate();
                message_log.repaint();

                // Auto-scroll to bottom
                JScrollBar vertical = message_log_scroll.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }

    }

    public void new_message_out(String message) {
        client.getMessage(message);
    }

    private void showPinnedMessages(String[] messages) {
        if (pinnedMessagesFrame == null) {
            pinnedMessagesFrame = new JFrame("Pinned Messages");
            pinnedMessagesFrame.setSize(400, 300);
            pinnedMessagesFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            pinnedMessagesPanel = new JPanel();
            pinnedMessagesPanel.setLayout(new BoxLayout(pinnedMessagesPanel, BoxLayout.Y_AXIS));
            pinnedScrollPane = new JScrollPane(pinnedMessagesPanel);
            pinnedMessagesFrame.add(pinnedScrollPane);
        }

        // Clear existing messages
        pinnedMessagesPanel.removeAll();

        // Add each pinned message
        if (messages.length == 0 || (messages.length == 1 && messages[0].isEmpty())) {
            JLabel noMessagesLabel = new JLabel("No pinned messages yet");
            noMessagesLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            pinnedMessagesPanel.add(noMessagesLabel);
        } else {
            for (String message : messages) {
                if (!message.isEmpty()) {
                    JLabel messageLabel = new JLabel(message);
                    messageLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                    pinnedMessagesPanel.add(messageLabel);
                }
            }
        }

        // Add some padding at the bottom
        pinnedMessagesPanel.add(Box.createVerticalGlue());

        // Refresh the panel
        pinnedMessagesPanel.revalidate();
        pinnedMessagesPanel.repaint();

        // Show the frame if not visible
        if (!pinnedMessagesFrame.isVisible()) {
            pinnedMessagesFrame.setLocationRelativeTo(null);
            pinnedMessagesFrame.setVisible(true);
        }
    }

    private void showLogMessages(String[] messages) {
        if (logMessagesFrame == null) {
            logMessagesFrame = new JFrame("Message Log");
            logMessagesFrame.setSize(400, 300);
            logMessagesFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            logMessagesPanel = new JPanel();
            logMessagesPanel.setLayout(new BoxLayout(logMessagesPanel, BoxLayout.Y_AXIS));
            logScrollPane = new JScrollPane(logMessagesPanel);
            logMessagesFrame.add(logScrollPane);
        }

        // Clear existing messages
        logMessagesPanel.removeAll();

        // Add each log message
        if (messages.length == 0 || (messages.length == 1 && messages[0].isEmpty())) {
            JLabel noMessagesLabel = new JLabel("No log messages yet");
            noMessagesLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            logMessagesPanel.add(noMessagesLabel);
        } else {
            for (String message : messages) {
                if (!message.isEmpty()) {
                    JLabel messageLabel = new JLabel(message);
                    messageLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                    logMessagesPanel.add(messageLabel);
                }
            }
        }

        // Add some padding at the bottom
        logMessagesPanel.add(Box.createVerticalGlue());

        // Refresh the panel
        logMessagesPanel.revalidate();
        logMessagesPanel.repaint();

        // Show the frame if not visible
        if (!logMessagesFrame.isVisible()) {
            logMessagesFrame.setLocationRelativeTo(null);
            logMessagesFrame.setVisible(true);
        }
    }

}
