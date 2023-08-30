import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;


public class ChatServer2 {

	Map<String, String> listw;
	Map<String, String> inviteM;
	static Connection con;
	static ServerSocket serverSocket = null;
	static Socket socket = null;
	Map<String, PrintWriter> waitUser;									//���� �����
	Map<String, PrintWriter> clientMap;									// ����� ���̵�� ����� ���� -- ���ǿ� �ؽø�		
	Map<String, Map<String, PrintWriter>> roomN;						// ������ �ؽø�	
	Map<String, Map<String, PrintWriter>> roomP;						// ��й�
	Map<String, Integer> capa;											// ������ ����
	Map<String, String> pwd;											// ��й��� ��й�ȣ
	
	//������
	public ChatServer2() {
		//client�� ��½�Ʈ���� ������ �ؽø� ����.
		inviteM = new HashMap<String, String>();
		waitUser = new HashMap<String, PrintWriter>();
		clientMap = new HashMap<String, PrintWriter>();
		roomN = new HashMap<String, Map<String, PrintWriter>>();
		capa = new HashMap<String, Integer>();
		pwd = new HashMap<String, String>();
		listw = new HashMap<String, String>();
		listw.put("/list", "��� ����� ����Ʈ");
		listw.put("/waituser", "���� ���� ����Ʈ");
		listw.put("/rlist", "ä�ù� ����Ʈ ���");
		listw.put("/adword", "���� ��Ģ�� ���� �߰�");
		listw.put("/delword", "���� ���� ��Ģ�� ����");
		listw.put("/agree", "ä�ù� �ʴ� ���� ��ɾ�");
		

		//�ؽ��� ����ȭ ����.
		Collections.synchronizedMap(inviteM);
		Collections.synchronizedMap(waitUser);
		Collections.synchronizedMap(roomN);
		Collections.synchronizedMap(clientMap);
		Collections.synchronizedMap(capa);
		Collections.synchronizedMap(pwd);

	}
	
	public void init()
	{
		try {
			serverSocket = new ServerSocket(9990);
			System.out.println("������ ���۵Ǿ����ϴ�.");
		
			while(true) {
				socket = serverSocket.accept();
				//System.out.println(socket.getInetAddress()+" : " + socket.getPort());
				
				Thread mst = new MultiServerR(socket);	//������ ����(��ȭ�� ���� �˸�, ��ȭ)
				mst.start();									
			}
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			try {
				System.out.println("������ ����˴ϴ�.");
				serverSocket.close();
			}catch(Exception e) {
				
			}
		}
	}
	
	//���� ����� ����Ʈ
	public void waitU(PrintWriter out) {
		Iterator<String> it = waitUser.keySet().iterator();
		String msg = "���� ����� ����Ʈ [";
		while(it.hasNext()) {
			msg += (String)it.next() + ",";
		}
		msg = msg.substring(0, msg.length()-1) + "]";
			
		try {
			out.println(msg);		//����Ʈ ���
		}catch(Exception e) {
		}
	}
	
	//����� ����Ʈ
	public void list(PrintWriter out)
	{
		//��½�Ʈ���� ���������� ���ͼ� �ش� �޼����� ����Ѵ�
		Iterator<String> it = clientMap.keySet().iterator();
		String msg = "����� ����Ʈ [";
		while(it.hasNext()) {
			msg += (String)it.next() + ",";
		}
		msg = msg.substring(0, msg.length()-1) + "]";
			
		try {
			out.println(msg);		//����Ʈ ���
		}catch(Exception e) {
		}
	}

	//��ȭ�� ����Ʈ
	public void roomList(PrintWriter out) {
		Iterator<String> it = roomN.keySet().iterator();
		String msg = "�� ����Ʈ [";
		while(it.hasNext()) {
			msg += (String)it.next() + ",";
		}
		msg = msg.substring(0, msg.length()-1) + "]";
			
		try {
			out.println(msg);		//����Ʈ ���
		}catch(Exception e) {
		}
	}
	
	//�ش�� �����
	public void userR(String title, PrintWriter out) {
		Iterator<String> it = roomN.get(title).keySet().iterator();
		String msg = title + "�� ����� ����Ʈ [";
		while(it.hasNext()) {
			msg += (String)it.next() + ",";
		}
		msg = msg.substring(0, msg.length()-1) + "]";
			
		try {
			out.println(msg);		//����Ʈ ���
		}catch(Exception e) {
		}
	}
	
	//���ӵ� ��� Ŭ���̾�Ʈ�鿡�� �޼����� ����
	public void sendAllMsg(String msg, String name) {

		PreparedStatement pstmt2 = null;
		String sql = null;
		ResultSet rs = null;
		String key = null;
		String word = null;
		String query = null;
		int chk = 0;
		int id2 = 0;
		int id1 = 0;
		
		//�ؽ� �ʰ� ���ͷ����͸� �̿��ؼ� ��½�Ʈ���� ���������� ���ͼ� �ش� �޽����� ���濡 ����Ѵ�.
		Iterator<String> it = clientMap.keySet().iterator();
		
		while(it.hasNext()) {
			chk = 0;
			id2 = 0;
			try {
				key = it.next();
				PrintWriter it_out = (PrintWriter) clientMap.get(key);
				sql = "select * from blackword";
				pstmt2 = con.prepareStatement(sql);
				rs = pstmt2.executeQuery();
				while(rs.next()) {
					word = rs.getString(1);
					if(msg.contains(word)) {
						chk = 1;
						for(int i = 0; i < msg.length(); i++) {
							 it_out.print("*");
						 }
						 it_out.println();
						 break;
					}
				}
				
				
				sql = "select count(*) from $tablename";
				query = sql.replace("$tableName", key);
				pstmt2 = con.prepareStatement(query);
				rs = pstmt2.executeQuery();
				while(rs.next()) {
					id1 = Integer.parseInt(rs.getString(1));
				}
				
				if(id1 > 0) {
					sql = "select * from $tablename";
					query = sql.replace("$tableName", key);
					pstmt2 = con.prepareStatement(query);
					rs = pstmt2.executeQuery();
					
					while(rs.next()) {
						 word = rs.getString(1);
						 if(msg.equals(word)) {
							 id2 = 1;
							 for(int i = 0; i < msg.length(); i++) {
								 it_out.print("*");
							 }
							 it_out.println();
						 }
					}
					
					id1 = 0;
				}
				if(id2 == 0) {
					if(name.equals(""))
							it_out.println(msg);				//���� �޼��� �� �濡 ���
						else
							it_out.println(name + " > " + msg);
				}
			}catch(Exception e) {
				System.out.println("����:" + e);
			}
				
		}
	}
	
	//�� ���� Ŭ���̾�Ʈ�鿡�� �޼��� ����
	public void sendMsg(String msg, String name, String i) {

		//�ؽ� �ʰ� ���ͷ����͸� �̿��ؼ� ��½�Ʈ���� ���������� ���ͼ� �ش� �޽����� ���濡 ����Ѵ�.
		
		Iterator<String> it = roomN.get(i).keySet().iterator();
		String sql = null;
		ResultSet rs = null;
		String word = null;
		String query = "";
		PreparedStatement pstmt2 = null;
		PreparedStatement pstmt3 = null;
		String next = null;
		int chk = 0;
		int id2 = 0;
		int id1 = 0;
		while(it.hasNext()) {
			chk = 0;
			id2 = 0;
			try {
				next = it.next();
				if(!next.equals(name)) {

					PrintWriter it_out = (PrintWriter) roomN.get(i).get(next);
					
					sql = "select * from blackword";
					pstmt2 = con.prepareStatement(sql);
					rs = pstmt2.executeQuery();
					while(rs.next()) {
						word = rs.getString(1);
						if(msg.equals(word)) {
							chk = 1;
							for(int j = 0; j < word.length(); j++) {
								 it_out.print("*");
							 }
							 it_out.println();
							 break;
						}
					}
	//				����!! �ٽñ���
					if(chk == 1) continue;			//blackword�� �ɸ��� �Ʒ��� ���� 
								
				
					sql = "select * from " + next ;
					pstmt2 = con.prepareStatement(sql);
					rs = pstmt2.executeQuery();
					
					while(rs.next()) {
						 word = rs.getString(1);
						 if(msg.equals(word)) {
							 id2 = 1;
							 for(int j = 0; j < msg.length(); j++) {
								 it_out.print("*");
							 }
							 it_out.println();
							 break;
						 }
					}
				
					if(id2 == 1)continue;
					
					if(name.equals(""))
						it_out.println(msg);				//���� �޼��� �� �濡 ���
					else
						it_out.println(name + " > " + msg);
					
				}
			}catch(Exception e) {
				System.out.println("����:" + e);
			}
				
		}
	}
	
	//�����ڿ�
	public void adlogin(BufferedReader in, PrintWriter out) throws IOException, SQLException {
		
		String menu = "";
		String sql = null;
		PreparedStatement pstmt4 = null;
		PreparedStatement pstmt2 = null;
		ResultSet rs = null;
		String id = null;
		String word = null;
		
		while(true) {
			out.println("===�޴�===");
			out.println("1. ������Ʈ ���");
			out.println("2. ������Ʈ ���");
			out.println("3. ������Ʈ ����");
			out.println("4. ������");
			out.println("5. ��Ģ�� ���");
			out.println("6. ��Ģ�� ���");
			out.println("7. ��Ģ�� ����");
			out.println("8. ������");
			out.println();
			menu = in.readLine();
			//������Ʈ ���
			if(menu.equals("1")) {

				sql = "select id from chatuser where utype = ?";
				pstmt2 = con.prepareStatement(sql);
				pstmt2.setString(1,  "1");
				rs = pstmt2.executeQuery();
				out.println("<<������Ʈ ���>>");
				while(rs.next()) {					
					word = rs.getString(1);
					out.println(word);
				}
				out.println();
			}
			//������Ʈ ���
			else if(menu.equals("2")) {
								
				sql = "select id from chatuser";
				pstmt2 = con.prepareStatement(sql);
				rs = pstmt2.executeQuery();
				out.println("<<����� ���̵�>>");
				while(rs.next()) {					
					word = rs.getString(1);
					out.println(word);
				}
				
				out.println("������Ʈ ����� ���ϴ� id�� �Է��ϼ���.");
				id = in.readLine();
				
				sql = "update chatuser set utype = '1' where id = ?";
				pstmt4 = con.prepareStatement(sql); 
				pstmt4.setString(1,  id);
				rs = pstmt4.executeQuery();
				out.println("�Ϸ�Ǿ����ϴ�.");
				out.println();
			}
			//������Ʈ ����
			else if(menu.equals("3")) {
				out.println("������Ʈ ������ ���ϴ� id�� �Է��ϼ���.");
				id = in.readLine();
				sql = "update chatuser set utype = '0' where id = ?";
				pstmt4 = con.prepareStatement(sql); 
				pstmt4.setString(1, id);
				rs = pstmt4.executeQuery();
				out.println("�Ϸ�Ǿ����ϴ�.");
				out.println();
			}
			//������
			else if(menu.equals("4")) {
				roomList(out);
				out.println("�����ĸ� ���ϴ� �� �̸��� �Է��ϼ���");
				id = in.readLine();
				roomN.remove(id);
				out.println(id + "���� ���ĵǾ����ϴ�.");
				out.println();
			}
			//��Ģ�� ���
			else if(menu.equals("5")) {
				out.println("<<��Ģ�� ���>>");
				sql = "select * from blackword";
				pstmt2 = con.prepareStatement(sql);
				rs = pstmt2.executeQuery();
				while(rs.next()) {
					out.println("> " + rs.getString(1));
				}
				out.println();
			}
			//��Ģ�� �߰�
			else if(menu.equals("6")) {
				out.println("<<��Ģ�� �߰�>>");
				out.println("��Ģ�� : ");
				id = in.readLine();
				sql = "insert into blackword values (?)";
				pstmt4 = con.prepareStatement(sql); 
				pstmt4.setString(1, id);
				rs = pstmt4.executeQuery();
				out.println("\'" + id + "\'�� ��Ģ��� �߰��Ǿ����ϴ�. ");
				out.println();
			}
			//��Ģ�� ����
			else if(menu.equals("7")) {
				
				out.println("������ ��Ģ�� : ");
				id = in.readLine();
				sql = "delete from blackword where word = ?";
				pstmt2 = con.prepareStatement(sql); 
				pstmt2.setString(1, id);
				rs = pstmt2.executeQuery();
				out.println("��Ģ� �����Ǿ����ϴ�.");
				out.println();
			}
			
			//������ ����
			else if(menu.equals("8"))
			{
				out.println("������ �������� �����մϴ�.");
				break;
			}else {
				out.println("�߸� �����ϼ̽��ϴ�.");
			}
		}
	}
	//�ӼӸ�
	public void secretMsg(String msg, String name, String nameto, String i) {
		
		PrintWriter p = null;
		try {
			p = (PrintWriter) roomN.get(i).get(nameto);
			if(name.equals(""))
				p.println(msg);				//���� �޼��� �� �濡 ���
			else
				p.println(name + "(�ӼӸ�) > " + msg);
		}catch(Exception e) {
			
		}
		
	}
	
	//�ʴ�
	public void invite( String name, String nameto, String i) {
		PrintWriter p = null;
		inviteM.put(nameto, i);
		try {
			p = (PrintWriter) clientMap.get(nameto);
			p.println(inviteM);
			p.println(name + "�Բ��� " + i + "������ �ʴ��ϼ̽��ϴ�.");
			p.println("���� --> /����");
			p.println("�ź� --> /�ź�");
			
		}catch(Exception e) {
		
		}
	}
	
	
	//����
	public void cutUser(String title, String name, PrintWriter out) {
		
		roomN.get(title).remove(name);
	}
	
	
	public static void main(String[] args) {
		
		try {
			//����Ŭ�� ����(������ ����)
			Class.forName("oracle.jdbc.driver.OracleDriver");
		}catch(ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
			System.out.println("����Ŭ ����");
		}

		try {
			con = DriverManager.getConnection(
					"jdbc:oracle:thin:@localhost:1521:xe", //localhost = �����ġ(����� ����ǻ��)
					"scott",
					"tiger");
		}catch(SQLException sqle) {System.out.println("�θ��̷� ����");}
		
		//������ü ����
		ChatServer2 ms = new ChatServer2();
		ms.init();
	
	}
	

////////////////////////////////////////////////////////////////////////////////////////////
/////////////���� --> ���� �����ϰų� �濡 ������ �� ����
	class MultiServerR extends Thread
	{
		String sql1 = null;
		String sql = null;
		PreparedStatement pstmt1 = null;
		PreparedStatement pstmt2 = null;
		PreparedStatement pstmt3 = null;
		PreparedStatement pstmt4 = null;
		ResultSet rs = null;
		
		Socket socket;
		PrintWriter out = null;
		BufferedReader in = null;
		String name = "";
		String title = null;
		
		//������
		public MultiServerR(Socket socket) {
			this.socket = socket;
			
			try {
				out = new PrintWriter(socket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			}catch(Exception e) {
				
			}
		}
		
		//�����带 ����ϱ� ���� run()�޼��� ������
		@Override
		public void run()
		{
			int type = 0;
			String s = "";
			String id = null;			//���̵�
			String pw = null;			//��й�ȣ
			int th = 0;
			Iterator<String> iter = null;
			String ss = "";
			String key = null;
			String getMenu = "";

			out.println("=============================================");
			out.println("==================����====================");
			out.println("=============================================");
			out.println();
			
			try {
				String pw1 = null;	//��й�ȣ �񱳿� ���
				int ld =0;
				while(true) {
					
					ld = 0;
					out.println("������ �α����� /adlogin�� �Է����ּ���");
					out.println("=============================================");
					out.println("�޴��� �����ϼ���");
					out.println("1. �α���  2. ȸ������ 3. ȸ��Ż��");
					out.println("=============================================");

					try {
						getMenu = in.readLine();
						
					}catch (IOException e) {
						
					}
					
					if(getMenu.equals("/adlogin")) {
						out.println("<<������ �������Դϴ�>> \n ��й�ȣ�� �Է����ּ���");
						pw = in.readLine();
						sql = "select pw from chatuser where id = ?";
						pstmt1 = con.prepareStatement(sql);
						pstmt1.setString(1, "head");
						rs = pstmt1.executeQuery();
						
						while(rs.next()){
							pw1 = rs.getString(1);
							if(pw.equals(pw1)) {
								out.println("ȯ���մϴ� �����ڴ�");
								adlogin(in, out);
							}else {
								out.println("��й�ȣ�� Ʋ�Ƚ��ϴ�.");
							}
							break;
						}
					}
					else {
						
						type = Integer.parseInt(getMenu);
						//�α���
						if(type == 1) {	
							
							int id1 = 0;
							String id11 = null;
								
							out.println("<<�α���>>");
							out.println("id");
							try {
								id = in.readLine();
								
								if(id.equals("back")) continue;
								sql = "select count(*) from chatuser where id = ?";
								pstmt2 = con.prepareStatement(sql);
								pstmt2.setString(1, id);
								rs = pstmt2.executeQuery();
								while(rs.next()) {
									id1 = Integer.parseInt(rs.getString(1));
								}
								if(id1 == 0) {
									out.println("�������� �ʴ� ���̵� �Դϴ�.");
									continue;
								}
								
								sql = "select utype from chatuser where id = ?";
								pstmt1 = con.prepareStatement(sql); 
								pstmt1.setString(1, id);
								rs = pstmt1.executeQuery();
								
								while(rs.next()) {
									int utype = Integer.parseInt(rs.getString(1));
									if(utype == 1) {
										out.println("�α����� ������ ���̵��Դϴ�.");
										ld = 1;
									}
								}
								
							}catch (IOException e1) {
								e1.printStackTrace();
							}
							if(ld == 1) continue;	
							
							id1 = 0;
							Iterator<String> at = clientMap.keySet().iterator();
							while(at.hasNext()) {
								id11 = (String)at.next();
								if(id.equals(id11)) {
									id1 = 1;
									break;
								}
							}
							if(id1 == 1){
								out.println("�̹� Ȱ������ ���̵��Դϴ�.");
							}
							else {
								out.println("pw ");
								try {
									pw = in.readLine();
								} catch (IOException e1) {
									
								}
									
											
								try {
									try {
										sql = "select pw from chatuser where id = ?";
										pstmt1 = con.prepareStatement(sql); 
										pstmt1.setString(1, id);
										rs = pstmt1.executeQuery();
											
										while(rs.next()) {
											pw1 = rs.getString(1);
										}
									}catch(SQLException sqle) {
										
										System.out.println("sql�� ����");
									}
										
									if(pw.equals(pw1)) {
										clientMap.put(id, out);
										waitUser.put(id, out);
										out.println("�α��ο� �����߽��ϴ�");
										Thread mst = new MultiServer2(socket,id,title);	//������ ����(��ȭ�� ���� �˸�, ��ȭ)
										th = 1;
										mst.start();
										mst.join();
									}else {
										out.println("�α��ο� �����߽��ϴ�");
									}
								}finally {
									try {
										if(rs != null) rs.close();
										if(pstmt1 != null) pstmt1.close();
										if(pstmt2 != null) pstmt2.close();
									}catch(SQLException sqle){}
								}
							} 
						}
							
						//ȸ������
						else if(type == 2) {
							try {
								out.println("<<ȸ������>>");
								int id1 = 0;
										
								while(true) {
									
									out.println("id: ");
									id = in.readLine();		
											
									try {
										if(id.equals("back")) break;
										sql = "select count(*) from chatuser where id = ?";
										pstmt2 = con.prepareStatement(sql);
										pstmt2.setString(1, id);
										rs = pstmt2.executeQuery();
										while(rs.next()) {
											id1 = Integer.parseInt(rs.getString(1));
										}
										if(id1 == 0) {
											out.println("pw: ");
											pw = id = in.readLine();	
											sql = "insert into chatuser(id, pw, utype) values(?, ?, 0)";
											pstmt3 = con.prepareStatement(sql); 
											pstmt3.setString(1, id);
											pstmt3.setString(2, pw);
											rs = pstmt3.executeQuery();
									
											//���� ��Ģ�� ���̺� ����
											String strQuery = "create table $tableName (wordd varchar(10) )";
											String query =strQuery.replace("$tableName",id);
											pstmt4 =con.prepareStatement(query);       
											rs = pstmt4.executeQuery();
											
											out.println("ȸ������ �Ǿ����ϴ�.");
											break;
											
										}else {
											out.println("�̹� �����ϴ� ���̵��Դϴ�.");
											break;
										}
									
									}catch(SQLException sqle) {
										sqle.printStackTrace();
									}finally {
										try {
											if(rs != null) rs.close();
											if(pstmt2 != null) pstmt2.close();
											if(pstmt3 != null) pstmt3.close();
										}catch(SQLException sqle){}
											
									}
								}
								//break;
							} catch (IOException e) {							
								
							}
						}
						//ȸ��Ż��
						else if(type == 3) {
							int id1 = 0;
							out.println("<<ȸ�� Ż��>>");
							out.println("id: ");
							id = in.readLine();	
							
							if(id.equals("back")) continue;
							
							sql = "select count(*) from chatuser where id = ?";
							pstmt2 = con.prepareStatement(sql);
							pstmt2.setString(1, id);
							rs = pstmt2.executeQuery();
							while(rs.next()) {
								id1 = Integer.parseInt(rs.getString(1));
							}
							if(id1 == 0) {
								out.println("�������� �ʴ� ���̵� �Դϴ�.");
								continue;
							}
							out.println("pw: ");
							pw = in.readLine();	
							
							sql = "select pw from chatuser where id = ?";
							pstmt1 = con.prepareStatement(sql); 
							pstmt1.setString(1, id);
							rs = pstmt1.executeQuery();
								
							while(rs.next()) {
								pw1 = rs.getString(1);
							}
							
							if(pw.equals(pw1)) {
								
								out.println("���� Ż���Ͻðڽ��ϱ�? Y/N");
								String o = in.readLine();
								if(o.equals("Y") || o.equals("y")) {
									sql = "delete from chatuser where id = ?";
									pstmt3 = con.prepareStatement(sql); 
									pstmt3.setString(1, id);
									rs = pstmt3.executeQuery();
									

									sql = "drop table " + id;
									pstmt4 = con.prepareStatement(sql);
					
									rs = pstmt4.executeQuery();
									out.println("Ż��Ǿ����ϴ�.");
									continue;
								}
							}
						}
					else {
						System.out.println(" �޴��� �ٽ� �����ϼ���");
					}			
				}
			}
			}catch(Exception e) {
				System.out.println("����1:" +e);
			}finally {
				//���ܰ� �߻��Ҷ� ����. �ؽ��ʿ��� �ش� ������ ����.
				//���� �����ϰų� ������ java.net.SocketException: ���ܹ߻�
				if(th == 0) {
					clientMap.remove(name);
					sendAllMsg(name + "���� �����ϼ̽��ϴ�.", "");
					System.out.println("00���� ������ ���� " + clientMap.size()+"�� �Դϴ�.");
				}
				
				try {
					serverSocket.close();
					in.close();
					out.close();
				}catch(Exception e) {
					
				}
			}
		}
	
	}

////////////////////////////////////////////////////////////////////////////////////
///////�α��� ���� ������ �ܰ�
	class MultiServer2 extends Thread
	{
		
		Socket socket;
		String title = null;
		PrintWriter out = null;
		BufferedReader in = null;
		String id = "";
		String pw = null;
		
		//������
		public MultiServer2(Socket socket, String name, String title) {
			this.socket = socket;
			this.id = name;
			this.title = title;
			try {
				out = new PrintWriter(socket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			}catch(Exception e) {
				
			}
		}
		
		//�����带 ����ϱ� ���� run()�޼��� ������
		@Override
		public void run()
		{	
			String sql1 = null;
			String sql = null;
			PreparedStatement pstmt1 = null;
			PreparedStatement pstmt2 = null;
			PreparedStatement pstmt3 = null;
			PreparedStatement pstmt4 = null;
			ResultSet rs = null;
			
			Iterator<String> iter = null;
			String ss = "";
			String menu = null;
			int count = 0;
			int th = 0;
			String type = null;
				
			try {
				
				
				System.out.println("==============================================");
				System.out.println("	" + id + "���� �����ϼ̽��ϴ�.	");
				System.out.println("==============================================");
				
				System.out.println();
				System.out.println("==============================================");
				System.out.println("==============================================");
				System.out.println("	���� ��ü ������ ���� " +clientMap.size() + "�� �Դϴ�.");
				System.out.println("==============================================");
				System.out.println("==============================================");
				System.out.println();
				
				while(true){
					
					
					out.println("/* --> ��ɾ� ����");
					out.println("���ϴ� �޴��� �����ϼ���\n"
								+ "1. ����ä�ù� �����\n"
								+ "2. ���ä�ù� �����\n"
								+ "3. ��ü �� ����Ʈ ����\n"
								+ "4. ä�ù� �����ϱ�");
					menu = in.readLine();
					type = menu;
					if(menu.equals("q")) {break;}

					else if(menu.startsWith("/")) {
						if(menu.equals("/*")) {
							iter = listw.keySet().iterator();
							out.println("<<��ɾ�>>");
							while(iter.hasNext()) {
								String key = iter.next();
								out.println(">> " + key + " :		" + listw.get(key));
							}
							out.println(">> /userlist :		�� ������ ����Ʈ");
							
						}
						else if(menu.equals("/����")) {
							title = inviteM.get(id);
							try {
								
								if(id.equals(""))
									sendMsg("�����ϼ̽��ϴ�", id, title);
								else 
									sendMsg("����",id, title);
								Thread mst = new MultiServerT(socket, id, title);	//������ ����(��ȭ�� ���� �˸�, ��ȭ)
								mst.start();
								mst.join();	
								//continue;
								
							}catch(Exception e) {
								out.println("����");
							}
						}else if(menu.equals("/�ź�")) {
							try {
								title = inviteM.get(id);
								inviteM.remove(id);
								
								if(id.equals(""))
									sendMsg("�ź��ϼ̽��ϴ�", id, title);			//���� �޼��� �� �濡 ���
								else
									sendMsg("�ź�", id, title);
								
							}catch(Exception e) {
								out.println("����");
							}
							
						}
						else if(menu.equals("/list")) {
							list(out);
							
						}
						
						//�渮��Ʈ
						else if(menu.equals("/rlist")){
							roomList(out);
							
						}
						//���� ����� ����Ʈ
						else if(menu.equals("/waituser")) {
							waitU(out);
						}
						
						//��Ģ�� �߰�
						else if(menu.equals("/adword")) {
							out.println("<<��Ģ�� �߰�>>");
							out.println("��Ģ�� : ");
							ss = in.readLine();
							sql = "insert into $tableName values (?)";
							String query =sql.replace("$tableName",id);
							pstmt1 = con.prepareStatement(query); 
							pstmt1.setString(1, ss);
							rs = pstmt1.executeQuery();
							out.println("\'" + ss + "\'�� ��Ģ��� �߰��Ǿ����ϴ�. ");
						
						}
						
						//��Ģ�� ����
						else if(menu.equals("/delword")) {
							sql = "select * from $tableName";
							String query = sql.replace("$tableName", id);
							pstmt2 = con.prepareStatement(query);
							rs = pstmt2.executeQuery();
							while(rs.next()) {
								out.println("> " + rs.getString(1));
							}
							
							out.println("������ ��Ģ�� : ");
							ss = in.readLine();
							sql = "delete from $tableName where wordd = ?";
							String query1 =sql.replace("$tableName",id);
							pstmt3 = con.prepareStatement(query1); 
							pstmt3.setString(1, ss);
							rs = pstmt3.executeQuery();
							out.println("��Ģ� �����Ǿ����ϴ�.");
							
						}
						
						else if(menu.equals("/userlist")) {
							userR(title, out);
						}
					}
					
						//1�� ���ý� ���߰�
				
					else if(type.equals("1")) {
						
						out.println(" �������� �����մϴ�");
						out.println("========================");
							out.println("���̸��� ������ �ּ���: ");
							title = in.readLine();
							
							out.println("���� ������ �����ϼ���: ");
							count = Integer.parseInt(in.readLine());
							
							//���� �����ص� �ؽøʿ� ������ �ؽø� �߰�
							roomN.put(title, new HashMap<String, PrintWriter>(count));
							capa.put(title, count);
							waitUser.remove(id);
							
							th = 1;
							Thread mst = new MultiServerT(socket, id, title);	//������ ����(��ȭ�� ���� �˸�, ��ȭ)
							mst.start();
							mst.join();
						
							
						}
						
						//2�� ���ý� ��й��߰�
						else if(type.equals("2")) {
							
					
							out.println("  ��й��� �����մϴ�");
							out.println("========================");
							out.println("���̸��� ������ �ּ���: ");
							title = in.readLine();
							
							out.println("���� ������ �����ϼ���: ");
							count = Integer.parseInt(in.readLine());
							
							
							out.println("��й�ȣ�� �Է��ϼ���: ");
							pw = in.readLine();
							
							//���� �����ص� �ؽøʿ� ������ �ؽø� �߰�
							roomN.put(title, new HashMap<String, PrintWriter>(count));
							capa.put(title, count);
							pwd.put(title, pw);
							waitUser.remove(id);
							
							th = 1;
							Thread mst = new MultiServerT(socket, id, title);	//������ ����(��ȭ�� ���� �˸�, ��ȭ)
							mst.start();
							mst.join();
						
	
						}
						
						//3�� ���ý� ��ü �� ����Ʈ
						else if(type.equals("3")) {
							roomList(out);
						}
						
						//4�� ���ý� ������ ä�ù� �����ϱ�
						else if(type.equals("4")) {
								menu = null;
								out.println("������ ���ϴ� �� �̸��� �Է��� �ּ���: ");
								title = in.readLine();
								out.println();
								
								//�Է��� ���̸� ����
								if(!(roomN.containsKey(title))) {
									out.println("�������� �ʴ� ���Դϴ�.");
								}
								
								////////�����ʰ� ���� �ȵ�~~~~~~~~~~~~!!!!!!!!!!!!!!!!
								else if(roomN.get(title).size() == capa.get(title)) {
									out.println("�����ʰ��� �����Ͻ� �� �����ϴ�.");
								} 			
								
								//��й� ����
								else if(pwd.containsKey(title)) {
									
									out.println("��й�ȣ�� �Է����ּ���: ");
									pw = in.readLine();
										
									//��й�ȣ ����
									if(pwd.get(title).equals(pw)) {
										//roomN.get(title).put(name, out);
										System.out.println("\'" + title + "\' �濡 �����ϼ̽��ϴ�.");
					
											
										Thread mst = new MultiServerT(socket, id, title);	//������ ����(��ȭ�� ���� �˸�, ��ȭ)
										th = 1;
										waitUser.remove(id);
										mst.start();
										mst.join();
											
										//��й�ȣ Ʋ��
									}else {
											out.println("��й�ȣ�� Ʋ�Ƚ��ϴ�");
									}
									
								//�Ϲݹ� ����
								}else {
									//roomN.get(title).put(name, out);
									
									
									Thread mst = new MultiServerT(socket, id, title);	//������ ����(��ȭ�� ���� �˸�, ��ȭ)
									th = 1;
									waitUser.remove(id);
									mst.start();
									mst.join();
									
								}
						}
								
				}	
			}catch(Exception e) {
				
				
			}finally {
				//���ܰ� �߻��Ҷ� ����. �ؽ��ʿ��� �ش� ������ ����.
				//���� �����ϰų� ������ java.net.SocketException: ���ܹ߻�
				if(th == 0) {
					roomN.get(title).remove(id);
					sendMsg(id + "���� �����ϼ̽��ϴ�.", "", title);
					System.out.println("���� ������ ���� " + roomN.get(title).size()+"�� �Դϴ�.");
				}try {}
				catch(Exception e) {
					out.println("������ ����");
				}
			}
		}
	}
	

	
/////////////////////////////////////////////////////////////////////////////////////
////////////////����Ŭ����
////////////////Ŭ���̾�Ʈ�κ��� �о�� �޽����� �ٸ� Ŭ���̾�Ʈ(socket)�� ������ ������ �ϴ� �޼���
	
	class MultiServerT extends Thread
	{
		Socket socket;
		String title = null;
		PrintWriter out = null;
		BufferedReader in = null;
		String name = "";
		
		//������
		public MultiServerT(Socket socket, String name, String title) {
			this.socket = socket;
			this.title = title;
			this.name = name;
			try {
				out = new PrintWriter(socket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			}catch(Exception e) {
				
			}
		
		}
		
		//�����带 ����ϱ� ���� run()�޼��� ������
		@Override
		public void run()
		{
			int th = 0;
			roomN.get(title).put(name, out);
			String s = "";
			String sss = "";

			String ss;
			String sql;
			PreparedStatement pstmt1;
			ResultSet rs;
				
			try {
				
				//���� ��ü�� ������ �ִ� ������ �����ϰ� �ٸ� ����(Ŭ���̾�Ʈ)�鿡��
				//������ �˸�.
				//sendAllMsg("!!!!" + name + "���� �����ϼ̽��ϴ�. !!!!", "");
				
				sendMsg("------" + name + "���� �����ϼ̽��ϴ�------", "", title);
				
				out.println("\'" + title + "\' �濡 �����ϼ̽��ϴ�.");
				out.println();
				out.println("\'" + title + "\'���� ������ " + capa.get(title) + "�� �Դϴ�.");
				out.println("\'" + title + "\'���� ���� ������ ���� " +roomN.get(title).size() + "�� �Դϴ�.");
				out.println("������ ���Ͻø� /������,  /������ �Է����ּ���");
				out.println("��ɾ� ����� ���Ͻø� /*�� �Է��� �ּ���");
				
				//�Է� ��Ʈ���� null�� �ƴϸ� �ݺ�
				while(in != null) {
					
					s = in.readLine();
					//�������������
					//������
					if(!roomN.keySet().contains(title)) {
						out.println("����.");
						th = 1;
						break;
					}
					//����
					if(!roomN.get(title).keySet().contains(name)) {
						out.println("����.");
						th = 1;
						break;
					}
					else {
						
						if(s.startsWith("/")) {
							String s2 = null;
							String n = null;
							String[] s1 = s.split(" ");
							if(s1.length > 1) {
								s2 = s1[0];
								
								//�ӼӸ� ��ɾ�
								if(s2.equals("/to")) {
									n = s1[1];
									out.println(n + "���� �ӼӸ�>>");
									while(in != null) {
										sss = in.readLine();
										if(sss.equals("//stop")) {
											break;
										}else {
											secretMsg(sss, name, n, title); 
										}
									}
									continue;
								}
								//������
								else if(s2.equals("/����")) {
									for(int j = 1; j < s1.length; j++) {
										n = s1[j];
										roomN.get(title).remove(n);
									}
									continue;
								}
								//�ʴ�
								else if(s2.equals("/�ʴ�")) {
									n = s1[1];
									invite(name, n, title);
								}
							}
							
							
							//��ɾ� ����Ʈ
							else if(s.equals("/*")) {
								Iterator<String> iter = listw.keySet().iterator();
								out.println("<<��ɾ�>>");
								while(iter.hasNext()) {
									String key = iter.next();
									out.println(">> " + key + " :		" + listw.get(key));
								}
								out.println(">> /userlist :		�� ������ ����Ʈ");
								out.println(">> /to ������̸�:	�ӼӸ�");
								out.println(">> /�ʴ� ������̸�:����� �ʴ��ϱ�");
								
							}
							
							else if(s.equals("/list")) {
								list(out);
								
							}
							
							//�渮��Ʈ
							else if(s.equals("/rlist")){
								roomList(out);
								
							}
							//���� ����� ����Ʈ
							else if(s.equals("/waituser")) {
								waitU(out);
							
							}
							
							//��Ģ�� �߰�
							else if(s.equals("/adword")) {
								out.println("<<��Ģ�� �߰�>>");
								out.println("��Ģ�� : ");
								ss = in.readLine();
								sql = "insert into $tableName values (?)";
								String query =sql.replace("$tableName",name);
								pstmt1 = con.prepareStatement(query); 
								pstmt1.setString(1, ss);
								rs = pstmt1.executeQuery();
								out.println("\'" + ss + "\'�� ��Ģ��� �߰��Ǿ����ϴ�. ");
					
							}
							
							//��Ģ�� ����
							else if(s.equals("/delword")) {
								sql = "select * from $tableName";
								String query = sql.replace("$tableName", name);
								PreparedStatement pstmt2 = con.prepareStatement(query);
								rs = pstmt2.executeQuery();
								while(rs.next()) {
									out.println("> " + rs.getString(1));
								}
								
								out.println("������ ��Ģ�� : ");
								ss = in.readLine();
								sql = "delete from $tableName where wordd = ?";
								String query1 =sql.replace("$tableName",name);
								pstmt1 = con.prepareStatement(query1); 
								pstmt1.setString(1, ss);
								rs = pstmt1.executeQuery();
								out.println("��Ģ� �����Ǿ����ϴ�.");
								
							}
							else if(s.equals("/userlist")) {
								userR(title, out);
								
							}
							
							else if(s.equals("/������")|| s.equals("/����")) {
								roomN.get(title).remove(name);
								if(roomN.get(title).isEmpty()) {
									roomN.remove(title);
								}
								th = 1;
								break;
							}else {
								out.println("�ùٸ��� ���� ��ɾ� �Դϴ�.");
							}
							
							
						}
						else {
							//sendAllMsg(s, title);
							sendMsg(s, name, title);
							
						}	
					}
					
				}
				
				
			}catch(Exception e) {
				
				
			}finally {
				//���ܰ� �߻��Ҷ� ����. �ؽ��ʿ��� �ش� ������ ����.
				//���� �����ϰų� ������ java.net.SocketException: ���ܹ߻�
				if(th != 1) {
					roomN.get(title).remove(name);
				}
				if(!roomN.get(title).isEmpty())
					sendMsg(name + "���� �����ϼ̽��ϴ�.", "", title);			
				try {
				}catch(Exception e) {
					
				}
			}
				
		}
	
	
	}
}
