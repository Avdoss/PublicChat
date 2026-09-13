package lesson3;

public abstract class StringMessageDecorator implements StringMessage
{
    protected StringMessage message;

    public StringMessageDecorator(StringMessage message)
    {
        this.message = message;
    }

    public MsgType getMsgType() {
        return message.getMsgType();
    }

    public long getSerialVersion() {
        return message.getSerialVersion();
    }
}
