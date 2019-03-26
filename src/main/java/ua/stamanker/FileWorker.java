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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileWorker {

    public static final String EXT = ".json";
    public static ObjectMapper OBJECTMAPPER;
    public static final String DATA_Dir = "data";

    public FileWorker() {
        OBJECTMAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    public void save(long chatId, Integer msgId, Object data) {
        long start = System.currentTimeMillis();
        if(data instanceof MsgData) {
            ((MsgData) data).updated = new Date();
        }
        save2(chatId+"", msgId, data);
        System.out.println("\tsave = " + String.format("%,3d", System.currentTimeMillis() - start));
    }

    public void save2(String chatId, Integer fileName, Object data) {
        try {
            String dataTxt = OBJECTMAPPER.writeValueAsString(data);
            writeFile(chatId, fileName, dataTxt);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeFile(String subDir, Integer fileName, String data) {
        String first = DATA_Dir;
        if(subDir!=null) {
            first += "/" + subDir;
        }
        new File(first).mkdir();
        List<String> subDirs = getSubDirs(fileName);//TODO!!!!
        first += "/" + fileName + EXT;
        writeFile(data, first);
    }

    private void writeFile(String data, String path) {
        try {
            Files.write(Paths.get(path), data.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> getSubDirs(Integer fileNumber) {
        List<String> result = new ArrayList<>();
        int n = 1000_000;
        int x;
        do {
            x = fileNumber / n;
            if(x==0) {
                result.add(n + "");
            } else {
                result.add( (x * n)+"" );
            }
            n = n / 10;
        } while (x < 100);
        return result;
    }

    public static void main(String[] args) {
        System.out.println(getSubDirs(24_533));
    }

    public MsgData read (long chatId, Integer messageId) {
        long start = System.currentTimeMillis();
        try {
            String chatDir = chatId + "";
            byte[] bytes = Files.readAllBytes(Paths.get(DATA_Dir + "/" + chatDir + "/" + messageId + EXT));
            String s = new String(bytes);
            return deserialize(s, MsgData.class);
        } catch (FileNotFoundException | NoSuchFileException fnfe) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            System.out.println("\tread = " + String.format("%,3d", System.currentTimeMillis() - start));
        }
    }

    private <T> T deserialize(String s, Class<T> c) throws IOException {
        return OBJECTMAPPER.readValue(s, c);
    }

    private String serialize(Object data) {
        try {
            return OBJECTMAPPER.writeValueAsString(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Settings readSettings() {
        try {
            String json = new String(Files.readAllBytes(Paths.get(getSettingsPath())));
            return deserialize(json, Settings.class);
        } catch (IOException e) {
            System.err.println("*** Error while reading settings: " + e.getMessage());
            Settings settings = new Settings();
            saveSettings(settings);
            return settings;
        }
    }

    private String getSettingsPath() {
        return String.format("%s/settings%s", DATA_Dir, EXT);
    }

    public void saveSettings(Settings settings) {
        try {
            String json = serialize(settings);
            writeFile(json, getSettingsPath());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
