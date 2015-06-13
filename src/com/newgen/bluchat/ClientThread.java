package com.newgen.bluchat;

import java.io.IOException;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

public class ClientThread extends Thread 
{
	private  BluetoothSocket socket;
    private final HomeScreen activity;
 
    public ClientThread(BluetoothDevice device,HomeScreen activity) 
    {
        this.activity=activity;
        try 
        {
        	socket = device.createRfcommSocketToServiceRecord(HomeScreen.MY_UUID_SECURE);
        } 
        catch (IOException e) 
        {        	
        }
    }
 
    public void run() 
    {
    	BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
		byte[] buffer = new byte[1024];      		        			

        boolean connected=false;
        int bytes;
        while(true)
        {
        	if(!connected)
        		try 
        		{
        			socket.connect();
        			connected=true;
                    activity.setSocket(socket);
        			Message m=Message.obtain();
        			m.obj="Connected.";
        			activity.showStatus(""+m.obj);
        			activity.hideProgress();
        		} 
        	catch (IOException e) 
        	{
        			Message  m=Message.obtain();
        			m.obj="err"+e;
           			activity.showStatus(""+m.obj);
           		        		
        		}
        	else
        	{
        		try 
        		{
        			Message m=Message.obtain();
           		  	socket.getInputStream().read(buffer);
           		  	m.obj=new String(buffer, "UTF-8");
                	activity.recieveMessage(""+m.obj);		
                		
        		} catch (Exception e) 
        		{
        		}
	      
		}
    }   
}
    public void cancel() {
        try {
            socket.close();
        } catch (IOException e) { }
    }
    public void write(byte[] buffer) {
        try {
        	socket.getOutputStream().write(buffer);

        } catch (IOException e) {
        }
    }
}
