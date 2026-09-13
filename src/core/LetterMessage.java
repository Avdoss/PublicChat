package core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class LetterMessage implements StringMessage
{
    public static final MsgType MSG_TYPE = MsgType.LETTER;
    public static final long SERIAL_VERSION = 4592876249462551902L;

    public String content;

    public LetterMessage()
    {
        this.content = null;
    }

    public LetterMessage(String content)
    {
        this.content = content;
    }

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
        out.put(content.getBytes(StandardCharsets.UTF_8));
    }

    public void deserialize(ByteBuffer in)
    {
        content = StandardCharsets.UTF_8.decode(in).toString();
    }
}
