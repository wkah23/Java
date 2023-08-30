import java.io.*;
import java.net.*;
import java.util.Scanner;

public class TClient
{
	public static void main(String[] args) throws UnknownHostException, IOException
	{
		try
		{
			String ServerIP = "localhost";
			if (args.length > 0)
				ServerIP = args[0];
			Socket socket = new Socket(ServerIP, 9199);
			System.out.println("서버와 연결이 되었습니다.....");
			
			Thread receiver = new TReceiver(socket);
			receiver.start();
			
			Thread sender = new TSender(socket);
			sender.start();
		}
		catch (Exception e)
		{
			System.out.println("예외 [MultiClient class] : " + e);
		}
	}
}
