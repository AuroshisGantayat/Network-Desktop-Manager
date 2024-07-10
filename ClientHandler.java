import java.io.*;
import java.net.*;
import java.awt.image.BufferedImage;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;

import javax.imageio.ImageIO;
public class ClientHandler implements Runnable { //Created to handle the communication for client
    private Socket clientSocket;
    private BufferedReader reader;
    private PrintWriter writer;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
       

        try {
             clientSocket.setSoTimeout(100000);
            this.reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.writer = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                // Read client messages
                String clientMessage = reader.readLine();
                
                if (clientMessage == null || "exit".equalsIgnoreCase(clientMessage)) {
                    break;
                }
                if(clientMessage!=null){
                    
                
                System.out.println(clientMessage);
                }
                switch (clientMessage) {
                    case "Help":
                        System.out.println("Recieved Help Message from client "+clientSocket.getInetAddress());
                        helpFolder();
                        break;
                    case "StartDesktopSharing":
                        startDesktopSharing();
                        break;
                    case "File":
                        receiveFile();
                        break;
                    default:
                        // Handle other messages as needed
                        break;
                }

                // Send a response back to the client
                writer.println("Server received: " + clientMessage);
            }

            // Close resources
            reader.close();
            writer.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void startDesktopSharing() {
        System.out.println("dektop shairing");
        try{
            sendDesktopScreenshots(clientSocket);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    private static void sendDesktopScreenshots(Socket clientSocket) throws Exception {
       DataOutputStream os= new DataOutputStream(clientSocket.getOutputStream());
       Robot robot = new Robot();
            while (true) {
                BufferedImage screenshot = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(screenshot, "png",baos);
                if(screenshot!=null){
                    byte[] imageBytes = baos.toByteArray();
                os.writeInt(imageBytes.length);
                os.write(imageBytes);
                Thread.sleep(100);
                }
                else{
                    System.err.println("Null is sending");
                }
                
            }
        }
    

        private void receiveFile() {
            try (InputStream inputStream = clientSocket.getInputStream();
                 
                 FileOutputStream fileOutputStream = new FileOutputStream("C:\\Users\\khand\\Desktop\\CN project\\received.txt")) {
        
                // Verify the existence of directories
                File directory = new File("C:\\Users\\khand\\Desktop\\CN project");
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                byte[] fileSizeBytes = new byte[8];
                inputStream.read(fileSizeBytes);
                long fileSize = bytesToLong(fileSizeBytes);
                System.out.println("Receiving file of size " + fileSize + " bytes");
        
                byte[] buffer = new byte[1024];
                int bytesRead;
                long totalBytesRead = 0;
        
                while ((bytesRead = inputStream.read(buffer)) > 0) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
        
                    if (totalBytesRead >= fileSize) {
                        break;
                    }
                }
        
                System.out.println("File received successfully");
        
            } catch (IOException e) {
                e.printStackTrace();
            } 
            catch (Exception e){
                e.printStackTrace();
            }
        }
        
     private static long bytesToLong(byte[] bytes) {
        return ((long) bytes[0] << 56)
                | ((long) bytes[1] & 0xFF) << 48
                | ((long) bytes[2] & 0xFF) << 40
                | ((long) bytes[3] & 0xFF) << 32
                | ((long) bytes[4] & 0xFF) << 24
                | ((long) bytes[5] & 0xFF) << 16
                | ((long) bytes[6] & 0xFF) << 8
                | (bytes[7] & 0xFF);
    }
    private void helpFolder(){
        File directory = new File("C:\\Users\\khand\\Desktop\\CN project\\Help Folder");
                if (!directory.exists()) {
                    directory.mkdirs();
                }
    }
}
