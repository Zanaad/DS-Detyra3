import java.io.*;
import java.net.*;
import java.security.*;
import javax.crypto.*;

public class RSAKeyDistributionServer {

    private static final int PORT = 12345;
    private KeyPair rsaKeyPair;

    public RSAKeyDistributionServer() throws Exception {
        rsaKeyPair = KeyGeneratorUtil.generateRSAKeyPair();
    }

    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Starting Key Distribution Server...");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            new ClientHandler(clientSocket, rsaKeyPair).start();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket clientSocket;
        private KeyPair rsaKeyPair;

        public ClientHandler(Socket socket, KeyPair rsaKeyPair) {
            this.clientSocket = socket;
            this.rsaKeyPair = rsaKeyPair;
        }

        public void run() {
            try (ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {

                System.out.println("Awaiting client requests for symmetric keys...");

                // Send public key to client
                out.writeObject(rsaKeyPair.getPublic());

                // Generate a symmetric key (AES)
                SecretKey symmetricKey = KeyGeneratorUtil.generateAESKey();

                // Encrypt the symmetric key with RSA
                byte[] encryptedSymmetricKey = EncryptionUtil.encryptRSA(symmetricKey.getEncoded(), rsaKeyPair.getPrivate());

                // Send the encrypted symmetric key to the client
                out.writeObject(encryptedSymmetricKey);
                System.out.println("Encrypted symmetric key sent to client.");

                // Receive encrypted message from client
                byte[] encryptedMessage = (byte[]) in.readObject();

                // Decrypt the message with the symmetric key
                byte[] decryptedMessage = EncryptionUtil.decryptAES(encryptedMessage, symmetricKey);

                System.out.println("Client's decrypted message: " + new String(decryptedMessage));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        try {
            new RSAKeyDistributionServer().start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
