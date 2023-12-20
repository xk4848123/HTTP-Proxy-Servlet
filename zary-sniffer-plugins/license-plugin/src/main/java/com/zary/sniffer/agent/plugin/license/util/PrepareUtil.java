package com.zary.sniffer.agent.plugin.license.util;

import com.zary.sniffer.agent.plugin.license.entity.LicenseInfox;
import com.zary.sniffer.util.FileUtil;

import java.nio.charset.StandardCharsets;

public class PrepareUtil {

    public static void generateLicense(String originalPath,String targetPath) throws Exception {
        String licenseInfox = FileUtil.readLines(originalPath, StandardCharsets.UTF_8.name());
        String encryptLicenseInfox = DESUtil.encrypt(licenseInfox, "298b09b4");
        FileUtil.write(targetPath, encryptLicenseInfox, StandardCharsets.UTF_8.name());
    }

    public static LicenseInfox parseLicense(String targetPath) throws Exception {

        String licenseInfoxStr = DESUtil.decrypt(FileUtil.readLines(targetPath, StandardCharsets.UTF_8.name()), "298b09b4");

        String[] split = licenseInfoxStr.split("&");
        LicenseInfox licenseInfox = new LicenseInfox();
        for (String s : split) {
            String[] keyValue = s.split("=");
            if (keyValue[0].equals("effectDate")) {
                licenseInfox.setEffectDate(keyValue[1]);
            }
            if (keyValue[0].equals("deadDate")) {
                licenseInfox.setDeadDate(keyValue[1]);
            }
            if (keyValue[0].equals("customerName")) {
                licenseInfox.setCustomerName(keyValue[1]);
            }
            if (keyValue[0].equals("version")) {
                licenseInfox.setVersion(keyValue[1]);
            }
            if (keyValue[0].equals("machineCode")) {
                licenseInfox.setMachineCode(keyValue[1]);
            }
        }
        return licenseInfox;
    }
}
