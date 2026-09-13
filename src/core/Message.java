package core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public interface Message
{
    MsgType getMsgType();
    long getSerialVersion();
    void serialize(ByteBuffer out);
    void deserialize(ByteBuffer in);

    static void serializeMessage(ByteBuffer out, Message message)
    {
        serializeString(out, message.getMsgType().name());
        out.putLong(message.getSerialVersion());
        message.serialize(out);
    }

    static Message deserializeMessage(ByteBuffer in)
    {
        Message message = null;
        try {
            String msgTypeStr = deserializeString(in);
            MsgType msgType = MsgType.valueOf(msgTypeStr);
            long serialVersion = in.getLong();
            Message message_tmp = MessageFactory.createMessage(msgType);
            if (message_tmp.getSerialVersion() == serialVersion)
            {
                message_tmp.deserialize(in);
                message = message_tmp;
            }
            else
                Debug.Log(DebugType.ERROR, "The version number in the new message does not match the expected one");
        } catch (IllegalArgumentException e) {
            Debug.Log(DebugType.ERROR, "Unknown input message type");
        }
        return message;
    }

    static void serializeString(ByteBuffer out, String str)
    {
        out.putInt(str.length());
        out.put(str.getBytes(StandardCharsets.UTF_8));
    }

    static String deserializeString(ByteBuffer in)
    {
        int size = in.getInt();
        byte[] bytes = new byte[size];
        in.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
