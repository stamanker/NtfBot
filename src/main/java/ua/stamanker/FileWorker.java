package ua.stamanker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import ua.stamanker.entities.MsgData;
import ua.stamanker.entities.Settings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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

    private void writeFile(String chatId, Integer fileName, String data) {
        List<String> subDirs = getFileDir(chatId, fileName);
        String path = "";
        for (String dir : subDirs) {
            path = path + dir + "/";
            File d = new File(path);
            if(!d.exists()) {
                d.mkdir();
            }
        }
        path = path + fileName + EXT;
        writeFile(data, path);
    }

    private List<String> getFileDir(String subDir, Integer fileName) {
        List<String> subDirs = getSubDirs(fileName);
        subDirs.add(0, subDir);
        subDirs.add(0, DATA_Dir);
        return subDirs;
    }

    private void writeFile(String data, String path) {
        try {
            Files.write(Paths.get(path), data.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
            System.out.println("File written: " + path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> getSubDirs(Integer fileNumber) {
        List<String> result = new ArrayList<>();
        int n = 100_000;
        int x;
        do {
            x = fileNumber / n;
            String value;
            if(x==0) {
                value = n + "";
            } else {
                value = (++x * n) + "";
            }
            result.add(value);
            n = n / 10;
        } while (n > 10);
        return result;
    }

    public static void main(String[] args) throws IOException {
        FileWorker fileWorker = new FileWorker();
        String chatId = "-1001287571102";
        String[] list = new File(DATA_Dir + "/" + chatId).list();
        for (int i = 0; i < list.length; i++) {
            String f = list[i];
            try {
                f = DATA_Dir + "/" + chatId + "/" + f;
                if(!f.startsWith(DATA_Dir + "/" + chatId + "/1000")) {
                    String data = new String(Files.readAllBytes(Paths.get(f)));
                    Integer fileName = Integer.parseInt(Utils.getAfterLast(Utils.getBefore(f, "."), "/"));
                    System.out.println("fileName = " + fileName);
                    fileWorker.writeFile(chatId, fileName, data);
                    System.out.println("pathname = " + f);
                    new File(f).delete();
                } else {

                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public MsgData read (long chatId, Integer messageId) {
        long start = System.currentTimeMillis();
        try {
            String path = getFileDir(chatId + "", messageId).stream().collect(Collectors.joining("/"));
            System.out.println("path2Read = " + path);
            byte[] bytes = Files.readAllBytes(Paths.get(path + "/" + messageId + EXT));
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
