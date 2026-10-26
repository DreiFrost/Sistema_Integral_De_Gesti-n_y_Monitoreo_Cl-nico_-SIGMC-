package servicio;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class seguridad {
    public static String hash(String clave) {
        try {
            byte[] sal = new byte[16];
            new SecureRandom().nextBytes(sal);
            return Base64.getEncoder().encodeToString(sal) + ":" + derivar(clave, sal);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static String derivar(String clave, byte[] sal) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(clave.toCharArray(), sal, 120000, 256);
        try {
            return Base64.getEncoder()
                    .encodeToString(
                            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                                    .generateSecret(spec)
                                    .getEncoded());
        } finally {
            spec.clearPassword();
        }
    }

    public static boolean verificar(String clave, String almacenado) {
        try {
            String[] v = almacenado.split(":", 2);
            return v.length == 2
                    && MessageDigest.isEqual(
                            Base64.getDecoder().decode(v[1]),
                            Base64.getDecoder()
                                    .decode(derivar(clave, Base64.getDecoder().decode(v[0]))));
        } catch (Exception e) {
            return false;
        }
    }
}
