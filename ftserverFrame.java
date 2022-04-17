import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

class RequestProcessor extends Thread
{
private Socket socket;
private FTServerFrame f;
String id;
RequestProcessor(Socket socket,FTServerFrame f,String id)
{
this.f=f;
this.socket=socket;
this.id=id;
start();
}
public void run()
{
try
{
String sm="Client connected and ID alloted is : "+id;
SwingUtilities.invokeLater(()->{
RequestProcessor.this.f.setData(sm);
});
OutputStream os=socket.getOutputStream();
InputStream is=socket.getInputStream();
byte header[]=new byte[1024];
byte tmp[]=new byte[1024];
int bytesReadCount;
int i=0;
int j=0;

while(j<1024)
{
bytesReadCount=is.read(tmp);
if(bytesReadCount==-1) continue;
for(int k=0;k<bytesReadCount;k++)
{
header[i]=tmp[k];
i++;
}
j=j+bytesReadCount;
}
Long lengthOfFile=(long)0;
String name;
i=0;
j=1;
while(header[i]!=',')
{
lengthOfFile=lengthOfFile+(header[i]*j);
j=j*10;
i++;
}

i++;
StringBuffer sb=new StringBuffer();
while(i<=1023)
{
sb.append((char)header[i]);
i++;
}
name=sb.toString().trim();
byte ack[]=new byte[1];
ack[0]=1;
os.write(ack,0,1);
os.flush();
String str="Receiving file : "+name+" of length : "+lengthOfFile;
SwingUtilities.invokeLater(()->{
RequestProcessor.this.f.setData(str);
});
File fold=new File("uploads");
if(fold.exists()==false)fold.mkdir();


File file=new File("uploads"+File.separator+name);
FileOutputStream fos=new FileOutputStream(file);
byte bytes[]=new byte[4096];
j=0;
while(j<lengthOfFile)
{
bytesReadCount=is.read(bytes);
if(bytesReadCount==-1) continue;
fos.write(bytes,0,bytesReadCount);
fos.flush();
j=j+bytesReadCount;
}

fos.close();
ack[0]=1;
os.write(ack,0,1);
os.flush();

socket.close();
SwingUtilities.invokeLater(()->{
RequestProcessor.this.f.setData("File Saved To : "+file.getAbsolutePath());
RequestProcessor.this.f.setData("Connection with client whose id is : "+id+" is closed");
});
}catch(Exception e)
{
SwingUtilities.invokeLater(()->{
JOptionPane.showMessageDialog(this.f,e.getMessage());
});
}
}
}

class FTServer extends Thread
{
private ServerSocket serverSocket;
private FTServerFrame f;
private int portNumber;
FTServer(FTServerFrame f,int portNumber)
{
this.f=f;
this.portNumber=portNumber;
try
{
serverSocket=new ServerSocket(portNumber);

start();
}catch(Exception e)
{
SwingUtilities.invokeLater(()->{
JOptionPane.showMessageDialog(this.f,"unable to create server socket at port no. : "+portNumber);
return;
});
}
}

public void run()
{
RequestProcessor requestProcessor;
Socket socket;

try
{
while(true)
{
SwingUtilities.invokeLater(()->{
String g="Server is ready to accept the request at port no. "+portNumber+" :";
FTServer.this.f.setData(g);
});
socket=serverSocket.accept();
requestProcessor=new RequestProcessor(socket,this.f,UUID.randomUUID().toString());
}
}catch(Exception e)
{
SwingUtilities.invokeLater(()->{
FTServer.this.f.setData("\n"+e.getMessage()+"\n");
});
}

}

public void closeServer()
{
try
{
serverSocket.close();
}catch(Exception e)
{
SwingUtilities.invokeLater(()->{
FTServer.this.f.setData(e.getMessage());
});
}
}

}


class FTServerFrame extends JFrame
{
private JLabel jl;
private JTextField jtf;
private JTextArea jta;
private JScrollPane scrollPane;
private JButton jb;
private int startMode=1;
private int stopMode=2;
private int mode;
private Container container;
private FTServer ftserver;
FTServerFrame()
{
super("Server Frame");
int lm=0;
int tm=0;
jl=new JLabel("port No :");
jtf=new JTextField();
jb=new JButton("Start");
jta=new JTextArea();
scrollPane=new JScrollPane(jta,ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
mode=startMode;
addListeners();
container=getContentPane();
container.setLayout(null);
jl.setBounds(lm+150,tm+10,60,40);
jtf.setBounds(lm+215,tm+10,70,30);
jb.setBounds(lm+290,tm+10,70,30);
scrollPane.setBounds(lm+10,tm+50,500,500);
container.add(jl);
container.add(jtf);
container.add(jb);
container.add(scrollPane);
setSize(530,600);
setLocation(740,20);
setVisible(true);
setDefaultCloseOperation(EXIT_ON_CLOSE);
}
public void addListeners()
{
jb.addActionListener(new ActionListener(){

public void actionPerformed(ActionEvent ae)
{
if(mode==startMode)
{
String g;
g=jtf.getText();
if(g.length()==0) 
{
JOptionPane.showMessageDialog(FTServerFrame.this,"Empty port Number");
return;
}
int portNumber;
try
{
portNumber=Integer.parseInt(g);
}catch(Exception e)
{
JOptionPane.showMessageDialog(FTServerFrame.this,"Invalid port Number");
return;
}
ftserver=new FTServer(FTServerFrame.this,portNumber);
jb.setText("Stop");
mode=stopMode;
}
else
{
ftserver.closeServer();
jb.setText("Start");
mode=startMode;
}

}
});

}

public void setData(String data)
{
jta.append(data+"\n");
}

}

class psp
{
public static void main(String gg[])
{
FTServerFrame ftServerFrame=new FTServerFrame();
}
}