import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;

public class ClientGUI extends JFrame {
    private BufferedReader reader;
    private PrintWriter writer;
    private Socket socket;  // Add a Socket field to keep track of the client socket
    private JLabel screenLabel;
    public ClientGUI(BufferedReader reader, PrintWriter writer, Socket socket) {
        this.reader = reader;
        this.writer = writer;
        this.socket = socket;
        initUI();
    }

    private void initUI() {
        setTitle("Network Desktop Manager - Client");
        setSize(800, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JButton requestHelpButton = new JButton("Request Help");//It will create a button with the given text
        requestHelpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               try {
                        writer.println("Help");
                        writer.flush();
                        System.out.printf("Message"+reader.readLine());
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
            }
        });
        JButton desktopShareButton = new JButton("Start Desktop Sharing");
        desktopShareButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                 try {
                        writer.println("StartDesktopSharing");
                        writer.flush();
                        System.out.printf("Message"+reader.readLine());
                        DesktopSharingClient();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
            }
        });

        JButton fileShareButton = new JButton("Send File");
        fileShareButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(null);

                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    try {
                        writer.println("File");
                        writer.flush();
                        sendFile(selectedFile);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        getContentPane().setLayout(new FlowLayout());
        getContentPane().add(requestHelpButton);
        getContentPane().add(desktopShareButton);
        getContentPane().add(fileShareButton);

        setLocationRelativeTo(null);
        setVisible(true);
    }
    private void sendFile(File file) throws Exception {
        int bytes = 0;
    
        // Send a header indicating the start of file transfer
         writer.println(file.length());
        writer.flush();
        FileInputStream fileInputStream = new FileInputStream(file);
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
    
        dataOutputStream.writeLong(file.length());
        dataOutputStream.flush();
    
        byte[] buffer = new byte[4 * 1024];
        while ((bytes = fileInputStream.read(buffer)) != -1) {
            dataOutputStream.write(buffer, 0, bytes);
        }
    
    
        System.out.println("File Sent");
        fileInputStream.close();
        dataOutputStream.close();
    }
    public void DesktopSharingClient() {
        screenLabel = new JLabel();
        add(screenLabel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setVisible(true);

        connectToServer();
    }

    private void connectToServer() {
        try {
           DataInputStream inputStream = new DataInputStream(socket.getInputStream());

            while (true) {
                int imageSize = inputStream.readInt();
                if (imageSize < 0) {
                    System.err.println("Received negative image size from the server.");
                    continue; // Skip processing this iteration
                }
                byte[] imageBytes = new byte[imageSize];
                inputStream.readFully(imageBytes);
                try {
                    BufferedImage screenshot = ImageIO.read(new ByteArrayInputStream(imageBytes));
                    if (screenshot != null) {
                        ImageIcon icon = new ImageIcon(screenshot);
                        screenLabel.setIcon(icon);
                    } else {
                        System.err.println("Received null image from the server.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
                
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    // Add a method to close the socket and associated stream
}
