package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<Integer, ConcurrentHashMap<Session, String>> games = new ConcurrentHashMap<>();

    public void add(int gameID, Session session, String authToken) {
        games.putIfAbsent(gameID, new ConcurrentHashMap<>());
        games.get(gameID).put(session, authToken);
    }

    public void remove(int gameID, Session session) {
        if (games.containsKey(gameID)){
            games.get(gameID).remove(session);
        }
    }

    public void broadcast(int gameID, Session exclude, String json) throws IOException {
        if (!games.containsKey(gameID)){return;}

        for (Session s : games.get(gameID).keySet()){
            if (s.isOpen() && !s.equals(exclude)) {
                s.getRemote().sendString(json);
            }
        }
    }
}