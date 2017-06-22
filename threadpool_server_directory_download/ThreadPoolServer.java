import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.*;
/**
 * Created by Matthew Aizlewood on 28/04/17.
 */
public class ThreadPoolServer {
    private ServerSocket SOCKET = null;
    private int THREAD_NUMBER;
    private int PORT_NUMBER;

    public ThreadPoolServer(int portNumber, int maxThreads)
    {
        PORT_NUMBER = portNumber;
        THREAD_NUMBER = maxThreads;

        try
        {
            SOCKET = new ServerSocket(PORT_NUMBER);
            Executor tasks = Executors.newFixedThreadPool(THREAD_NUMBER);

            for (int i = 0; i < THREAD_NUMBER; i++)
            {
                ClientHandler currThread = new ClientHandler(SOCKET);
                tasks.execute(currThread);
            }
        }
        catch (IOException e)
        {
            System.out.println("PORT NUMBER NOT AVAILABLE");
            //e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        ThreadPoolServer server = new ThreadPoolServer(5000, 10);
    }
}
