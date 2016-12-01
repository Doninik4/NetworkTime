package sample;

import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.PointerByReference;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sun.jna.platform.win32.KnownFolders.FOLDERID_Documents;

/**
 * Created by Dominik on 30.11.2016.
 */
public class TCPServer implements Runnable{

    private ServerSocket welcomeSocket;
    private boolean running = true;


    public static void main(String argv[]) throws Exception
    {
        TCPServer server = new TCPServer();
        int port = server.getPortFromConfigFile();
        server.startTCPServer(port);
    }


    public void startTCPServer(int port) throws IOException {
        String clientFormat;
        String responseForClient = "\n";
        InetAddress miad = InetAddress.getByName("0.0.0.0");
        welcomeSocket = new ServerSocket(port, 10, miad);
        System.out.println("Listening on port " + port);
        while(running)
        {
            Socket connectionSocket = null;
            try {
                connectionSocket = welcomeSocket.accept();

            BufferedReader inFromClient =
                    new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

            clientFormat = inFromClient.readLine();

            System.out.println("Received: " + clientFormat);
            SimpleDateFormat simpleDateFormat = null;
            try{
                simpleDateFormat = new SimpleDateFormat(clientFormat);
            } catch (IllegalArgumentException e) {
                responseForClient = "bad date format\n";
            }

            if (simpleDateFormat != null){
                responseForClient = simpleDateFormat.toPattern() + " -> " + simpleDateFormat.format(new Date()) + "\n";
            }

            outToClient.writeBytes(responseForClient);
            } catch (IOException e) {
                System.out.println(e.toString());
            }
        }
    }

    private int getPortFromConfigFile() throws Exception {
        boolean bResult = false;
        String path = "error";
        WinNT.HANDLEByReference hToken = new WinNT.HANDLEByReference();

        bResult = Advapi32.INSTANCE.OpenProcessToken(Kernel32.INSTANCE.GetCurrentProcess(),
                WinNT.TOKEN_IMPERSONATE | WinNT.TOKEN_QUERY, hToken);

        if(bResult != false){

            PointerByReference pathPtr = new PointerByReference();

            WinNT.HRESULT  hResult = Shell32.INSTANCE.SHGetKnownFolderPath(FOLDERID_Documents, 0, hToken.getValue(), pathPtr);

            if (hResult.intValue() == 0) {
                char delim='\0';
                StringBuilder sb = new StringBuilder();
                char pathArray[]=pathPtr.getValue().getCharArray(0,255);
                for (int i = 0; i < pathArray.length; i++) {
                    if(pathArray[i]==delim){
                        char temparr[]=new char[i];
                        System.arraycopy(pathArray,0,temparr,0,i);
                        pathArray=temparr;
                        break;
                    }
                }
                Ole32.INSTANCE.CoTaskMemFree(pathPtr.getValue());
                path = new String(pathArray);
            } else {
                System.err.println("Error: " + hResult);
                throw new Exception("Error while getting path");
            }

            Kernel32.INSTANCE.CloseHandle(hToken.getValue());
        }

        File file = new File(path + File.separator + "Clock" + File.separator + "port.txt");

        if(!file.exists()){
            if(!file.getParentFile().exists()){
                file.getParentFile().mkdirs();
            }

            file.createNewFile();

            BufferedWriter bw = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
            bw.write("6789");
            bw.close();

            System.out.println("New file created");
            return 6789;
        } else {
            BufferedReader br = new BufferedReader(new FileReader(file));
            char[] bufferReadPort = new char[5];
            for(int j = 0; j<5; j++){
                bufferReadPort[j] = ' ';
            }
            br.read(bufferReadPort);
            String port = new String(bufferReadPort);
            port = port.replaceAll("\\s","");
            Pattern p = Pattern.compile("\\d{1,5}");
            Matcher m = p.matcher(port);

            if(!m.matches()){
                throw new Exception("Bad port in file");
            }

            br.close();

            return Integer.parseInt(port);
        }

    }

    public void stopServer() throws IOException {
        running = false;
        welcomeSocket.close();
    }

    @Override
    public void run() {
        int port = 0;
        try {
            port = getPortFromConfigFile();
            startTCPServer(port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
