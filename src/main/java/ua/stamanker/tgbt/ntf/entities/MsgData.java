package ua.stamanker.tgbt.ntf.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.*;

import static ua.stamanker.tgbt.ntf.Emoji.*;

public class MsgData {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    public Date created;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    public Date updated;

    public Map<String, Integer> buttons = new LinkedHashMap<>();
    public Map<String, Set<String>> userReactions = new HashMap<>();
    public Set<String> voters = new HashSet<>();

    @JsonIgnore
    public Long chatId2Store;

    public MsgData() {
    }

    public MsgData initButtons(List<String> btns) {
        created = new Date();
        for (String button : btns) {
            buttons.put(button, 0);
        }
        return this;
    }

    public MsgData initDefault() {
        created = new Date();
        buttons.put(THUMB_UP, 0);
        buttons.put(OK, 0);
        buttons.put(THUMB_DN, 0);
        buttons.put(SMILE_MIMI, 0);
        buttons.put(GRINNING, 0);
        buttons.put(FEARFUL, 0);
        buttons.put(ASTONISHED, 0);
        return this;
    }

    public void registerNewButtonClick(Integer userId, String username, String buttonClicked) {
        System.out.println("\tuserId = [" + userId + "], username = [" + username + "], buttonClicked = [" + buttonClicked + "]");
        voters.add(username);
        Set<String> userButtonClicked = userReactions.computeIfAbsent("user-" + userId, x -> new HashSet<>());
        if(true) { //user can vote only for 1 button
            if (!userButtonClicked.isEmpty()) {
                Iterator<String> iterator = userButtonClicked.iterator();
                String wasBefore = iterator.next();
                iterator.remove();
                System.out.println("\twasBefore = " + wasBefore + " buttonClicked = " + buttonClicked);
                buttons.compute(wasBefore, (x, v) -> --v);
                if(!wasBefore.equals(buttonClicked)) {
                    userButtonClicked.add(buttonClicked);
                    buttons.compute(buttonClicked, (x, v) -> {
                        return v==null ? 1 : ++v;
                    });
                }
            } else {
                userButtonClicked.add(buttonClicked);
                buttons.compute(buttonClicked, (x, v) -> {
                    return v == null ? 1 : ++v;
                });
            }
        } else { //user can vote for all buttons
            if (userButtonClicked.contains(buttonClicked)) {
                userButtonClicked.remove(buttonClicked);
                buttons.put(buttonClicked, buttons.get(buttonClicked) - 1);
            } else {
                userButtonClicked.add(buttonClicked);
                buttons.put(buttonClicked, buttons.get(buttonClicked) + 1);
            }
        }
    }

    @JsonIgnore
    public List<Map.Entry<String, Integer>> getButtonsAndCount() {
        return new ArrayList<>(buttons.entrySet());
    }

    public MsgData setChatId2Store(Long v) {
        this.chatId2Store = v;
        return this;
    }
}
