package com.payroll.license;

import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
public class LicenseCryptoService {
    private final ObjectMapper mapper;
    private final PublicKey publicKey;
    private final byte[] payloadEncryptionKey;

    public LicenseCryptoService(ObjectMapper mapper, @Value("${license.public-key}") Resource keyResource,
                                @Value("${license.payload-encryption-key}") String encryptionKey) throws Exception {
        this.mapper = mapper;
        this.payloadEncryptionKey = Base64.getDecoder().decode(encryptionKey);
        String pem = new String(keyResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8)
                .replaceAll("-----BEGIN PUBLIC KEY-----|-----END PUBLIC KEY-----|\\s", "");
        this.publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(pem)));
    }

    public LicensePayload verifyAndRead(String base64License) {
        try {
            byte[] envelopeBytes = Base64.getMimeDecoder().decode(base64License.trim());
            LicenseEnvelope envelope = mapper.readValue(envelopeBytes, LicenseEnvelope.class);
            String signed = envelope.formatVersion() + "|" + envelope.keyId() + "|" + envelope.encrypted() + "|"
                    + envelope.payload() + "|" + nullToEmpty(envelope.encryptedKey()) + "|" + nullToEmpty(envelope.iv());
            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(publicKey);
            verifier.update(signed.getBytes(StandardCharsets.UTF_8));
            if (!verifier.verify(Base64.getDecoder().decode(envelope.signature()))) {
                throw new LicenseException(LicenseStatus.INVALID, "License signature verification failed");
            }
            byte[] payload = envelope.encrypted() ? decrypt(envelope) : Base64.getDecoder().decode(envelope.payload());
            return mapper.readValue(payload, LicensePayload.class);
        } catch (LicenseException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new LicenseException(LicenseStatus.INVALID, "Invalid license file: " + ex.getMessage());
        }
    }

    private byte[] decrypt(LicenseEnvelope envelope) throws Exception {
        Cipher aes = Cipher.getInstance("AES/GCM/NoPadding");
        aes.init(Cipher.DECRYPT_MODE, new SecretKeySpec(payloadEncryptionKey, "AES"),
                new GCMParameterSpec(128, Base64.getDecoder().decode(envelope.iv())));
        return aes.doFinal(Base64.getDecoder().decode(envelope.payload()));
    }

    private String nullToEmpty(String value) { return value == null ? "" : value; }
}


