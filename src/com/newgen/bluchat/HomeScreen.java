package com.newgen.bluchat;


import java.util.ArrayList;
import java.util.UUID;
import com.newgen.adapter.CustomAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout.LayoutParams;

public class HomeScreen extends Activity {

	private int REQUEST_ENABLE_BT=1,REQUEST_DISCOVERABLE_BT=2;
	private RelativeLayout rootView,dialog,exitDialog,frndList;
	private LinearLayout msgHolder;
	private LayoutInflater inflater;
	private EditText msgBox;
	private BluetoothDevice device;
	private ArrayList<String> list;
	private ServerThread serverThread;
	private ClientThread clientThread;
	private TextView status;
	private ProgressBar progressBar;
	private CustomAdapter<String> adapter;
	private ListView listView;
	private Button listen,search;
	public  static final UUID MY_UUID_SECURE =
	        UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
	private BluetoothSocket socket;
	Handler uiHandler=new Handler()
	{
		@Override
    	public void handleMessage(Message msg) 
		{
	 		TextView textView=(TextView)inflater.inflate(R.layout.server, null); 
     		LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
     		p.setMargins(5,5,5,5);
     		textView.setLayoutParams(p);
    		textView.setText(" "+socket.getRemoteDevice().getName()+" :"+msg.obj);
    		msgHolder.addView(textView);   		 
		}
	};
	Handler temp=new Handler()
	{
		@Override
    	public void handleMessage(Message msg) 
		{
			status.setText(""+msg.obj);
		}
	};
	Handler progOff=
	new Handler()
	{
		@Override
    	public void handleMessage(Message msg) 
		{
			progressBar.setVisibility(View.GONE);
			frndList.setVisibility(View.GONE);
		}
	};
	private final BroadcastReceiver bReceiver = new BroadcastReceiver() 
	{
		public void onReceive(Context context, Intent intent) 
		{
	        String action = intent.getAction();
	        if (BluetoothDevice.ACTION_FOUND.equals(action)) 
	        {
	            device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	        	list.add(device.getAddress()+","+device.getName());	        	
	        	String s[]=new String[list.size()];
	        	for(int i=0;i<s.length;++i)
	        		s[i]=list.get(i);	        	
	   		 	adapter = new CustomAdapter<String>(HomeScreen.this,s);	   	
	    		listView.setAdapter(adapter);
	        }
	    }	
	};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen);
        initComponents();        
        checkBluetooth();
	    
    }
    public void hideProgress()
    {
    	Message m=Message.obtain();    	
    	this.progOff.sendMessage(m);
    }
    private boolean hasBluetooth()
	{
	    return BluetoothAdapter.getDefaultAdapter()!=null?true:false;
	}
	public void enableDiscovery(View view)
	{
				Intent discoverableIntent = new
				Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
				discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
	    	    startActivityForResult(discoverableIntent,this.REQUEST_DISCOVERABLE_BT);
	}
	public void discover(View view) 
	{
    	list.clear();
    	list.add("Devices");
    	       if (BluetoothAdapter.getDefaultAdapter().isDiscovering()) 
    	           BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
    	       else 
    	       {
    	    	   if(BluetoothAdapter.getDefaultAdapter().isEnabled())
    	    		   BluetoothAdapter.getDefaultAdapter().startDiscovery();
    	    	   else
    	    		   toast("Turn on the Bluetooth ");
    	       }   
    	   }

    private void initComponents()
	{
    	
		inflater=(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		dialog=(RelativeLayout)inflater.inflate(R.layout.dialogbox, null);
		LayoutParams p=new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
		dialog.setLayoutParams(p);		
		rootView=(RelativeLayout) this.findViewById(R.id.root);
		rootView.addView(dialog);
		exitDialog=(RelativeLayout)inflater.inflate(R.layout.exitbox, null);
		LayoutParams p2=new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
		exitDialog.setLayoutParams(p2);		
	
		rootView.addView(exitDialog);
		frndList=(RelativeLayout)inflater.inflate(R.layout.friends_list, null);
		LayoutParams p1=new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
		frndList.setLayoutParams(p1);
		rootView.addView(frndList);
		listView=(ListView) frndList.findViewById(R.id.friend_list);
		adapter = new CustomAdapter<String>(this,new String[]{});
		list=new ArrayList();
		listView.setAdapter(adapter);
		msgHolder=(LinearLayout)findViewById(R.id.message_holder);
		msgBox=(EditText)findViewById(R.id.msgbox);
		status=(TextView) this.findViewById(R.id.status);
		progressBar=(ProgressBar) this.findViewById(R.id.progressBar1);
		listen=(Button)this.findViewById(R.id.listen);
		search=(Button)this.findViewById(R.id.search);
		
		OnItemClickListener ol=new OnItemClickListener()
		{
			@Override
			synchronized public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) 
			{
				TextView textView = (TextView) arg1.findViewById(R.id.label);
				String s=textView.getText().toString();
				String addr=s.split(",")[0];
				connect(BluetoothAdapter.getDefaultAdapter().getRemoteDevice(addr));
			}			
		};			
		listView.setOnItemClickListener(ol);

	}
    private void connect(BluetoothDevice device)
    {
    	if(clientThread==null)
    	{
    		clientThread=new ClientThread(device,this);
    		clientThread.start();
    	}
    }
    private void checkBluetooth()
    {
    	if(hasBluetooth())
    	{
    		if(!BluetoothAdapter.getDefaultAdapter().isEnabled())
    			dialog.setVisibility(View.VISIBLE);
    		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND); 		   
 			registerReceiver(bReceiver, filter);    	 		   
    	}
    	
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        getMenuInflater().inflate(R.menu.home_screen, menu);
        return true;
    }
    public void switchOn(View view)
    {
	    
 		Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }
    public void switchOff(View view)
    {
    	finish();
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
        if(requestCode == REQUEST_ENABLE_BT)
        {
             if(BluetoothAdapter.getDefaultAdapter().isEnabled()) 
             {
                 toast("Bluetooth enabled.");
     			dialog.setVisibility(View.GONE);
             }
             else 
             {  
                 toast("Bluetooth disabled.");
             }
        }
        else if(requestCode ==this.REQUEST_DISCOVERABLE_BT)
        {
      	  	if(resultCode==0)
      	  		toast("failure");
      	  	else
      	  		toast("success");
        }
    }
    public void toast(String text)
    {
    	Toast.makeText(this, text,Toast.LENGTH_SHORT).show();
    }
    public void search(View view)
    {
      	list.clear();
        
    	if(frndList.getVisibility()==View.GONE)
    	{
    		
	    	   if(BluetoothAdapter.getDefaultAdapter().isEnabled())
	    	   {
	    		   frndList.setVisibility(View.VISIBLE);
	     		   BluetoothAdapter.getDefaultAdapter().startDiscovery();
	    	   }
	    	   else
	    		   toast("Turn on the Bluetooth ");
    	}
    }
    public void doNothing()
    {
    	
    }
    public void hide(View view)
    {
		frndList.setVisibility(View.GONE);
	       if (BluetoothAdapter.getDefaultAdapter().isDiscovering()) 
	           BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
    }
    public void listen(View view)
    {
    	if(serverThread==null)
    	{
    		serverThread=new ServerThread(this);
    		serverThread.start();
    		progressBar.setVisibility(View.VISIBLE);
    		listen.setText("Cancel");
    	}
    	else
    	{
    		serverThread.stop();
    		serverThread=null;
    		progressBar.setVisibility(View.GONE);
    		listen.setText("Listen");
    		if(socket!=null)
    			status.setText("Disconnected.");
    		else
    			status.setText("Cancelled.");
    	}    	
    }
    public void send(View view)
    {
    	if(!msgBox.getText().toString().trim().equals(""))
    	{
     		try 
    		{
     			byte buffer[]=msgBox.getText().toString().trim().getBytes();        		
    			socket.getOutputStream().flush();
    			socket.getOutputStream().write(buffer,0,msgBox.getText().toString().length());
    			TextView textView=(TextView)inflater.inflate(R.layout.client, null); 
         		LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
         		p.setMargins(5,5,5,5);
         		textView.setLayoutParams(p);
        		textView.setText(" Me: "+msgBox.getText().toString().trim());
        		msgHolder.addView(textView);          		
    			msgBox.setText("");
			}
    		catch (Exception e) 
			{
    			showStatus("Error Occurred.");
			}
    	}
     	InputMethodManager imm = (InputMethodManager)getSystemService(Service.INPUT_METHOD_SERVICE);
    	imm.hideSoftInputFromWindow(msgHolder.getWindowToken(), 0);
    }
	@Override
	public void onBackPressed() 
	{
			if(frndList.getVisibility()==View.VISIBLE)
			{
				frndList.setVisibility(View.GONE);
	    	       if (BluetoothAdapter.getDefaultAdapter().isDiscovering()) 
	    	           BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
			}
			else if(this.exitDialog.getVisibility()==View.VISIBLE)
			{
		    	this.exitDialog.setVisibility(View.GONE);
			}
			else if(this.exitDialog.getVisibility()==View.GONE)
				this.exitDialog.setVisibility(View.VISIBLE);

	}
    public void setText(String s)
    {
    	
    }
    public void setSocket(BluetoothSocket socket)
    {
    	this.socket=socket;
    }
    public void recieveMessage(String s)
    {
    	Message msg=Message.obtain();
    	msg.obj=s;
    	uiHandler.sendMessage(msg);
    }
    public void showStatus(String s)
    {
     	Message msg=Message.obtain();
    	msg.obj=s;
    	temp.sendMessage(msg);
   
    }
    public void exitOk(View view)
    {
    	finish();
    }
    public void exitCancel(View view)
    {
    	this.exitDialog.setVisibility(View.GONE);
    }
   
}
