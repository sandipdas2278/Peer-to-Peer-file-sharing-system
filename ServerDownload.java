import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerDownload extends Thread {
    int peerServerPort;
    String directoryPath = null;
    ServerSocket dwldServerSocket;
    Socket dwldSocket = null;

    ServerDownload(int peerServerPort, String directoryPath) {
        this.peerServerPort = peerServerPort;
        this.directoryPath = directoryPath;
    }

    public void run() {
        try {
            dwldServerSocket = new ServerSocket(peerServerPort);
            dwldSocket = dwldServerSocket.accept();
            new ServerDownloadThread(dwldSocket, directoryPath).start();
        } catch (IOException ex) {
            Logger.getLogger(ServerDownload.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

class ServerDownloadThread extends Thread {
    Socket dwldThreadSocket;
    String directoryPath;

    public ServerDownloadThread(Socket dwldThreadSocket, String directoryPath) {
        this.dwldThreadSocket = dwldThreadSocket;
        this.directoryPath = directoryPath;
    }

    @SuppressWarnings({ "unused", "resource" })
    public void run() {
        FileInputStream FIS = null;
        BufferedInputStream objBIS = null;
        try {
            ObjectOutputStream objOS = new ObjectOutputStream(dwldThreadSocket.getOutputStream());
            ObjectInputStream objIS = new ObjectInputStream(dwldThreadSocket.getInputStream());

            String fileName = (String) objIS.readObject();
            File myFile = new File(directoryPath + "//" + fileName);
            long length = myFile.length();

            byte[] byte_arr = new byte[(int) length];

            objOS.writeObject((int) myFile.length());
            objOS.flush();

            FIS = new FileInputStream(myFile);
            objBIS = new BufferedInputStream(FIS);

            int bytesRead = objBIS.read(byte_arr, 0, (int) myFile.length());
            if (bytesRead == -1) {
                System.out.println("End of stream reached or no bytes read");
            } else {
                System.out.println("Sending the file of " + bytesRead + " bytes");
                objOS.write(byte_arr, 0, bytesRead);
            }

            objOS.flush();
            dwldThreadSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (FIS != null) {
                try {
                    FIS.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (objBIS != null) {
                try {
                    objBIS.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}