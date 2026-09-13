package core;

import java.io.IOException;
import java.nio.ByteBuffer;

public class AuthorizationResponseMessage implements Message
{
    public static final MsgType MSG_TYPE = MsgType.AUTHORIZATION_RESPONSE;
    public static final long SERIAL_VERSION = 994047712849350545L;

    public enum Status
    {
        SUCCESSFULLY,
        BAD_LOGIN,
        BAD_PASSWORD,
        ALREADY_LOGGED,
    }

    public Status status;

    public MsgType getMsgType() { return MSG_TYPE; }

    public long getSerialVersion() { return SERIAL_VERSION; }

    public void serialize(ByteBuffer out)
    {
        Message.serializeString(out, status.name());
    }

    public void deserialize(ByteBuffer in)
    {
        try {
            String str = Message.deserializeString(in);
            status = Status.valueOf(str);
        } catch (IllegalArgumentException e) {
        Debug.Log(DebugType.ERROR, "Unknown authorization status");
        }
    }
}
