package lesson3;

public interface Channel
{
    boolean hasConnection();
    Connection getConnection();
    void start();
    void stop();
}
