package ua.stamanker.entities;

import java.util.*;

import static ua.stamanker.Emoji.*;

public class MsgData {

    public Map<String, Integer> buttons = new LinkedHashMap<>();
    public Map<String, Set<String>> userReactions = new HashMap<>();

    public MsgData init() {
        buttons.put(THUMB_UP, 0);
        buttons.put(OK, 0);
        buttons.put(THUMB_DN, 0);
        buttons.put(SMILE_MIMI, 0);
        buttons.put(GRINNING, 0);
        buttons.put(FEARFUL, 0);
        buttons.put(ASTONISHED, 0);
        return this;
    }

    public void registerNewButtonClick(Integer userId, String buttonClicked) {
        Set<String> userButtonClicked = userReactions.computeIfAbsent("user-" + userId, x -> new HashSet<>() );
        if(userButtonClicked.contains(buttonClicked)) {
            userButtonClicked.remove(buttonClicked);
            buttons.put(buttonClicked, buttons.get(buttonClicked)-1);
        } else {
            userButtonClicked.add(buttonClicked);
            buttons.put(buttonClicked, buttons.get(buttonClicked)+1);
        }
    }

    public List<Map.Entry<String, Integer>> getButtonsAndCount() {
        return new ArrayList<>(buttons.entrySet());
    }
}
