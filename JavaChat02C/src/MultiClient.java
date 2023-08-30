import java.io.*;
import java.net.*;
import java.util.Scanner;

public class MultiClient
{
	public static void main(String[] args) throws UnknownHostException, IOException
	{
		System.out.println("이름을 입력해 주세요.");
		Scanner sc = new Scanner(System.in);
		String name = sc.nextLine();
		
		PrintWriter out = null;
		BufferedReader in = null;
		
		try
		{
			String ServerIP = "localhost";
			if (args.length > 0)
				ServerIP = args[0];
			Socket socket = new Socket(ServerIP, 9999);
			System.out.println("서버와 연결이 되었습니다.....");
			
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(
										socket.getInputStream() ));
			
			out.println(name);
			
			System.out.println("Receive : " + in.readLine());
			
			in.close();
			out.close();
			socket.close();
		} 
		catch (Exception e)
		{
			System.out.println("예외 [MultiClient class] : " + e);
		}
	}
}
