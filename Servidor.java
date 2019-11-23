import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import java.util.Calendar;
import java.text.SimpleDateFormat;

public class Servidor extends Thread {

private static ArrayList<BufferedWriter>clientes;  
private static ArrayList<Boolean> salas; 
private String Sala1 = "[Sala 1]";
private String Sala2 = "[Sala 2]"; 
//lista de ultimas mensagens
private static ArrayList<String> last_msgs;           
private static int num_msg_buffer = 5;
private static ServerSocket server; 
private String nome;
private Socket con;
private InputStream in;  
private InputStreamReader inr;  
private BufferedReader bfr;
private String SAIR = "__Sair";

/**
  * Método construtor 
  * @param com do tipo Socket
  */
  public Servidor(Socket con){
    this.con = con;
    try {
          in  = con.getInputStream();
          inr = new InputStreamReader(in);
           bfr = new BufferedReader(inr);
    } catch (IOException e) {
           e.printStackTrace();
    }                          
 }

 /**
  * Método run
  */
public void run(){
                       
    try{
                                        
      String msg;
      OutputStream ou =  this.con.getOutputStream();
      Writer ouw = new OutputStreamWriter(ou);
      BufferedWriter bfw = new BufferedWriter(ouw); 
      clientes.add(bfw);
      nome = msg = bfr.readLine();
      System.out.println(nome);
      getCurrentTime(); 
      String msg_send = getCurrentTime()+"... "+msg+" entrou no chat!";
      sendToOneServer(bfw,returnLastMensagens());
      
      sendToAllServer(bfw, getCurrentTime()+"... "+msg+" entrou no chat!");
      //checkLastMensagens(msg_send);

      //System.out.println(clientes);     
      while(!SAIR.equalsIgnoreCase(msg) && msg != null)
        {           
         msg = bfr.readLine();
         sendToAll(bfw, msg);
         System.out.println(msg);                                              
         }  
         msg_send = getCurrentTime()+"... "+nome+" saiu do chat!";
         sendToAllServer(bfw, msg_send);
         //checkLastMensagens(msg_send);
         clientes.remove(bfw);
                                      
     }catch (Exception e) {
       e.printStackTrace();
       
      
     }   
     //System.out.println(clientes);
     
                      
  }

  public void checkLastMensagens(String msg){
      if(last_msgs.size()>num_msg_buffer){
          last_msgs.remove(0);
      }     
      last_msgs.add(msg);
  }

  public String returnLastMensagens(){
    String msg="";
    for (int i=0;i<last_msgs.size();i++){
      msg += last_msgs.get(i);
    }

    return msg;
  }

  /***
 * Método usado para enviar mensagem para todos os clients
 * @param bwSaida do tipo BufferedWriter
 * @param msg do tipo String
 * @throws IOException
 */
public void sendToAll(BufferedWriter bwSaida, String msg) throws  IOException 
{
  if(SAIR.equalsIgnoreCase(msg)) return;
  BufferedWriter bwS;
  boolean sala = salas.get(clientes.indexOf(bwSaida));
  String comp;
  if(sala){
    comp = Sala1;
  }else{
    comp = Sala2;
  }
  String msg_ = "("+getCurrentTime()+") "+nome + "\n   -> " + msg+"\r\n";
  for(BufferedWriter bw : clientes){
   bwS = (BufferedWriter)bw;
   //if(!(SAIR.equalsIgnoreCase(msg)&& (bwSaida == bwS))){
     if(salas.get(clientes.indexOf(bwS))==sala){
      bw.write(comp+" "+msg_);
      bw.flush(); 
     }
    
   //}
   

   //if(!(bwSaida == bwS)){
     
   //}
  }  
  checkLastMensagens(msg_);        
}

public void sendToAllServer(BufferedWriter bwSaida, String msg) throws  IOException 
{
  BufferedWriter bwS;

  boolean sala = salas.get(clientes.indexOf(bwSaida));
  String comp;
  if(sala){
    comp = Sala1;
  }else{
    comp = Sala2;
  }
    for(BufferedWriter bw : clientes){
      bwS = (BufferedWriter)bw;

      if(salas.get(clientes.indexOf(bwS))==sala){
        bw.write(comp+" "+msg+"\r\n");
        bw.flush(); 
       }
        
      }

      checkLastMensagens(msg+"\n");
      
}

public void sendToOneServer(BufferedWriter bwSaida, String msg) throws  IOException 
{

    bwSaida.write(msg);
    bwSaida.flush();
      
}

//retornar data
public String getCurrentTime(){
  Calendar calendar = Calendar.getInstance(); // gets current instance of the calendar  SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
  SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
  System.out.println(formatter.format(calendar.getTime()));
  return ""+formatter.format(calendar.getTime());
}

/***
   * Método main
   * @param args
   */
  public static void main(String []args) {
    
    try{
      //Cria os objetos necessário para instânciar o servidor
      JLabel lblMessage = new JLabel("localhost");
      JTextField txtPorta = new JTextField("12345");
      Object[] texts = {lblMessage, txtPorta };  
      JOptionPane.showMessageDialog(null, texts);
      server = new ServerSocket(Integer.parseInt(txtPorta.getText()));
      clientes = new ArrayList<BufferedWriter>();
      salas = new ArrayList<Boolean>();
      last_msgs = new ArrayList<String>();

      JOptionPane.showMessageDialog(null,"Servidor ativo na porta: "+         
      txtPorta.getText());
      boolean salaChoose = true;
       while(true){
         System.out.println("Aguardando conexão...");
         Socket con = server.accept();
         System.out.println("Cliente conectado...");
         Thread t = new Servidor(con);
         t.start();   
         salas.add(salaChoose);
         salaChoose = !salaChoose;
      }
                                
    }catch (Exception e) {
      
      e.printStackTrace();
    }                       
   }// Fim do método main                      
  } //Fim da classe