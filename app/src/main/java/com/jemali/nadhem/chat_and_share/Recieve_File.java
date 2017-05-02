package com.jemali.nadhem.chat_and_share;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import static com.jemali.nadhem.chat_and_share.MainActivity.file_socket;

/**
 * Created by nadhem on 25/04/2017.
 */

public class Recieve_File extends Thread {
    private Activity main_activity;
    private Context context;
    final int file_PORT = 50010;
    Recieve_File(Activity activity,Context context)
    {
        main_activity=activity;
        this.context=context;
    }
    @Override
    public void run() {
        Socket socket = null;

        try {
            file_socket = new ServerSocket(file_PORT);
            String aux_file_name="unkown";
            int i=0;
            while (true) {
                socket = file_socket.accept();
                if(i==0)
                {
                    DataInputStream dataInputStream = new DataInputStream(
                            socket.getInputStream());
                    aux_file_name = dataInputStream.readUTF();
                    i=1;
                }
                else
                {
                    i=0;
                    FileRecieveThread f=new FileRecieveThread(socket,aux_file_name);
                    f.start();
                }
            }
        } catch (IOException e) {}
        finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {}
            }
        }
    }
    public class FileRecieveThread extends Thread {
        Socket socket;
        String aux_file_name;
        FileRecieveThread(Socket socket,String file_name){
            this.socket= socket;
            this.aux_file_name=file_name;
        }

        @Override
        public void run() {

            try {
                File file = new File(Environment.getExternalStorageDirectory(),aux_file_name);

                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

                byte[] bytes;
                FileOutputStream fos = null;

                bytes = (byte[])ois.readObject();
                fos = new FileOutputStream(file);
                fos.write(bytes);
                main_activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "recieved", Toast.LENGTH_LONG).show();}});
                if(fos!=null)
                    fos.close();
            }
            catch (FileNotFoundException e){}
            catch (IOException e){}
            catch (ClassNotFoundException e) {}
        }

    }

}