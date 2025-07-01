package org.chloyka.wp;

import org.keycloak.credential.hash.PasswordHashProvider;
import org.keycloak.models.PasswordPolicy;
import org.keycloak.models.credential.PasswordCredentialModel;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class WpPasswordHashProvider implements PasswordHashProvider {
    private final String providerId;
    private final String itoa64 = "./0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public WpPasswordHashProvider(String providerId) {
        this.providerId = providerId;
    }

    @Override
    public boolean policyCheck(PasswordPolicy policy, PasswordCredentialModel credential) {
        return true;
    }

    @Override
    public PasswordCredentialModel encodedCredential(String rawPassword, int iterations) {
        return null;
    }

    @Override
    public String encode(String rawPassword, int iterations) {
        return null;
    }

    @Override
    public void close() {
    }

    @Override
    public boolean verify(String rawPassword, PasswordCredentialModel credential) {
        final String storedHash = credential.getPasswordSecretData().getValue();

        return checkPassword(rawPassword, storedHash);
    }

    private boolean checkPassword(String password, String storedHash) {
        if (password == null || password.isEmpty() || storedHash == null || storedHash.isEmpty()) {
            return false;
        }

        if (storedHash.length() == 32 && storedHash.matches("^[a-f0-9]{32}$")) {
            return md5(password).equals(storedHash);
        }

        if (storedHash.length() != 34 || !storedHash.startsWith("$P$B")) {
            return false;
        }

        int countLog2 = itoa64.indexOf(storedHash.charAt(3));
        if (countLog2 < 7 || countLog2 > 30) {
            return false;
        }

        String salt = storedHash.substring(4, 12);
        if (salt.length() != 8) {
            return false;
        }

        int count = 1 << countLog2;

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            byte[] hash = md.digest((salt + password).getBytes());
            for (int i = 0; i < count; i++) {
                hash = md.digest(concat(hash, password.getBytes()));
            }

            String newHash = encode64(hash, 16);

            return newHash.equals(storedHash.substring(12));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();

            return false;
        }
    }

    private String md5(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            byte[] bytes = md.digest(s.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 not supported", e);
        }
    }

    private byte[] concat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];

        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);

        return result;
    }

    private String encode64(byte[] src, int count) {
        StringBuilder sb = new StringBuilder();

        int i = 0;

        do {
            int value = src[i++] & 0xff;

            sb.append(itoa64.charAt(value & 0x3f));
            if (i < count) {
                value |= (src[i] & 0xff) << 8;
            }

            sb.append(itoa64.charAt((value >> 6) & 0x3f));
            if (i++ >= count) {
                break;
            }

            if (i < count) {
                value |= (src[i] & 0xff) << 16;
            }

            sb.append(itoa64.charAt((value >> 12) & 0x3f));
            if (i++ >= count) {
                break;
            }

            sb.append(itoa64.charAt((value >> 18) & 0x3f));
        } while (i < count);

        return sb.toString();
    }
}