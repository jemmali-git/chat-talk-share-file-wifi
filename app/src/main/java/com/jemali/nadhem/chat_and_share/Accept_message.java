package com.jemali.nadhem.chat_and_share;

import android.app.Activity;
import android.content.Context;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static com.jemali.nadhem.chat_and_share.MainActivity.add_message;
import static com.jemali.nadhem.chat_and_share.MainActivity.message_socket;


/**
 * Created by nadhem on 22/04/2017.
 */

public class Accept_message extends Thread {
    final int MESSAGE_PORT = 8080;
    private Activity main_activity;
    private Context context;
    Accept_message(Activity activity,Context context)
    {
        main_activity=activity;
        this.context=context;
    }
    @Override
    public void run() {
        Socket socket = null;
        DataInputStream dataInputStream = null;
        DataOutputStream dataOutputStream = null;

        try {
            message_socket = new ServerSocket(MESSAGE_PORT);

            while (true) {
                socket = message_socket.accept();
                dataInputStream = new DataInputStream(
                        socket.getInputStream());
                dataOutputStream = new DataOutputStream(
                        socket.getOutputStream());

                String messageFromClient = "";
                final String ip_of_client=socket.getInetAddress().toString().substring(1);
                //If no message sent from client, this code will block the program
                messageFromClient = dataInputStream.readUTF();
                final String [] name_and_msg = messageFromClient.split(" ", 2);
                main_activity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                            add_message("from: "+name_and_msg[0],name_and_msg[1]);
                    }
                });

            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
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

            if (dataOutputStream != null) {
                try {
                    dataOutputStream.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}

