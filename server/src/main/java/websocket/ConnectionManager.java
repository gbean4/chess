package websocket;

import org.eclipse.jetty.websocket.api.Session;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<Integer, ConcurrentHashMap<Session, String>> games = new ConcurrentHashMap<>();

    public void add(int gameID, Session session, String authToken) {
//        games.putIfAbsent(gameID, new ConcurrentHashMap<>());
//        games.get(gameID).put(session, authToken);
        games.computeIfAbsent(gameID, id-> new ConcurrentHashMap<>()).put(session,authToken);
    }

    public void remove(int gameID, Session session) {
//        if (games.containsKey(gameID)){
//            games.get(gameID).remove(session);
//        }
        var gameMap = games.get(gameID);
        if (gameMap == null){return;}

        gameMap.remove(session);

        if (gameMap.isEmpty()){
            games.remove(gameID);
        }
    }

    public void broadcast(int gameID, Session exclude, String json) throws IOException {
//        if (!games.containsKey(gameID)){return;}
//
//        for (Session s : games.get(gameID).keySet()){
//            if (s.isOpen() && !s.equals(exclude)) {
//                s.getRemote().sendString(json);
//            }
//        }
        var gameMap = games.get(gameID);
        if(gameMap == null){
            return;
        }
        Iterator<Session> it = gameMap.keySet().iterator();

        while (it.hasNext()){
            Session s = it.next();
            if (s.equals(exclude)){
                continue;
            }
            try{
                if (s.isOpen()){
                    s.getRemote().sendString(json);
                } else {
                    it.remove();
                }
            } catch (IOException e){
                it.remove();
            }
        }
        if (gameMap.isEmpty()){
            games.remove(gameID);
        }
    }
}