package lesson3;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class StringColorDecorator extends StringMessageDecorator
{
    public StringColorDecorator(StringMessage message)
    {
        super(message);
    }

    @Override
    public void serialize(ByteBuffer out)
    {
        out.put("\u001B[31m".getBytes(StandardCharsets.UTF_8));
        message.serialize(out);
        out.put("\u001B[0m".getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void deserialize(ByteBuffer in)
    {
        message.deserialize(in);
    }
}
