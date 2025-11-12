package datamodel;

public record RegisterResponse (UserData user, String username, String authToken){
}
