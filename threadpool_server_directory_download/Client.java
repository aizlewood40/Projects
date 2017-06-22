import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.StringJoiner;

/**
 * Created by Matthew Aizlewood on 23/04/2017.
 */

public class Client {

    private Socket socket = null;
    private Scanner socketIn = null;
    private PrintWriter socketOut = null;
    private Scanner keyboardIn = null;
    private int fileSize;
    private String sentmessage;



     //constructor


    public Client( String host, int port ) {

        try {
            socket = new Socket( host, port );
            socketIn = new Scanner( socket.getInputStream() );
            socketOut = new PrintWriter( socket.getOutputStream(), true );
            keyboardIn = new Scanner( System.in );
        }
        catch( IOException e ) {
            e.printStackTrace();
        }
    }


     // handles text from keyboard to the server


    private void talktoServer() {



        // input
        Thread readerThread = new Thread( new IncomingReader() );
        readerThread.start();

        // keyboard -> output
        while ((sentmessage = keyboardIn.nextLine()) != null) {
            System.out.println("client typed: " + sentmessage);
            socketOut.println( sentmessage );
        }
    }


    //this function allows the client to read in the data from the server
    //and then save this into a file
    public void receiveFile(String fName) throws Exception
    {
        DataInputStream is = new DataInputStream(socket.getInputStream());
        //receive data on the file size
        int fileSize = is.readInt();
        byte[] mybytearray = new byte[fileSize];
        System.out.println("available data " + is.available());

        //output to file
        FileOutputStream fos = new FileOutputStream(fName);
        DataOutputStream dos = new DataOutputStream(fos);

        //read in each individual byte due
        for (int i = 0; i < mybytearray.length; i++)
        {
            byte b = is.readByte();
            mybytearray[i] = b;
        }

        //write to the file
        dos.write(mybytearray, 0, mybytearray.length);
        dos.close();
    }

    //arguements: a -> /img/direc    message -> dirdl /img/direc
    public void receiveDirectory(String a,String message)
    {
        String mess = message.substring(7);
        String[] array = message.split("/");
        System.out.println(a);

        String[] sArray = a.split("/");

        File dir = new File(array[array.length-1]);
        dir.mkdir();
        for(int i = sArray.length - array.length; i < sArray.length; i++)
        {
            try {
                receiveFile(array[array.length - 1] + "/" + sArray[i]);
            }
            catch (Exception e)
            {

            }

        }
    }

    /*
     * handles text from the server
     */

    private class IncomingReader implements Runnable {


        @Override
        public void run() {

            String message;
            while ((message = socketIn.nextLine()) != null) {
                System.out.println("client read: " + message);
                if(message.startsWith("dl"))
                {
                    try{
                        receiveFile(message.substring(3));
                    }
                    catch(Exception e)
                    {

                    }
                }
                if(message.startsWith("dirdl"))
                {
                    receiveDirectory(message.substring(7), sentmessage);
                }
            }


        }
    }

    /*
     * Main function: set the process running
     */

    public static void main( String[] args ) {
        Client c = new Client( "127.0.0.1", 5000 );
        c.talktoServer();
    }
}
