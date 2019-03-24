package ua.stamanker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import ua.stamanker.entities.MsgData;
import ua.stamanker.entities.Settings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;

public class FileWorker {

    public static final String EXT = ".json";
    public static ObjectMapper OBJECTMAPPER;
    public static final String dataDir = "data";

    public FileWorker() {
        OBJECTMAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    public void save(long chatId, Integer msgId, Object data) {
        long start = System.currentTimeMillis();
        if(data instanceof MsgData) {
            ((MsgData) data).updated = new Date();
        }
        save(chatId+"", msgId+"", data);
        System.out.println("\tsave took " + String.format("%,3d", System.currentTimeMillis() - start));
    }

    public void save(String subDir, String fileName, Object data) {
        try {
            String dataTxt = OBJECTMAPPER.writeValueAsString(data);
            writeFile(subDir, fileName, dataTxt);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeFile(String subDir, String fileName, String data) {
        try {
            String first = dataDir;
            if(subDir!=null) {
                first += "/" + subDir;
            }
            new File(first).mkdir();
            first += "/" + fileName + EXT;
            Files.write(Paths.get(first), data.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public MsgData read (long chatId, Integer messageId) {
        try {
            String chatDir = chatId + "";
            byte[] bytes = Files.readAllBytes(Paths.get(dataDir + "/" + chatDir + "/" + messageId + EXT));
            String s = new String(bytes);
            return deserialize(s, MsgData.class);
        } catch (FileNotFoundException | NoSuchFileException fnfe) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T deserialize(String s, Class<T> c) throws IOException {
        return OBJECTMAPPER.readValue(s, c);
    }

    public Settings readSettings() {
        String settingsPath = String.format("%s/settings%s", dataDir, EXT);
        try {
            String json = new String(Files.readAllBytes(Paths.get(settingsPath)));
            return deserialize(json, Settings.class);
        } catch (IOException e) {
            System.err.println("*** Error while reading settings: " + e.getMessage());
            Settings settings = new Settings();
            save(null, "settings", settings);
            return settings;
        }
    }

    public void saveSettings(Settings settings) {
        try {
            String json = OBJECTMAPPER.writeValueAsString(settings);
            writeFile(null, "settings", json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
