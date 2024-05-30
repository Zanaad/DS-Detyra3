import java.io.*;
import java.net.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class RSAKeyDistributionClient {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try {
            // Generate client's RSA key pair
            KeyPair clientKeyPair = KeyGeneratorUtil.generateRSAKeyPair();

            // Connect to the server
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            System.out.println("Welcome to the Symmetric Key Distribution Client.");

            // Send client's public key to the server
            out.writeObject(clientKeyPair.getPublic());

            // Receive encrypted symmetric key from server
            byte[] encryptedSymmetricKey = (byte[]) in.readObject();

            // Decrypt the symmetric key with client's private key
            byte[] symmetricKeyBytes = EncryptionUtil.decryptRSA(encryptedSymmetricKey, clientKeyPair.getPrivate());
            SecretKey symmetricKey = new SecretKeySpec(symmetricKeyBytes, "AES");
            System.out.println("Symmetric key received and successfully decrypted using RSA.");

            // Get message from the user
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Please enter the message to encrypt with symmetric key: ");
            String message = reader.readLine();

            // Encrypt the message with the symmetric key
            byte[] encryptedMessage = EncryptionUtil.encryptAES(message.getBytes(), symmetricKey);
            String encodedEncryptedMessage = Base64.getEncoder().encodeToString(encryptedMessage);
            System.out.println("Encrypted message (Base64 encoded): " + encodedEncryptedMessage);

            // Send the encrypted message to the server
            out.writeObject(encryptedMessage);
            System.out.println("Message encrypted with symmetric key and sent to server.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
