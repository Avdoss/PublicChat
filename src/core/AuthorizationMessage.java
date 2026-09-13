package core;

import java.nio.ByteBuffer;

public class AuthorizationMessage implements Message
{
    public static final MsgType MSG_TYPE = MsgType.AUTHORIZATION;
    public static final long SERIAL_VERSION = 4674810209473567476L;

    public String login;
    public String password;

    public MsgType getMsgType() { return MSG_TYPE; }

    public long getSerialVersion() { return SERIAL_VERSION; }

    public void serialize(ByteBuffer out)
    {
        Message.serializeString(out, login);
        Message.serializeString(out, password);
    }

    public void deserialize(ByteBuffer in)
    {
        login = Message.deserializeString(in);
        password = Message.deserializeString(in);
    }
}
