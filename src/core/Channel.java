package core;

public interface Channel
{
    boolean hasConnection();
    Connection getConnection();
    void start();
    void stop();
}
