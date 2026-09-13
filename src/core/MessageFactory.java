package lesson3;

public class MessageFactory
{
    public static Message createMessage(MsgType msgType)
    {
        return switch (msgType)
        {
            case AUTHORIZATION -> new AuthorizationMessage();
            case AUTHORIZATION_RESPONSE -> new AuthorizationResponseMessage();
            case DISCONNECT -> new DisconnectMessage();
            case LETTER -> new LetterMessage();
        };
    }
}
