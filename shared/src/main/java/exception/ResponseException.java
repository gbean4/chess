package exception;

import com.google.gson.Gson;
import java.util.HashMap;

public class ResponseException extends Exception {

    public enum Code {
        ServerError,
        BadRequest,
        Unauthorized,
        Forbidden,
        NotFound
    }

    public ResponseException(String message) {
        super(message);
    }

    public static ResponseException fromJson(String json) {
        var map = new Gson().fromJson(json, HashMap.class);
        var statusString = map.get("status").toString();
        String message = map.get("message").toString();

        Code status;
        try {
            status = Code.valueOf(statusString);
        } catch (IllegalArgumentException e){
            if (statusString.contains("403")){
                status = Code.Forbidden;
            } else if (statusString.contains("400")){
                status = Code.BadRequest;
            } else if (statusString.contains("401")){
                status = Code.Unauthorized;
            } else {
                status = Code.ServerError;
            }
        }

        return new ResponseException(message);
    }
}
