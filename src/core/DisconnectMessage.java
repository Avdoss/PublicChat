package core;


import java.nio.ByteBuffer;

public class DisconnectMessage implements Message
{
    public static final MsgType MSG_TYPE = MsgType.DISCONNECT;
    public static final long SERIAL_VERSION = 5610457651527594625L;

    public MsgType getMsgType()
    {
        return MSG_TYPE;
    }

    public long getSerialVersion()
    {
        return SERIAL_VERSION;
    }

    public void serialize(ByteBuffer out)
    {
    }

    public void deserialize(ByteBuffer in)
    {
    }
}
