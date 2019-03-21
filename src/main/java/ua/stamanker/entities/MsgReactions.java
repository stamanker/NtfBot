package ua.stamanker.entities;

import java.util.*;

public class MsgReactions {

    public Set<String> buttons = new HashSet<>();

    public Map<String, Set<String>> userReactions = new HashMap<>();

    public boolean putForUser(Integer userId, String buttonClicked) {
        Set<String> userButtonClicked = (Set<String>)userReactions.computeIfAbsent("user-" + userId, x -> new HashSet<>() );
        return userButtonClicked.add(buttonClicked);
    }
}
