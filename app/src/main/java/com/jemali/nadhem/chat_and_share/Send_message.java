package com.jemali.nadhem.chat_and_share;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import static com.jemali.nadhem.chat_and_share.MainActivity.destination_name;
import static com.jemali.nadhem.chat_and_share.MainActivity.my_name;
import static com.jemali.nadhem.chat_and_share.MainActivity.write_message;
import static com.jemali.nadhem.chat_and_share.MainActivity.add_message;
/**
 * Created by nadhem on 22/04/2017.
 */

public class Send_message extends AsyncTask<Void, Void, Void> {

    String dstAddress;
    String msgToServer;
    boolean message_state=false;
    Context main_context;
    final int MESSAGE_PORT = 8080;

    Send_message(String addr, String msgTo,Context context) {
        dstAddress = addr;
        msgToServer = msgTo;
        message_state=false;
        main_context=context;
    }

    @Override
    protected Void doInBackground(Void...  arg0) {

        Socket socket = null;
        DataOutputStream dataOutputStream = null;
        DataInputStream dataInputStream = null;


        try {

            InetAddress serverAddr = InetAddress.getByName(dstAddress);

            socket = new Socket(serverAddr, MESSAGE_PORT);
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataInputStream = new DataInputStream(socket.getInputStream());

            if(msgToServer != null){
                dataOutputStream.writeUTF(my_name+" "+msgToServer);
                message_state=true;

            }

            //  response = dataInputStream.readUTF();

        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            // response = "UnknownHostException: " + e.toString();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            //response = "IOException: " + e.toString();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            if (dataOutputStream != null) {
                try {
                    dataOutputStream.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            if (dataInputStream != null) {
                try {
                    dataInputStream.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if (message_state)
        {
            add_message("to: "+destination_name,msgToServer);
            write_message.setText("");
        }
        else
            Toast.makeText(main_context,"message not sent", Toast.LENGTH_SHORT).show();
        super.onPostExecute(result);
    }
}