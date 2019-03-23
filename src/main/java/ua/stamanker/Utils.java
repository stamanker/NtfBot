package ua.stamanker;

public class Utils {

    public static Integer getNum(String data, String separ) {
        int i = data.indexOf(separ);
        if(i>-1) {
            try {
                return Integer.parseInt(data.substring(i + 1));
            } catch (Exception e) {

            }
        }
        return null;
    }

    public static String getBefore(String data, String separ) {
        int i = data.indexOf(separ);
        if(i>-1) {
            return data.substring(0, i);
        }
        return data;
    }

    public static String getAfter(String data, String a) {
        int i = data.indexOf(a);
        if(i>-1) {
            return data.substring(i+a.length());
        }
        return data;
    }
}
