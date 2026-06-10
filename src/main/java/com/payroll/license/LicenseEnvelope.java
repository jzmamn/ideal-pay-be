package com.payroll.license;

public record LicenseEnvelope(
        int formatVersion, String keyId, boolean encrypted, String payload,
        String encryptedKey, String iv, String signature) {
}
