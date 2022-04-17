import java.awt.*;
import javax.swing.*;
import java.util.*;
import javax.swing.table.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

class FTClient extends Thread
{
private String fileName;
private String ip;
private int port;
private FTClientFrame frm;
ProgressBarPanel pPanel;

public FTClient(String fileName,String ip,int port,FTClientFrame frm,ProgressBarPanel pPanel)
{
this.fileName=fileName;
this.ip=ip;
this.port=port;
this.frm=frm;
this.pPanel=pPanel;
start();
}
public void run()
{
try
{
File file=new File(fileName);
if(file.exists()==false)
{
SwingUtilities.invokeLater(()->{
JOptionPane.showMessageDialog(this.frm,fileName+" does not exists");
});
return;
}
if(file.isDirectory())
{
SwingUtilities.invokeLater(()->{
JOptionPane.showMessageDialog(this.frm,"it is a directory");
});
return;
}
long lengthOfFile=file.length();
String name=file.getName();
byte header[]=new byte[1024];
int i,j;
i=0;
j=1;
long x=lengthOfFile;
while(x>0)
{
header[i]=(byte)(x%10);
i++;
x=x/10;
}
header[i]=(byte)',';
i++;
for(int r=0;r<name.length();r++)
{
header[i]=(byte)name.charAt(r);
i++;
}
while(i<=1023)
{
header[i]=(byte)32;
i++;
}
Socket socket=null;
try
{
socket=new Socket(ip,port);
}catch(Exception exc)
{
SwingUtilities.invokeLater(()->{
JOptionPane.showMessageDialog(this.frm,"Server Not Responding");
});
return;
}
OutputStream os=socket.getOutputStream();
InputStream is=socket.getInputStream();
os.write(header,0,1024);
os.flush();
byte ack[]=new byte[1];
int bytesReadCount;
while(true)
{
bytesReadCount=is.read(ack);
if(bytesReadCount==-1) continue;
break;
}

FileInputStream fis=new FileInputStream(file);
byte bytes[]=new byte[4096];
long incr=lengthOfFile/100;
long inc=incr;
int val=0;
j=0;
while(j<lengthOfFile)
{
if(j>=incr)
{
val++;
incr+=inc;
}
int fl=val;
SwingUtilities.invokeLater(()->{
pPanel.setValue(fl);
});
bytesReadCount=fis.read(bytes);
os.write(bytes,0,bytesReadCount);
os.flush();
j=j+bytesReadCount;
}
SwingUtilities.invokeLater(()->{
pPanel.setValue(100);
});
fis.close();
while(true)
{
bytesReadCount=is.read(ack);
if(bytesReadCount==-1) continue;
break;
}
socket.close();
SwingUtilities.invokeLater(()->{
this.frm.removeNameFromDS(fileName);
this.frm.removeProgressBarPanel(pPanel);
});
}catch(Exception e)
{
System.out.println(e);
}
}
}


class ProgressBarPanel extends JPanel
{
private JProgressBar bar;
private JLabel l;
public ProgressBarPanel()
{
setBorder(BorderFactory.createLineBorder(new Color(160,160,160)));
setLayout(null);
bar=new JProgressBar();
bar.setMinimum(0);
bar.setMaximum(100);
l=new JLabel("");
l.setBounds(10,5,280,10);
bar.setBounds(10,18,280,40);
add(bar,BorderLayout.CENTER);
add(l,BorderLayout.NORTH);
}
public void setValue(int x)
{
this.bar.setValue(x);
}
public void setFileName(String fileName)
{
l.setText(fileName);
}
}




class FTClientFrame extends JFrame
{
private Container container;
private LeftPanel leftPanel;
private RightPanel rightPanel;
private Set<String> set;

public FTClientFrame()
{
super("Client Frame");
set=new LinkedHashSet<>();
container=getContentPane();
container.setLayout(null);
leftPanel=new LeftPanel();
int lm=0,tm=0;
leftPanel.setBounds(lm+1,tm+2,348,698);
rightPanel=new RightPanel();
rightPanel.setBounds(lm+350,tm+2,348,698);
container.add(leftPanel);
container.add(rightPanel);
setSize(711,700);
setLocation(20,20);
setVisible(true);
}

public void removeNameFromDS(String nm)
{
this.leftPanel.filesModel.list.remove(nm);
this.leftPanel.filesModel.fireTableDataChanged();
if(this.leftPanel.filesModel.list.size()==0) 
{
this.leftPanel.serverLabelTextField.setEnabled(true);
this.leftPanel.portNumberLabelTextField.setEnabled(true);
this.leftPanel.selectFileButton.setEnabled(true);
this.leftPanel.startButton.setEnabled(true);
this.leftPanel.statusValue.setText("");
}
}

public void removeProgressBarPanel(ProgressBarPanel pPanel)
{
this.rightPanel.containerPanel.remove(pPanel);
this.revalidate();
this.repaint();
}

class FilesModel extends AbstractTableModel
{
private int serialNumber;
private java.util.List<String> list=new LinkedList<>();
private String []x;
public FilesModel()
{
serialNumber=1;
for(String g:set)
{
list.add(g);
}
x=new String[2];
x[0]="S.NO";
x[1]="File Name";
}
public int getRowCount()
{
return list.size();
}
public int getColumnCount()
{
return x.length;
}
public String getColumnName(int columnIndex)
{
return x[columnIndex];
}
public Object getValueAt(int rowIndex,int columnIndex)
{
if(columnIndex==0) return rowIndex+1;
else return list.get(rowIndex);
}
public boolean isCellEditable(int rowIndex,int columnIndex)
{
return false;
}
public Class getColumnClass(int columnIndex)
{
if(columnIndex==0) return Integer.class;
return String.class;
}
}





// left inner class starts 

public class LeftPanel extends JPanel
{
private JLabel titleLabel;
private JLabel serverLabel,portNumberLabel;
private JTextField serverLabelTextField,portNumberLabelTextField;
private JButton selectFileButton;
private JTable table;
private JScrollPane scrollPane;
private FilesModel filesModel;
private JButton startButton;
private JLabel statusLabel,statusValue;
public LeftPanel()
{
setBorder(BorderFactory.createLineBorder(Color.black));
initComponents();
setAppearence();
addListeners();
}

public void initComponents()
{
titleLabel=new JLabel("Files Selected");
serverLabel=new JLabel("Server");
serverLabelTextField=new JTextField();
portNumberLabel=new JLabel("Port");
portNumberLabelTextField=new JTextField();
selectFileButton=new JButton("Select File");
filesModel=new FilesModel();
table=new JTable(filesModel);
scrollPane=new JScrollPane(table,ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
startButton=new JButton("Start");
statusLabel=new JLabel("Status : ");
statusValue=new JLabel("");
}

public void setAppearence()
{
Font titleFont=new Font("verdana",Font.BOLD,24);
Font titleFont1=new Font("verdana",Font.PLAIN,18);
this.setLayout(null);
this.titleLabel.setFont(titleFont);
this.serverLabel.setFont(titleFont1);
this.portNumberLabel.setFont(titleFont1);
this.statusLabel.setFont(titleFont1);
this.statusValue.setFont(titleFont1);
this.statusValue.setForeground(Color.GREEN);
table.getColumnModel().getColumn(0).setPreferredWidth(45);
table.getColumnModel().getColumn(1).setPreferredWidth(400);
int lm,tm;
lm=0;
tm=0;
this.titleLabel.setBounds(lm+75,tm+10,200,30);
this.serverLabel.setBounds(lm+15,tm+50,70,30);
this.serverLabelTextField.setBounds(lm+90,tm+55,100,25);
this.portNumberLabel.setBounds(lm+205,tm+50,50,30);
this.portNumberLabelTextField.setBounds(lm+260,tm+55,60,25);
this.selectFileButton.setBounds(lm+15,tm+100,110,30);
this.scrollPane.setBounds(lm,tm+140,347,400);
this.startButton.setBounds(lm+250,tm+550,80,30);
this.statusLabel.setBounds(lm+10,tm+620,80,30);
this.statusValue.setBounds(lm+85,tm+620,100,30);
add(this.titleLabel);
add(this.serverLabel);
add(this.serverLabelTextField);
add(this.portNumberLabel);
add(this.portNumberLabelTextField);
add(this.selectFileButton);
add(this.scrollPane);
add(this.startButton);
add(this.statusLabel);
add(this.statusValue);
}

public void addListeners()
{
this.selectFileButton.addActionListener(new ActionListener(){

public void actionPerformed(ActionEvent ae)
{
JFileChooser jfc=new JFileChooser();
jfc.setCurrentDirectory(new File("."));
int selectedOption=jfc.showSaveDialog(LeftPanel.this);
if(selectedOption==jfc.APPROVE_OPTION)
{
File file=jfc.getSelectedFile();
FTClientFrame.this.set.add(file.getAbsolutePath());
LeftPanel.this.filesModel.list.add(file.getAbsolutePath());
LeftPanel.this.filesModel.fireTableDataChanged();
}
}

});

this.startButton.addActionListener(new ActionListener(){

public void actionPerformed(ActionEvent ae)
{
String server=LeftPanel.this.serverLabelTextField.getText().trim();
if(server.length()==0) 
{
JOptionPane.showMessageDialog(LeftPanel.this,"IP Address required");
return;
}
String port=LeftPanel.this.portNumberLabelTextField.getText().trim();
if(port.length()==0) 
{
JOptionPane.showMessageDialog(LeftPanel.this,"Port Number Required");
return;
}
int portNumber=Integer.parseInt(port);
if(LeftPanel.this.filesModel.list.size()==0) 
{
JOptionPane.showMessageDialog(LeftPanel.this,"No File Selected");
return;
}
LeftPanel.this.serverLabelTextField.setEnabled(false);
LeftPanel.this.portNumberLabelTextField.setEnabled(false);
LeftPanel.this.selectFileButton.setEnabled(false);
LeftPanel.this.startButton.setEnabled(false);
LeftPanel.this.statusValue.setText("Connected");
try
{
if(LeftPanel.this.filesModel.list.size()>9)
{
FTClientFrame.this.rightPanel.containerPanel.setLayout(null);
FTClientFrame.this.rightPanel.containerPanel.setLayout(new GridLayout(LeftPanel.this.filesModel.list.size(),1));
FTClientFrame.this.rightPanel.containerPanel.revalidate();
FTClientFrame.this.rightPanel.containerPanel.repaint();
}
for(String fName:LeftPanel.this.filesModel.list)
{
ProgressBarPanel p=new ProgressBarPanel();
p.setFileName(fName);
FTClient t1=new FTClient(fName,server,portNumber,FTClientFrame.this,p);
FTClientFrame.this.rightPanel.containerPanel.add(p);
FTClientFrame.this.revalidate();
FTClientFrame.this.repaint();
}
}catch(Exception e)
{
JOptionPane.showMessageDialog(LeftPanel.this,"Programming error");
return;
}


}

});

}


}
// left inner class ends

// right inner class starts

public class RightPanel extends JPanel
{
private JLabel titleLabel;
private JPanel containerPanel;
private JScrollPane jsp;
public RightPanel()
{
setBorder(BorderFactory.createLineBorder(Color.black));
initComponents();
setAppearence();
addListeners();
}


public void initComponents()
{
titleLabel=new JLabel("Progress");
containerPanel=new JPanel();
jsp=new JScrollPane(containerPanel,ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
}

public void setAppearence()
{
setLayout(null);
Font titleFont=new Font("verdana",Font.BOLD,26);
titleLabel.setFont(titleFont);
//containerPanel.setBorder(BorderFactory.createLineBorder(Color.black));
int lm=0,tm=0;
this.titleLabel.setBounds(lm+75,tm+10,200,30);
this.jsp.setBounds(lm+2,tm+45,344,651);
this.containerPanel.setLayout(new GridLayout(9,1));
add(this.titleLabel);
add(this.jsp);
}

public void addListeners()
{


}


}
// right inner class ends

}


class pspClient
{
public static void main(String gg[])
{
FTClientFrame client=new FTClientFrame();

}
}