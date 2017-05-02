package com.jemali.nadhem.chat_and_share;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import static java.lang.System.in;


public class MainActivity extends AppCompatActivity {
    //les personne connectés
    public static RecyclerView.Adapter Connected_people_adapter;
    public static ArrayList<String> names_list = new ArrayList<String>();
    public static ArrayList<InetAddress> ip_brodcast_list = new ArrayList<InetAddress>();
    public static ArrayList<String> ip_adress_list = new ArrayList<String>();
    public static ArrayList<Integer> images_list = new ArrayList<Integer>();
   // les messages;
    public static Show_message adapter_messge;
    public static ArrayList<String> message_list = new ArrayList<String>();
    public static ArrayList<String> name_list = new ArrayList<String>();
    public static ArrayList<String> date_list = new ArrayList<String>();
  //  public static ArrayList<String> message_direction_list = new ArrayList<String>();
    //les infos de la destination
    public static String destination_name = "other";
    public static InetAddress destination_brodcast_ip=null;
    public static String destination_ip;
    public static TextView current_destination;
    //mes info personelle
    public static String my_name = "unknown";
    //message a envoyé
    public static EditText write_message;
    //fichier a envoyer
    TextView file_path;
    private String file_to_send;
    public boolean there_is_file=false;
    //les listener socket
    public static ServerSocket  message_socket;
    public static ServerSocket  file_socket;
    public static DatagramSocket call_socket;
    ///*add call socket and close when exit with file socket
    //l'eat de la conexion
    public boolean is_connected_wifi = false;
    public boolean is_connected_hotspot = false;
    //les donnees sauvegardé
    public Data_Base local_data_base;
    //gestionnaire de contact
    public static ContactManager contactManager;
  //  private String displayName;
    private boolean STARTED = false;
    private boolean IN_CALL = false;
    private boolean LISTEN = false;
    //les infos echangés entre les activités
    public final static String EXTRA_CONTACT = "CONTACT";
    public final static String EXTRA_IP = "IP";
    public final static String EXTRA_DISPLAYNAME = "DISPLAYNAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //sethasoptionsmenu(true);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
      /*  ArrayList<String> my_date_list=date_list;
        Toast.makeText(this,my_date_list.isEmpty()+"",Toast.LENGTH_LONG).show();*/
        local_data_base=new Data_Base(this) ;
        verify_conncetion(); //??????
        find_connected_people();////??
        ImageButton send_message = (ImageButton) findViewById(R.id.send_message);
        send_message.setOnClickListener(buttonSendOnClickListener);

        ImageButton pick_file=(ImageButton)findViewById(R.id.pick_file);
        pick_file.setOnClickListener(buttonSelect_file_OnClickListener);

        file_path=(TextView)findViewById(R.id.file_path_id);
        file_path.setVisibility(View.INVISIBLE);

        write_message = (EditText) findViewById(R.id.write_message);

        ImageButton send_call=(ImageButton)findViewById(R.id.audio_call) ;
        send_call.setOnClickListener(buttonCallOnClickListener);

        current_destination = (TextView) findViewById(R.id.destination_name);
        set_message_adapter();
        contactManager = new ContactManager(my_name,getBroadcastIp(),getIpAddress());

        startCallListener();
        startMessageListener();
        startFileListener();
    }
    void startFileListener()
    {
        Recieve_File recieve_file=new Recieve_File(this,MainActivity.this);
        recieve_file.start();
    }
    void startMessageListener()
    {
        Accept_message recieve_message = new Accept_message(this,MainActivity.this);
        recieve_message.start();

    }
    private void startCallListener() {
        LISTEN = true;
        final int LISTENER_PORT = 50003;
        final int BUF_SIZE = 1024;
        Thread listener = new Thread(new Runnable() {

            @Override
            public void run() {

                try {

                    call_socket = new DatagramSocket(LISTENER_PORT);
                    call_socket.setSoTimeout(1000);
                    byte[] buffer = new byte[BUF_SIZE];
                    DatagramPacket packet = new DatagramPacket(buffer, BUF_SIZE);
                    while(LISTEN) {
                        // Listen for incoming call requests
                        try {
                            call_socket.receive(packet);
                            String data = new String(buffer, 0, packet.getLength());
                            String action = data.substring(0, 4);
                            if(action.equals("CAL:")) {
                                // Received a call request. Start the ReceiveCallActivity
                                String address = packet.getAddress().toString();
                                String name = data.substring(4, packet.getLength());

                                Intent intent = new Intent(MainActivity.this, ReceiveCallActivity.class);
                                intent.putExtra(EXTRA_CONTACT, name);
                                intent.putExtra(EXTRA_IP, address.substring(1, address.length()));
                                IN_CALL = true;
                                //LISTEN = false;
                                //stopCallListener()
                                startActivity(intent);
                            }
                            else {
                            }
                        }
                        catch(Exception e) {}
                    }
                    call_socket.disconnect();
                    call_socket.close();
                }
                catch(SocketException e) {

                }
            }
        });
        listener.start();
    }
    public  void refresh_Contact_List() {
        if(!(contactManager==null))
        {
            HashMap<String, Contact_info> contacts = contactManager.getContacts();
            names_list.removeAll(names_list);
            ip_brodcast_list.removeAll(ip_brodcast_list);
            ip_adress_list.removeAll(ip_adress_list);
            images_list.removeAll(images_list);

            for(String ipadd : contacts.keySet()) {
                ip_brodcast_list.add(contacts.get(ipadd).udp_adress);
                names_list.add(contacts.get(ipadd).user_name);
                images_list.add(R.drawable.ic_connected);
                ip_adress_list.add(ipadd);
            }
            Connected_people_adapter.notifyDataSetChanged();
           Toast.makeText(MainActivity.this,names_list.size()+" people are connecetd",Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(MainActivity.this," no one is connected",Toast.LENGTH_SHORT).show();
        return;
    }
    public  void send_file()
    {
        Send_File file_sender_task = new Send_File(destination_ip,file_to_send,this,MainActivity.this);
        file_sender_task.execute();
        file_path.setVisibility(View.INVISIBLE);
    }


    View.OnClickListener buttonSelect_file_OnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View arg0) {
            FileChooser file_pick_dialog = new FileChooser();

            if (FileChooser.isPermissionGranted(MainActivity.this))
            {
                file_pick_dialog.setOnChosenListener(new FileChooser.FileChooserListener() {

                    @Override
                    public void onFileChosen(File file) {
                        setText(file.getAbsolutePath());
                    }

                    @Override
                    public void onDirectoryChosen(File directory) {
                        //   setText(directory.getAbsolutePath());
                    }

                    @Override
                    public void onCancel() {
                        //setText("onCancel");
                    }
                });

                file_pick_dialog.setShowHidden(false);
                file_pick_dialog.show(getFragmentManager(), "SimpleFileChooserDialog");
            }
            else
                file_pick_dialog.requestPermission(MainActivity.this);

        }
    };

    private void setText(String text) {
        file_path.setText(text);
        file_to_send=text;
        there_is_file=true;
        file_path.setVisibility(View.VISIBLE);
    }



    View.OnClickListener buttonCallOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            if(destination_brodcast_ip == null) {
                // If no device was selected, present an error message to the user
                final android.app.AlertDialog alert = new android.app.AlertDialog.Builder(MainActivity.this).create();
                alert.setTitle("Oops");
                alert.setMessage("You must select a contact first");
                alert.setButton(-1, "OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        alert.dismiss();
                    }
                });
                alert.show();
                return;
            }
            // Collect details about the selected contact
            String contact = destination_name;

            InetAddress ip = destination_brodcast_ip;
            String address = ip.toString();
            IN_CALL = true;
            Intent intent = new Intent(MainActivity.this, MakeCallActivity.class);
            intent.putExtra(EXTRA_CONTACT, contact);

            address = address.substring(1, address.length());
            intent.putExtra(EXTRA_IP, address);
            intent.putExtra(EXTRA_DISPLAYNAME, my_name);
            startActivity(intent);
        }
    };
    View.OnClickListener buttonSendOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View arg0) {
            if(there_is_file)
            {
                there_is_file=false;
                send_file();
            }
            else
                send_message();

        }
    };
    public  void send_message()
    {
        String tMsg = write_message.getText().toString();
        // destination_brodcast_ip.replaceAll("\\s+","");
        if(current_destination.getText().toString().equals(""))
            Toast.makeText(MainActivity.this,"please select your destination",Toast.LENGTH_SHORT).show();
        else if(!(tMsg.trim().isEmpty()))
        {
            Send_message send_message_task = new Send_message(destination_ip, tMsg,MainActivity.this);
            send_message_task.execute();
        }
    }

    private InetAddress getBroadcastIp() {
        // Function to return the broadcast address, based on the IP address of the device
        try {

            WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();
            String addressString = toBroadcastIp(ipAddress);
            InetAddress broadcastAddress = InetAddress.getByName(addressString);
            return broadcastAddress;
        }
        catch(UnknownHostException e) {
            return null;
        }

    }
    private String toBroadcastIp(int ip) {
        // Returns converts an IP address in int format to a formatted string
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                "255";
    }
    private void stopListener() {
        // Ends the listener thread
        LISTEN = false;
    }

    @Override
    public  void onResume()
    {
        super.onResume();
        retrieve_data();
    }
    @Override
    public void onRestart() {

        super.onRestart();
        IN_CALL = false;
        STARTED = true;
        contactManager = new ContactManager(my_name,getBroadcastIp(),getIpAddress());
        startCallListener();
    }
    public  void save_data()
    {
        local_data_base.putListString("message_list", message_list);
        local_data_base.putListString("name_list", name_list);
        local_data_base.putListString("date_list", date_list);
        local_data_base.putString("my_name",my_name);
    }
    public  void retrieve_data()
    {
      if(local_data_base.getString("my_name").equals(""))
            my_name="unknown";
        else
            my_name=local_data_base.getString("my_name");
        if(message_list.isEmpty()&&name_list.isEmpty()&&date_list.isEmpty())
        {
            message_list.addAll(local_data_base.getListString("message_list")) ;
            name_list.addAll(local_data_base.getListString("name_list")) ;
            date_list.addAll(local_data_base.getListString("date_list")) ;
        }
        adapter_messge.notifyDataSetChanged();

    }
    public void delete_message()
    {
        message_list.clear();
        name_list.clear();
        date_list.clear();
        adapter_messge.notifyDataSetChanged();
        save_data();
    }
    public  void set_my_name()
    {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.dialog_set_name, null);
        final EditText edit_name = (EditText) mView.findViewById(R.id.write_new_name);

        edit_name.setText(my_name);
        edit_name.setSelection(edit_name.getText().length());
        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.show();
        Button mLogin = (Button) mView.findViewById(R.id.ok_name);
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                my_name=edit_name.getText().toString();
                dialog.dismiss();
                save_data();
                Toast.makeText(MainActivity.this,"name is saved",Toast.LENGTH_SHORT).show();
            }
        });
        Button Cancel = (Button) mView.findViewById(R.id.cancel_name);
        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }
    @Override
    public  void onPause()
    {
        super.onPause();
        save_data();
        if(STARTED) {
        contactManager.bye(my_name);
        contactManager.stopBroadcasting();
        contactManager.stopListening();
        }
        stopListener();
        close_socket();
    }
    @Override
    public void onStop() {

        super.onStop();
        save_data();
        stopListener();
        if(!IN_CALL) {
            finish();
        }
        close_socket();
    }
    void close_socket()
    {
        if (message_socket != null) {
            try {
                message_socket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (call_socket != null) {
            call_socket.close();
        }
        if (file_socket != null) {
            try {
                file_socket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
       @Override
    protected void onDestroy() {
        super.onDestroy();
           save_data();
        close_socket();
    }
    public static void add_message(String sender_reciever,String msg)
    {
        int hour=new Time(System.currentTimeMillis()).getHours();
        int minute=new Time(System.currentTimeMillis()).getMinutes();
        String currenttime=hour+":"+minute;
        name_list.add(sender_reciever);
        date_list.add(currenttime);
        message_list.add(msg);
        adapter_messge.notifyDataSetChanged();
    }
    public  void set_message_adapter()
    {
        final ListView list_message = (ListView) findViewById(R.id.scroll_message);
        adapter_messge = new Show_message(this,name_list,date_list,message_list);
        list_message.setAdapter(adapter_messge);

        list_message.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long id)
            {
                String [] direction_and_name = name_list.get(position).split(" ", 2);
                String format_string="<small>"+direction_and_name[0]+"</small>"+" <big>"+direction_and_name[1]+"</big>";
                display_message(message_list.get(position),direction_and_name[1]);
            }

        });
    }

    public String getIpAddress() {
        String ip = "0";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip =inetAddress.getHostAddress();
                    }

                }

            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }

        return ip;
    }

    public  void verify_conncetion()
    {
        this.registerReceiver(this.mConnReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        this.registerReceiver(this.mReceiver, new IntentFilter("android.net.wifi.WIFI_AP_STATE_CHANGED"));
    }
    public  void find_connected_people()
    {
        RecyclerView people = (RecyclerView) findViewById(R.id.recycler_view);
        // people.setHasFixedSize(true);

        RecyclerView.LayoutManager Layout_manager_people_connected = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        people.setLayoutManager(Layout_manager_people_connected);

        Connected_people_adapter = new Show_connected_people(MainActivity.this, names_list, images_list);
        people.setAdapter(Connected_people_adapter);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

       // Toast.makeText(MainActivity.this,(menu==null)+"",Toast.LENGTH_LONG).show();
        return super.onCreateOptionsMenu(menu);

    }
    void display_message(String msg,String name)
    {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.dialog_display_message, null);
        TextView message1 = (TextView) mView.findViewById(R.id.message_to_dispaly);
        TextView name1 = (TextView) mView.findViewById(R.id.name_of_sender);
        message1.setText(msg);
        name1.setText(name);
        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.show();
        ImageView message_vu = (ImageView) mView.findViewById(R.id.vu);
        message_vu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }
    public void quit() {
        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);
        System.exit(0);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id==R.id.edit_name)
            set_my_name();
        if(id==R.id.exit)
        {
            save_data();
            quit();
        }
        if(id==R.id.save_data)
            save_data();
        if(id==R.id.delete_option)
            delete_message();
        if (id == R.id.refresh) {
            refresh_Contact_List();
            Toast.makeText(MainActivity.this,"refresh",Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    public final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(action)) {
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);

                if (WifiManager.WIFI_STATE_ENABLED == state % 10) {
                    is_connected_hotspot=true;
                }
                else
                    is_connected_hotspot=false;

            }
        }
    };
    public BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            NetworkInfo currentNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if(currentNetworkInfo.isConnected()){
                is_connected_wifi=true;
            }
            else {
                is_connected_wifi=false;
            }

        }
    };

}
