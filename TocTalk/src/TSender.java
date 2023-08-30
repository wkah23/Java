import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class TSender extends Thread
{
	Socket socket;
	PrintWriter out = null;
	String name;
	
	public TSender(Socket socket)
	{
		this.socket = socket;
		try
		{
			out = new PrintWriter(this.socket.getOutputStream(), true);
		} 
		catch (Exception e)
		{
			System.out.println("예외 S3 : " + e);
		}
	}
	
	public void run()
	{
		Scanner sc = new Scanner(System.in);
		
		try
		{
			out.println(name);
			
			while (out != null)
			{
				try
				{
					String s = sc.nextLine();
					out.println(s);
					
					if ( s.equals("q") || s.equals("Q") )
						break;
				}
				catch (Exception e)
				{
					System.out.println("예외 S1 : " + e);
				}
			}
			out.close();
			socket.close();
		}
		catch (Exception e)
		{
			System.out.println("예외 S2 : " + e);
		}
		sc.close();
	}
}