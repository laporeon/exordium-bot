package com.laporeon.javoid.helpers;

import net.dv8tion.jda.api.utils.FileUpload;
import okhttp3.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class FileManager {
    private static final String ASSETS_PATH = "/assets/";
    private static final MediaType OGG = MediaType.get("audio/ogg");
    private static final byte[] WAVEFORM = new byte[64];
    private static final Map<String, byte[]> CACHE = new ConcurrentHashMap<>();

    static {
        for (int i = 0; i < WAVEFORM.length; i++) {
            WAVEFORM[i] = (byte) (140 + 90 * Math.sin(i * 0.7));
        }
    }

    private FileManager() {
    }

    public static FileUpload upload(String fileName) {
        byte[] data = CACHE.computeIfAbsent(fileName, FileManager::load);
        return FileUpload.fromData(data, fileName);
    }

    public static FileUpload voiceMessage(String fileName, double durationSeconds) {
        return upload(fileName).asVoiceMessage(OGG, WAVEFORM, durationSeconds);
    }

    private static byte[] load(String fileName) {
        try (InputStream in = FileManager.class.getResourceAsStream(ASSETS_PATH + fileName)) {
            if (in == null) {
                throw new IllegalStateException("File asset not found: " + fileName);
            }
            return in.readAllBytes();
        } catch (IOException e) {
            throw new IllegalStateException("Could not read asset: " + fileName, e);
        }
    }
}
