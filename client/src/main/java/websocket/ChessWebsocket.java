package websocket;

import com.google.gson.Gson;
import exception.ResponseException;
import jakarta.websocket.*;
import org.glassfish.tyrus.core.wsadl.model.Endpoint;

import java.io.IOException;
import java.net.URISyntaxException;

public class ChessWebsocket extends Endpoint {
    private Session session;
    private final NotificationHandler handler;
    private final Gson gson = new Gson();
    private final String authToken;

    public ChessWebsocket(String url, String authToken, NotificationHandler handler) throws ResponseException
        try{
            this.handler = handler;
            this.authToken = authToken;

            url = url.replace ("http","ws");

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, url);

        this.session.addMessageHandler((MessageHandler.Whole<String>) message -> {
            ChessNotification note = gson.fromJson(message, ChessNotification.class);
            handler.notify(note);
        });

    } catch (Exception e){
        throw new ResponseException(e.getMessage());
    }
}

@Override
public void onOpen(Session session, EndpointConfig config){
    try{
        session.getBasicRemote().sendText(authJson());
    }
}
