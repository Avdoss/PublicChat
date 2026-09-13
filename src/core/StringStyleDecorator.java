package lesson3;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;


public class StringStyleDecorator extends StringMessageDecorator
{
    public StringStyleDecorator(StringMessage message)
    {
        super(message);
    }

    @Override
    public void serialize(ByteBuffer out)
    {
        out.put("\u001B[1m".getBytes(StandardCharsets.UTF_8));
        message.serialize(out);
        out.put("\u001B[0m".getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void deserialize(ByteBuffer in)
    {
        message.deserialize(in);
    }
}

