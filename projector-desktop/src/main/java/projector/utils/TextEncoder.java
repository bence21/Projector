package projector.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.Base64;

public class TextEncoder {
    private static final Logger LOG = LoggerFactory.getLogger(TextEncoder.class);
    private static final String SECRET_KEY = "Jesus Loves You";
    private static final String INIT_VECTOR = "encryptionIntVec";
    private static final String AES_CBC_PKCS_5_PADDING = "AES/CBC/PKCS5PADDING";

    private static SecretKey generateKey(String password) {
        try {
            byte[] salt = {0, 1, 2, 3, 4, 5, 6, 7}; // the salt for the key derivation function
            int iterations = 1000; // the number of iterations for the key derivation function
            int keyLength = 128; // the desired key length in bits
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLength);
            SecretKey tmp = factory.generateSecret(spec);
            return new SecretKeySpec(tmp.getEncoded(), "AES");
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    public static String encode(String text) {
        try {
            IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes(StandardCharsets.UTF_8));
            Cipher cipher = Cipher.getInstance(AES_CBC_PKCS_5_PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), iv);
            byte[] encrypted = cipher.doFinal(text.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    private static SecretKey getSecretKey() {
        return generateKey(SECRET_KEY + getUniqueKey());
    }

    private static String getUniqueKey() {
        if (!AppProperties.getInstance().isMacOs()) {
            return getMacAddress();
        } else {
            return getUniqueIdentifierForMac();
        }
    }

    private static String getUniqueIdentifierForMac() {
        String computerName = System.getProperty("user.name");
        String osVersion = System.getProperty("os.version");
        return computerName + osVersion;
    }

    private static String getMacAddress() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            byte[] mac = network.getHardwareAddress();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
            return sb.toString();
        } catch (UnknownHostException | SocketException e) {
            LOG.error(e.getMessage(), e);
        }
        return "";
    }

    public static String decode(String encodedText) {
        if (encodedText == null) {
            return null;
        }
        try {
            IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes(StandardCharsets.UTF_8));
            Cipher cipher = Cipher.getInstance(AES_CBC_PKCS_5_PADDING);
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), iv);
            byte[] decoded = Base64.getDecoder().decode(encodedText);
            return new String(cipher.doFinal(decoded));
        } catch (BadPaddingException ignored) {
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }
}
