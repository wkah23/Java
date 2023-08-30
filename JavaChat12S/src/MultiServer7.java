import java.io.*;
import java.net.*;
import java.util.*;

public class MultiServer7
{
		ServerSocket serverSocket = null;
		Socket socket = null;
		Map<String, PrintWriter> clientMap;
	   
	   //생성자
	   	public MultiServer7()
	   	{
	       clientMap = new HashMap<String, PrintWriter>();
	       Collections.synchronizedMap(clientMap);
	   	}
	   
	    public void init() 
	    {
		    try 
		    {
		    	serverSocket = new ServerSocket(9999); //9999포트로 서버소켓 객체생성
		    	System.out.println("서버가 시작되었습니다.");
	         
		    	while (true)
		    	{
		    		socket = serverSocket.accept();
		    		System.out.println(socket.getInetAddress() + ":" + socket.getPort());
	            
		    		Thread mst = new MultiServerT(socket); //쓰레드 생성
		    		mst.start(); //쓰레드 시동
		    	}
		    } 
		    catch (Exception e)
		    {
		    	e.printStackTrace();
		    } 
		    finally
		    {
		        try
		        {
		        	serverSocket.close();
		        } 
		        catch (Exception e)
		        {
		            e.printStackTrace();
		        }
		    }
	    }   
	   
	    public void list(PrintWriter out)
	    {
	    	Iterator<String> it = clientMap.keySet().iterator();
		    String msg = "사용자 리스트[";
		    while (it.hasNext()) 
		    {
		    	msg += (String)it.next() + ",";
		    }
		    msg = msg.substring(0, msg.length()-1) + "]";
	      
		    try
		    {
		        out.println(msg);
		    } 
		    catch (Exception e) {}
	    }
	    public void sendAllMsg(String msg, String name)
	    {
	    	Iterator<String> it = clientMap.keySet().iterator();
	      
	    	while (it.hasNext()) 
	    	{
	    		try 
	    		{
	            
		            PrintWriter it_out = (PrintWriter) clientMap.get(it.next());
		            if (name.equals(""))
		            {
		            	it_out.println(msg);
		            } 	
		            else
		            {
		               it_out.println(name + " > " + msg);
		            }
	    		} 
	    		catch (Exception e) 
	    		{
	    			System.out.println("예외:"+e);
	    		}
		    }
		}
	   
	    public static void main(String[] args)
	    {
		    //서버객체 생성
		    MultiServer7 ms = new MultiServer7();
		    ms.init();
	    }
	   ///////////////////////////////////////////////////////////
	   //내부클래스
	   //
	   
	    class MultiServerT extends Thread
	    {
		    Socket socket = null;
		    PrintWriter out = null;
		    BufferedReader in = null;
		    String name = "";
	      
	    //생성자
	    public MultiServerT(Socket socket)
	    {
	        this.socket = socket;
	        try 
	        {
	            out = new PrintWriter(this.socket.getOutputStream(), true);
	            in = new BufferedReader(new InputStreamReader(
	                                this.socket.getInputStream() ));
	        } 
	        catch (Exception e) 
	        {
	            System.out.println("예외:"+e);
	        }
	    }
	      
	    @Override
	    public void run()
	    {
	        String s = "";
	         
	        try 
	        {
	        	name = in.readLine();
	            System.out.println("["+ name + "]님이 대화방에 입장하셨습니다.");
	            
	            //현재 객체가 가지고 있는 소켓을 제외하고 다른 소켓들에게
	            //접속을 알림
	            sendAllMsg(name + "님이 입장하셨습니다.", "");
	            //해쉬맵에 키를 name으로 출력스트림 객체를 저장
	            clientMap.put(name, out);
	            
	            System.out.println("현재 접속자 수는 "
	            		+clientMap.size()+"명 입니다.");
	            
	            while (in!=null) 
	            {
	               s = in.readLine();
	               
	               	if ( s.equals("q") || s.equals("Q") )
	            	   break;
	               
	                System.out.println(name + " > " + s);
	                if (s.equals("/list"))
	                	list(out);
	                else
	                	sendAllMsg(s, name);
	            }
	            System.out.println("쓰레드 종료");
	        } 
	        catch (Exception e)
	        {
	        	System.out.println("예외:"+e);
	        } 
	        finally 
	        {
	            clientMap.remove(name);
	            sendAllMsg(name + "님이 퇴장하셨습니다.", "");
	            System.out.println("현재 접속자 수는 "+clientMap.size()+"명 입니다.");
	            
	            try
	            {
	               in.close();
	               out.close();
	               
	               socket.close();
	            } 
	            catch (Exception e)
	            {
	               e.printStackTrace();
	            }
	        }
	    }
	}   
}	//telnet  127.0.0.1   9999