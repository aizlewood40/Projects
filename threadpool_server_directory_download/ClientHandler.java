/**
 * Created by Matthew Aizlewood on 28/04/17.
 */

import java.io.IOException;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.StringJoiner;
import java.util.concurrent.*;
import java.util.Scanner;
import java.io.*;
import java.util.logging.FileHandler;
import java.util.Date;
import java.util.logging.SimpleFormatter;


public class ClientHandler implements Runnable {

    private final ServerSocket servSocket;
    private Socket socket;
    private PrintWriter writer;
    private Writer fileWriter;

    public ClientHandler(ServerSocket socket) {
        servSocket = socket;

    }


    public void sendCurrentDirectory()
    {
        try {
            System.out.println("Thread: " + Thread.currentThread().toString() + " Wanting to view directories available");
            writer = new PrintWriter(socket.getOutputStream(), true);
            File dirName = new File(".");
            File[] fList = dirName.listFiles();
            for (File file : fList) {
                writer.println(file.getName());
            }
            System.out.println("Thread: " + Thread.currentThread().toString() + " Directories sent");
            writer.flush();
            //writer.close();
        }
        catch (IOException a)
        {
            System.out.println("IOException occured");
            a.printStackTrace();
        }
    }

    //this function sends all files within a folder
    public void sendDirectoryList(String fileName)
    {
        try {
            System.out.println("Thread: " + Thread.currentThread().toString() + " Wanting to view directories available");
            writer = new PrintWriter(socket.getOutputStream(), true);

            //assing all files within a folder to a list
            File dirName = new File(".".concat(fileName));
            File[] fList = dirName.listFiles();
            //output each files name to the client
            for (File file : fList) {
                writer.println(file.getName());
            }
            System.out.println("Thread: " + Thread.currentThread().toString() + " Directories sent");
            writer.flush();
        }
        catch (IOException a)
        {
            System.out.println("IOException occured");
            a.printStackTrace();
        }
    }

    //this function sends one file to a client
    public void sendFile(String filePath, String message) throws Exception
    {
        //send through name of the file being downloaded to client
        PrintWriter pw = new PrintWriter(socket.getOutputStream(),true);
        String[] sArray = message.substring(3).split("/");
        pw.println("dl " + sArray[sArray.length-1]);
        pw.flush();

        //read file into a byte array and output to the client
        File myFile = new File(".".concat(filePath));
        byte[] mybytearray = new byte[(int) myFile.length()];
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myFile));
        bis.read(mybytearray, 0, mybytearray.length);
        DataOutputStream os = new DataOutputStream(socket.getOutputStream());
        os.writeInt(mybytearray.length);
        os.write(mybytearray,0,mybytearray.length);

        os.flush();
        bis.close();
    }

    //this function is used within  sending directories and sends an individual file
    public void sendMultFile(File a) throws Exception
    {
        File myFile = a;
        byte[] mybytearray = new byte[(int) myFile.length()];
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myFile));
        bis.read(mybytearray, 0, mybytearray.length);
        DataOutputStream os = new DataOutputStream(socket.getOutputStream());
        //send file size to the client
        os.writeInt(mybytearray.length);
        //send file data to client
        os.write(mybytearray,0,mybytearray.length);
        System.out.println("DATA: " + mybytearray[mybytearray.length - 5]);

        os.flush();
        bis.close();
    }

    //send all files within a folder to the client
    public void sendDirectory(String dirName) throws Exception
    {
        File dir = new File(".".concat(dirName));
        File[] fList = dir.listFiles();
        PrintWriter pw = new PrintWriter(socket.getOutputStream());
        String sMessage = "dirdl " + dirName + "/";
        for(File a: fList)
        {
            System.out.println(a.getName());
            sMessage = sMessage + a.getName() + "/";
        }
        sMessage = sMessage.substring(0,sMessage.length()-1);
        pw.println(sMessage);
        pw.flush();
        for(int i = 0; i < fList.length; i++)
        {
            sendMultFile(fList[i]);
        }



    }

    //function to initalise writing to the log file
    public void initialiseLogFile()
    {
        File file = new File("log.txt");


    }

    //function to write to the log file that takes ther arguement of the type of request made by the client
    public void writeLineLogFile(String requestType) throws IOException
    {
        fileWriter = new BufferedWriter(new FileWriter("log.txt", true));
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        fileWriter.append(dateFormat.format(date) + " IP: " + socket.getInetAddress().toString() + " Type: " + requestType + "\n");
        fileWriter.close();

    }



    @Override
    public void run()
    {
        try{
            initialiseLogFile();
                socket = servSocket.accept();

                Scanner scanner = new Scanner(socket.getInputStream());
                while (true)
                {
                    String message = scanner.nextLine();
                    System.out.println("CLIENT_HANDLER: THREAD " + Thread.currentThread().toString() + " RECEIVED MESSAGE: " + message + " IP: " + socket.getInetAddress().toString());

                    //if message is equal to the command to view directories
                    if (message.equals("view"))
                    {
                        writeLineLogFile("directory view");
                        sendCurrentDirectory();
                    }
                    else if (message.startsWith("view"))
                    {
                        writeLineLogFile("directory view");
                        sendDirectoryList(message.substring(5));
                    }
                    if(message.equals("exit"))
                    {
                        writeLineLogFile("disconnect");
                        System.out.println("Client DC'd");
                        break;
                    }
                    if(message.startsWith("dl"))
                    {
                        try{
                            writeLineLogFile("single file download");
                            String filePath = message.substring(3);
                            sendFile(filePath, message);
                        }
                        catch(Exception e)
                        {

                        }
                    }
                    if(message.startsWith("dirdl"))
                    {
                        try {
                            writeLineLogFile("directory download");
                            sendDirectory(message.substring(6));
                        }
                        catch (Exception e)
                        {

                        }
                    }
            }

        }
        catch (IOException e)
        {
            System.out.println("FAILED TO GET INPUT STREAM");
        }

    }


}
