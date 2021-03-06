package com.learningmachine.android.app.util;

import android.content.Context;

import com.google.common.io.Files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import okio.Buffer;
import timber.log.Timber;

public class FileUtils {

    private static final String CERT_DIR = "certs";
    private static final String JSON_EXT = ".json";

    public static boolean saveCertificate(Context context, Buffer buffer, String uuid) {
        File file = getCertificateFile(context, uuid, true);
        return writeResponseBodyToDisk(file, buffer);
    }

    public static boolean copyCertificateStream(Context context, InputStream inputStream, String uuid) {
        File outputFile = getCertificateFile(context, uuid, true);
        return copyInputStream(inputStream, outputFile);
    }

    public static boolean deleteCertificate(Context context, String uuid) {
        File file = getCertificateFile(context, uuid);
        return file.delete();
    }

    public static void renameCertificateFile(Context context, String oldName, String newName) {
        File oldFile = getCertificateFile(context, oldName);
        File newFile = getCertificateFile(context, newName);
        try {
            Files.move(oldFile, newFile);
        } catch (IOException e) {
            Timber.e(e, "Failed to rename the certificate file");
        }
    }

    public static File getCertificateFile(Context context, String uuid) {
        return getCertificateFile(context, uuid, false);
    }

    private static File getCertificateFile(Context context, String uuid, boolean createDir) {
        File certDir = getCertificateDirectory(context, createDir);
        String filename = uuid + JSON_EXT;
        return new File(certDir, filename);
    }

    private static File getCertificateDirectory(Context context, boolean createDir) {
        File certDir = new File(context.getFilesDir(), CERT_DIR);
        if (createDir) {
            certDir.mkdirs();
        }
        return certDir;
    }

    private static boolean writeResponseBodyToDisk(File file, Buffer buffer) {
        try (InputStream inputStream = buffer.inputStream();
             OutputStream outputStream = new FileOutputStream(file)) {
            return copyStreams(inputStream, outputStream);
        } catch (IOException e) {
            Timber.e(e, "Unable to write ResponseBody to file");
            return false;
        }
    }

    private static boolean copyInputStream(InputStream inputStream, File outputFile) {
        try (OutputStream outputStream = new FileOutputStream(outputFile)) {
            return copyStreams(inputStream, outputStream);
        } catch (IOException e) {
            Timber.e(e, "Unable to copy certificate file");
            return false;
        }
    }

    public static boolean copyStreams(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[4096];
        while (true) {
            int read = inputStream.read(buffer);
            if (read == -1) {
                break;
            }
            outputStream.write(buffer, 0, read);
        }

        outputStream.flush();
        return true;
    }

    public static boolean appendCharactersToFile(InputStream inputStream, File outputFile) throws IOException {
        try (FileWriter writer = new FileWriter(outputFile, true);
             InputStreamReader reader = new InputStreamReader(inputStream)) {
            int characterCount = 4096;
            char[] buffer = new char[characterCount];
            while (true) {
                int read = reader.read(buffer, 0, characterCount);
                if (read == -1) {
                    break;
                }
                writer.write(buffer, 0, read);
            }

            writer.flush();
            return true;
        } catch (IOException ioe) {
            Timber.e("Failed to append to file " + outputFile.getName());
            throw ioe;
        }
    }

    public static boolean appendStringToFile(String string, File outputFile) throws IOException {
        Timber.i("Last 5 characters are" + string.substring(string.length()-5));
        FileWriter writer = new FileWriter(outputFile, true);
        writer.write(string);

        writer.flush();
        return true;
    }
}
