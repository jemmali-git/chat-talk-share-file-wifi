
package com.jemali.nadhem.chat_and_share;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import static com.jemali.nadhem.chat_and_share.MainActivity.destination_name;

public class Send_File extends AsyncTask<Void, Integer, Void> {

    String dstAddress;
    String file_path;
    String file_name;
    final int file_PORT = 50010;
    private Activity main_activity;
    private Context context;

    Send_File(String addr ,String aux_file_path,Activity activity,Context context ) {
        dstAddress = addr;
        file_path=aux_file_path;
        this.file_name=file_path.substring(file_path.lastIndexOf('/')).substring(1);
        main_activity=activity;
        this.context=context;
    }

    @Override
    protected Void doInBackground(Void...  arg0) {
        for (int i=0;i<2;i++)
        {
            Socket socket = null;
            if (i==0)
            {
                try {
                    InetAddress serverAddr = InetAddress.getByName(dstAddress);
                    socket = new Socket(serverAddr, file_PORT);
                    DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    dataOutputStream.writeUTF(file_name);
                }catch (UnknownHostException e){}
                catch (IOException e){}
                finally {
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (IOException e) {}
                    }
                }
            }
            else{
                try {
                    InetAddress serverAddr = InetAddress.getByName(dstAddress);
                    socket = new Socket(serverAddr, file_PORT);

                    File file = new File(file_path);

                    byte[] bytes = new byte[(int) file.length()];
                    BufferedInputStream bis;
                    try {
                        FileInputStream fis = new FileInputStream(file);
                        bis = new BufferedInputStream(fis);
                        bis.read(bytes, 0, bytes.length);
                        OutputStream os = socket.getOutputStream();
                        ObjectOutputStream oos = new ObjectOutputStream(os);
                        oos.writeObject(bytes);
                        oos.flush();
                        final String sentMsg = "File sent to: " + destination_name;
                        main_activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, sentMsg, Toast.LENGTH_LONG).show();
                            }});

                    } catch (FileNotFoundException e) {}
                    catch (IOException e) {}
                    finally {try {socket.close();} catch (IOException e) {}}
                }
                catch (UnknownHostException e) {}
                catch (IOException e) {}
                finally {
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (IOException e) {}
                    }
                }

            }
        }
        return null;
    }
    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
    }


}