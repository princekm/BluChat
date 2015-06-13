package com.newgen.bluchat;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class ServerThread extends Thread
{

	private final BluetoothServerSocket serverSocket;
	private final HomeScreen activity;
	
    public ServerThread(HomeScreen activity) {
        // Use a temporary object that is later assigned to mmServerSocket,
        // because mmServerSocket is final
        BluetoothServerSocket tmp = null;
        this.activity=activity;
        try {
            // MY_UUID is the app's UUID string, also used by the client code
            tmp = BluetoothAdapter.getDefaultAdapter().listenUsingRfcommWithServiceRecord("name",HomeScreen.MY_UUID_SECURE);
        } catch (Exception e) 
        { 
        
     	   Log.d("bl","err"+e);

        }
        serverSocket = tmp;
    }
 
    public void run() 
    {
        BluetoothSocket socket = null;
        byte[] buffer = new byte[1024];
        int bytes;
        boolean connected=false;
        while (true) 
        {
        	if(!connected)
        		try 
            	{
          	   		activity.showStatus("Searching Friends..");
          	   		socket = serverSocket.accept();
          	   		connected=true;
          	   		activity.setSocket(socket);
          	   		serverSocket.close();              
          	   		activity.showStatus("Connected.");
          	   		activity.hideProgress();
            	}
        		catch (Exception e)
        		{
        			activity.showStatus("error."+e);                   
        			break;
        		}
        	else{
                  	try 
                  	{
                  		  Message m=Message.obtain();
                  		  socket.getInputStream().read(buffer);
	                  	  m.obj=new String(buffer, "UTF-8");
	                  	  activity.recieveMessage(""+m.obj);	
	     			}
                  	catch (IOException e) 
					{
                  		activity.showStatus("error."+e);                   
            			break;
					}	
        		}        			                
            }      
        }
        public void cancel() {
            try {
                serverSocket.close();
            } catch (Exception e) { }
        }
}
