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
	Map<String, PrintWriter> waitUser;									//대기방 사용자
	Map<String, PrintWriter> clientMap;									// 사용자 아이디와 출력할 내용 -- 대기실용 해시맵		
	Map<String, Map<String, PrintWriter>> roomN;						// 공개방 해시맵	
	Map<String, Map<String, PrintWriter>> roomP;						// 비밀방
	Map<String, Integer> capa;											// 각방의 정원
	Map<String, String> pwd;											// 비밀방의 비밀번호
	
	//생성자
	public ChatServer2() {
		//client의 출력스트림을 저장할 해시맵 생성.
		inviteM = new HashMap<String, String>();
		waitUser = new HashMap<String, PrintWriter>();
		clientMap = new HashMap<String, PrintWriter>();
		roomN = new HashMap<String, Map<String, PrintWriter>>();
		capa = new HashMap<String, Integer>();
		pwd = new HashMap<String, String>();
		listw = new HashMap<String, String>();
		listw.put("/list", "모든 사용자 리스트");
		listw.put("/waituser", "대기방 유저 리스트");
		listw.put("/rlist", "채팅방 리스트 출력");
		listw.put("/adword", "개인 금칙어 설정 추가");
		listw.put("/delword", "개인 설정 금칙어 삭제");
		listw.put("/agree", "채팅방 초대 수락 명령어");
		

		//해쉬맵 동기화 설정.
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
			System.out.println("서버가 시작되었습니다.");
		
			while(true) {
				socket = serverSocket.accept();
				//System.out.println(socket.getInetAddress()+" : " + socket.getPort());
				
				Thread mst = new MultiServerR(socket);	//쓰레드 생성(대화방 입장 알림, 대화)
				mst.start();									
			}
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			try {
				System.out.println("서버가 종료됩니다.");
				serverSocket.close();
			}catch(Exception e) {
				
			}
		}
	}
	
	//대기방 사용자 리스트
	public void waitU(PrintWriter out) {
		Iterator<String> it = waitUser.keySet().iterator();
		String msg = "대기방 사용자 리스트 [";
		while(it.hasNext()) {
			msg += (String)it.next() + ",";
		}
		msg = msg.substring(0, msg.length()-1) + "]";
			
		try {
			out.println(msg);		//리스트 출력
		}catch(Exception e) {
		}
	}
	
	//사용자 리스트
	public void list(PrintWriter out)
	{
		//출력스트림을 순차적으로 얻어와서 해당 메세지를 출력한다
		Iterator<String> it = clientMap.keySet().iterator();
		String msg = "사용자 리스트 [";
		while(it.hasNext()) {
			msg += (String)it.next() + ",";
		}
		msg = msg.substring(0, msg.length()-1) + "]";
			
		try {
			out.println(msg);		//리스트 출력
		}catch(Exception e) {
		}
	}

	//대화방 리스트
	public void roomList(PrintWriter out) {
		Iterator<String> it = roomN.keySet().iterator();
		String msg = "방 리스트 [";
		while(it.hasNext()) {
			msg += (String)it.next() + ",";
		}
		msg = msg.substring(0, msg.length()-1) + "]";
			
		try {
			out.println(msg);		//리스트 출력
		}catch(Exception e) {
		}
	}
	
	//해당방 사용자
	public void userR(String title, PrintWriter out) {
		Iterator<String> it = roomN.get(title).keySet().iterator();
		String msg = title + "방 사용자 리스트 [";
		while(it.hasNext()) {
			msg += (String)it.next() + ",";
		}
		msg = msg.substring(0, msg.length()-1) + "]";
			
		try {
			out.println(msg);		//리스트 출력
		}catch(Exception e) {
		}
	}
	
	//접속된 모든 클라이언트들에게 메세지를 전달
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
		
		//해쉬 맵과 이터레이터를 이용해서 출력스트림을 순차적으로 얻어와서 해당 메시지를 각방에 출력한다.
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
							it_out.println(msg);				//받은 메세지 각 방에 출력
						else
							it_out.println(name + " > " + msg);
				}
			}catch(Exception e) {
				System.out.println("예외:" + e);
			}
				
		}
	}
	
	//방 내의 클라이언트들에게 메세지 전달
	public void sendMsg(String msg, String name, String i) {

		//해쉬 맵과 이터레이터를 이용해서 출력스트림을 순차적으로 얻어와서 해당 메시지를 각방에 출력한다.
		
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

					if(chk == 1) continue;			 
								
				
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
						it_out.println(msg);				//받은 메세지 각 방에 출력
					else
						it_out.println(name + " > " + msg);
					
				}
			}catch(Exception e) {
				System.out.println("예외:" + e);
			}
				
		}
	}
	
	//관리자용
	public void adlogin(BufferedReader in, PrintWriter out) throws IOException, SQLException {
		
		String menu = "";
		String sql = null;
		PreparedStatement pstmt4 = null;
		PreparedStatement pstmt2 = null;
		ResultSet rs = null;
		String id = null;
		String word = null;
		
		while(true) {
			out.println("===메뉴===");
			out.println("1. 블랙리스트 목록");
			out.println("2. 블랙리스트 등록");
			out.println("3. 블랙리스트 해제");
			out.println("4. 방폭파");
			out.println("5. 금칙어 목록");
			out.println("6. 금칙어 등록");
			out.println("7. 금칙어 삭제");
			out.println("8. 나가기");
			out.println();
			menu = in.readLine();
			//블랙리스트 목록
			if(menu.equals("1")) {

				sql = "select id from chatuser where utype = ?";
				pstmt2 = con.prepareStatement(sql);
				pstmt2.setString(1,  "1");
				rs = pstmt2.executeQuery();
				out.println("<<블랙리스트 목록>>");
				while(rs.next()) {					
					word = rs.getString(1);
					out.println(word);
				}
				out.println();
			}
			//블랙리스트 등록
			else if(menu.equals("2")) {
								
				sql = "select id from chatuser";
				pstmt2 = con.prepareStatement(sql);
				rs = pstmt2.executeQuery();
				out.println("<<사용자 아이디>>");
				while(rs.next()) {					
					word = rs.getString(1);
					out.println(word);
				}
				
				out.println("블랙리스트 등록을 원하는 id를 입력하세요.");
				id = in.readLine();
				
				sql = "update chatuser set utype = '1' where id = ?";
				pstmt4 = con.prepareStatement(sql); 
				pstmt4.setString(1,  id);
				rs = pstmt4.executeQuery();
				out.println("완료되었습니다.");
				out.println();
			}
			//블랙리스트 해제
			else if(menu.equals("3")) {
				out.println("블랙리스트 해제를 원하는 id를 입력하세요.");
				id = in.readLine();
				sql = "update chatuser set utype = '0' where id = ?";
				pstmt4 = con.prepareStatement(sql); 
				pstmt4.setString(1, id);
				rs = pstmt4.executeQuery();
				out.println("완료되었습니다.");
				out.println();
			}
			//방폭파
			else if(menu.equals("4")) {
				roomList(out);
				out.println("방폭파를 원하는 방 이름을 입력하세요");
				id = in.readLine();
				roomN.remove(id);
				out.println(id + "방이 폭파되었습니다.");
				out.println();
			}
			//금칙어 목록
			else if(menu.equals("5")) {
				out.println("<<금칙어 목록>>");
				sql = "select * from blackword";
				pstmt2 = con.prepareStatement(sql);
				rs = pstmt2.executeQuery();
				while(rs.next()) {
					out.println("> " + rs.getString(1));
				}
				out.println();
			}
			//금칙어 추가
			else if(menu.equals("6")) {
				out.println("<<금칙어 추가>>");
				out.println("금칙어 : ");
				id = in.readLine();
				sql = "insert into blackword values (?)";
				pstmt4 = con.prepareStatement(sql); 
				pstmt4.setString(1, id);
				rs = pstmt4.executeQuery();
				out.println("\'" + id + "\'가 금칙어로 추가되었습니다. ");
				out.println();
			}
			//금칙어 삭제
			else if(menu.equals("7")) {
				
				out.println("삭제할 금칙어 : ");
				id = in.readLine();
				sql = "delete from blackword where word = ?";
				pstmt2 = con.prepareStatement(sql); 
				pstmt2.setString(1, id);
				rs = pstmt2.executeQuery();
				out.println("금칙어가 삭제되었습니다.");
				out.println();
			}
			
			//페이지 종료
			else if(menu.equals("8"))
			{
				out.println("관리자 페이지를 종료합니다.");
				break;
			}else {
				out.println("잘못 선택하셨습니다.");
			}
		}
	}
	//귓속말
	public void secretMsg(String msg, String name, String nameto, String i) {
		
		PrintWriter p = null;
		try {
			p = (PrintWriter) roomN.get(i).get(nameto);
			if(name.equals(""))
				p.println(msg);				//받은 메세지 각 방에 출력
			else
				p.println(name + "(귓속말) > " + msg);
		}catch(Exception e) {
			
		}
		
	}
	
	//초대
	public void invite( String name, String nameto, String i) {
		PrintWriter p = null;
		inviteM.put(nameto, i);
		try {
			p = (PrintWriter) clientMap.get(nameto);
			p.println(name + "님께서 " + i + "방으로 초대하셨습니다.");
			p.println("수락 --> /수락");
			p.println("거부 --> /거부");
			
		}catch(Exception e) {
		
		}
	}
	
	
	//강퇴
	public void cutUser(String title, String name, PrintWriter out) {
		
		roomN.get(title).remove(name);
	}
	
	
	public static void main(String[] args) {
		
		try {
			//오라클과 연결(데이터 제공)
			Class.forName("oracle.jdbc.driver.OracleDriver");
		}catch(ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
			System.out.println("오라클 실패");
		}

		try {
			con = DriverManager.getConnection(
					"jdbc:oracle:thin:@localhost:1521:xe", //localhost = 사용위치(현재는 내컴퓨터)
					"scott",
					"tiger");
		}catch(SQLException sqle) {System.out.println("두마이런 실패");}
		
		//서버객체 생성
		ChatServer2 ms = new ChatServer2();
		ms.init();
	
	}
	

////////////////////////////////////////////////////////////////////////////////////////////
/////////////대기방 --> 방을 생성하거나 방에 입장할 수 있음
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
		
		//생성자
		public MultiServerR(Socket socket) {
			this.socket = socket;
			
			try {
				out = new PrintWriter(socket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			}catch(Exception e) {
				
			}
		}
		
		//쓰레드를 사용하기 위해 run()메서드 재정의
		@Override
		public void run()
		{
			int type = 0;
			String s = "";
			String id = null;			//아이디
			String pw = null;			//비밀번호
			int th = 0;
			Iterator<String> iter = null;
			String ss = "";
			String key = null;
			String getMenu = "";

			out.println("=============================================");
			out.println("==================대기실====================");
			out.println("=============================================");
			out.println();
			
			try {
				String pw1 = null;	//비밀번호 비교에 사용
				int ld =0;
				while(true) {
					
					ld = 0;
					out.println("관리자 로그인은 /adlogin을 입력해주세요");
					out.println("=============================================");
					out.println("메뉴를 선택하세요");
					out.println("1. 로그인  2. 회원가입 3. 회원탈퇴");
					out.println("=============================================");

					try {
						getMenu = in.readLine();
						
					}catch (IOException e) {
						
					}
					
					if(getMenu.equals("/adlogin")) {
						out.println("<<관리자 페이지입니다>> \n 비밀번호를 입력해주세요");
						pw = in.readLine();
						sql = "select pw from chatuser where id = ?";
						pstmt1 = con.prepareStatement(sql);
						pstmt1.setString(1, "head");
						rs = pstmt1.executeQuery();
						
						while(rs.next()){
							pw1 = rs.getString(1);
							if(pw.equals(pw1)) {
								out.println("환영합니다 관리자님");
								adlogin(in, out);
							}else {
								out.println("비밀번호가 틀렸습니다.");
							}
							break;
						}
					}
					else {
						
						type = Integer.parseInt(getMenu);
						//로그인
						if(type == 1) {	
							
							int id1 = 0;
							String id11 = null;
								
							out.println("<<로그인>>");
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
									out.println("존재하지 않는 아이디 입니다.");
									continue;
								}
								
								sql = "select utype from chatuser where id = ?";
								pstmt1 = con.prepareStatement(sql); 
								pstmt1.setString(1, id);
								rs = pstmt1.executeQuery();
								
								while(rs.next()) {
									int utype = Integer.parseInt(rs.getString(1));
									if(utype == 1) {
										out.println("로그인이 금지된 아이디입니다.");
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
								out.println("이미 활동중인 아이디입니다.");
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
										
										System.out.println("sql문 오류");
									}
										
									if(pw.equals(pw1)) {
										clientMap.put(id, out);
										waitUser.put(id, out);
										out.println("로그인에 성공했습니다");
										Thread mst = new MultiServer2(socket,id,title);	//쓰레드 생성(대화방 입장 알림, 대화)
										th = 1;
										mst.start();
										mst.join();
										continue;
									}else {
										out.println("로그인에 실패했습니다");
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
							
						//회원가입
						else if(type == 2) {
							try {
								out.println("<<회원가입>>");
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
									
											//개인 금칙어 테이블 생성
											String strQuery = "create table $tableName (wordd varchar(10) )";
											String query =strQuery.replace("$tableName",id);
											pstmt4 =con.prepareStatement(query);       
											rs = pstmt4.executeQuery();
											
											out.println("회원가입 되었습니다.");
											break;
											
										}else {
											out.println("이미 존재하는 아이디입니다.");
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
						//회원탈퇴
						else if(type == 3) {
							int id1 = 0;
							out.println("<<회원 탈퇴>>");
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
								out.println("존재하지 않는 아이디 입니다.");
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
								
								out.println("정말 탈퇴하시겠습니까? Y/N");
								String o = in.readLine();
								if(o.equals("Y") || o.equals("y")) {
									sql = "delete from chatuser where id = ?";
									pstmt3 = con.prepareStatement(sql); 
									pstmt3.setString(1, id);
									rs = pstmt3.executeQuery();
									

									sql = "drop table " + id;
									pstmt4 = con.prepareStatement(sql);
					
									rs = pstmt4.executeQuery();
									out.println("탈퇴되었습니다.");
									continue;
								}
							}
						}
					else {
						System.out.println(" 메뉴를 다시 선택하세요");
					}			
				}
			}
			}catch(Exception e) {
				System.out.println("예외1:" +e);
			}finally {
				//예외가 발생할때 퇴장. 해쉬맵에서 해당 데이터 제거.
				//보통 종료하거나 나가면 java.net.SocketException: 예외발생
				if(th == 0) {
					clientMap.remove(name);
					sendAllMsg(name + "님이 퇴장하셨습니다.", "");
					System.out.println("00현재 접속자 수는 " + clientMap.size()+"명 입니다.");
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
///////로그인 이후 방입장 단계
	class MultiServer2 extends Thread
	{
		
		Socket socket;
		String title = null;
		PrintWriter out = null;
		BufferedReader in = null;
		String id = "";
		String pw = null;
		
		//생성자
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
		
		//쓰레드를 사용하기 위해 run()메서드 재정의
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
				System.out.println("	" + id + "님이 입장하셨습니다.	");
				System.out.println("==============================================");
				
				System.out.println();
				System.out.println("==============================================");
				System.out.println("==============================================");
				System.out.println("	현재 전체 접속자 수는 " +clientMap.size() + "명 입니다.");
				System.out.println("==============================================");
				System.out.println("==============================================");
				System.out.println();
				
				while(true){
					
					
					out.println("/* --> 명령어 보기");
					out.println("원하는 메뉴를 선택하세요\n"
								+ "1. 공개채팅방 만들기\n"
								+ "2. 비밀채팅방 만들기\n"
								+ "3. 전체 방 리스트 보기\n"
								+ "4. 채팅방 입장하기\n"
								+ "5. 로그아웃");
					menu = in.readLine();
					type = menu;
					

					if(menu.startsWith("/")) {
						if(menu.equals("/*")) {
							iter = listw.keySet().iterator();
							out.println("<<명령어>>");
							while(iter.hasNext()) {
								String key = iter.next();
								out.println(">> " + key + " :		" + listw.get(key));
							}
							out.println(">> /userlist :		방 참여자 리스트");
							
							
						}
						else if(menu.equals("/수락")) {
							title = inviteM.get(id);
							try {
								
								if(id.equals(""))
									sendMsg("수락하셨습니다", id, title);
								else 
									sendMsg("수락",id, title);
								Thread mst = new MultiServerT(socket, id, title);	//쓰레드 생성(대화방 입장 알림, 대화)
								mst.start();
								mst.join();	
								//continue;
								
							}catch(Exception e) {
							
							}
						}else if(menu.equals("/거부")) {
							try {
								title = inviteM.get(id);
								inviteM.remove(id);
								
								if(id.equals(""))
									sendMsg("거부하셨습니다", id, title);			//받은 메세지 각 방에 출력
								else
									sendMsg("거부", id, title);
								
							}catch(Exception e) {
						
							}
							
						}
						else if(menu.equals("/list")) {
							list(out);
							
						}
						
						//방리스트
						else if(menu.equals("/rlist")){
							roomList(out);
							
						}
						//대기실 사용자 리스트
						else if(menu.equals("/waituser")) {
							waitU(out);
						}
						
						//금칙어 추가
						else if(menu.equals("/adword")) {
							out.println("<<금칙어 추가>>");
							out.println("금칙어 : ");
							ss = in.readLine();
							sql = "insert into $tableName values (?)";
							String query =sql.replace("$tableName",id);
							pstmt1 = con.prepareStatement(query); 
							pstmt1.setString(1, ss);
							rs = pstmt1.executeQuery();
							out.println("\'" + ss + "\'가 금칙어로 추가되었습니다. ");
						
						}
						
						//금칙어 삭제
						else if(menu.equals("/delword")) {
							sql = "select * from $tableName";
							String query = sql.replace("$tableName", id);
							pstmt2 = con.prepareStatement(query);
							rs = pstmt2.executeQuery();
							while(rs.next()) {
								out.println("> " + rs.getString(1));
							}
							
							out.println("삭제할 금칙어 : ");
							ss = in.readLine();
							sql = "delete from $tableName where wordd = ?";
							String query1 =sql.replace("$tableName",id);
							pstmt3 = con.prepareStatement(query1); 
							pstmt3.setString(1, ss);
							rs = pstmt3.executeQuery();
							out.println("금칙어가 삭제되었습니다.");
							
						}
						
						else if(menu.equals("/userlist")) {
							userR(title, out);
						}
					}
					
						//1번 선택시 방추가
				
					else if(type.equals("1")) {
						
						out.println(" 공개방을 생성합니다");
						out.println("========================");
							out.println("방이름을 설정해 주세요: ");
							title = in.readLine();
							
							out.println("방의 정원을 설정하세요: ");
							count = Integer.parseInt(in.readLine());
							
							//방을 저장해둔 해시맵에 참여자 해시맵 추가
							roomN.put(title, new HashMap<String, PrintWriter>(count));
							capa.put(title, count);
							waitUser.remove(id);
							
							th = 1;
							Thread mst = new MultiServerT(socket, id, title);	//쓰레드 생성(대화방 입장 알림, 대화)
							mst.start();
							mst.join();
						
							
						}
						
						//2번 선택시 비밀방추가
						else if(type.equals("2")) {
							
					
							out.println("  비밀방을 생성합니다");
							out.println("========================");
							out.println("방이름을 설정해 주세요: ");
							title = in.readLine();
							
							out.println("방의 정원을 설정하세요: ");
							count = Integer.parseInt(in.readLine());
							
							
							out.println("비밀번호를 입력하세요: ");
							pw = in.readLine();
							
							//방을 저장해둔 해시맵에 참여자 해시맵 추가
							roomN.put(title, new HashMap<String, PrintWriter>(count));
							capa.put(title, count);
							pwd.put(title, pw);
							waitUser.remove(id);
							
							th = 1;
							Thread mst = new MultiServerT(socket, id, title);	//쓰레드 생성(대화방 입장 알림, 대화)
							mst.start();
							mst.join();
						
	
						}
						
						//3번 선택시 전체 방 리스트
						else if(type.equals("3")) {
							roomList(out);
						}
						
						//4번 선택시 선택한 채팅방 입장하기
						else if(type.equals("4")) {
								menu = null;
								out.println("입장을 원하는 방 이름을 입력해 주세요: ");
								title = in.readLine();
								out.println();
								
								//입력한 방이름 없음
								if(!(roomN.containsKey(title))) {
									out.println("존재하지 않는 방입니다.");
								}
								
								////////정원초과 구현 안됨~~~~~~~~~~~~!!!!!!!!!!!!!!!!
								else if(roomN.get(title).size() == capa.get(title)) {
									out.println("정원초과로 입장하실 수 없습니다.");
								} 			
								
								//비밀방 입장
								else if(pwd.containsKey(title)) {
									
									out.println("비밀번호를 입력해주세요: ");
									pw = in.readLine();
										
									//비밀번호 맞음
									if(pwd.get(title).equals(pw)) {
										//roomN.get(title).put(name, out);
										System.out.println("\'" + title + "\' 방에 입장하셨습니다.");
					
											
										Thread mst = new MultiServerT(socket, id, title);	//쓰레드 생성(대화방 입장 알림, 대화)
										th = 1;
										waitUser.remove(id);
										mst.start();
										mst.join();
											
										//비밀번호 틀림
									}else {
											out.println("비밀번호가 틀렸습니다");
									}
									
								//일반방 입장
								}else {
									//roomN.get(title).put(name, out);
									
									
									Thread mst = new MultiServerT(socket, id, title);	//쓰레드 생성(대화방 입장 알림, 대화)
									th = 1;
									waitUser.remove(id);
									mst.start();
									mst.join();
									
								}
						}else if(type.equals("5")) {
							clientMap.remove(id);
							out.println("로그아웃 되었습니다.");
							out.println();
							break;
						}
								
				}	
			}catch(Exception e) {
				
				
			}finally {
				//예외가 발생할때 퇴장. 해쉬맵에서 해당 데이터 제거.
				//보통 종료하거나 나가면 java.net.SocketException: 예외발생
				if(th == 0) {
					roomN.get(title).remove(id);
					sendMsg(id + "님이 퇴장하셨습니다.", "", title);
					System.out.println("현재 접속자 수는 " + roomN.get(title).size()+"명 입니다.");
				}try {}
				catch(Exception e) {
					out.println("방입장 문제");
				}
			}
		}
	}
	

	
/////////////////////////////////////////////////////////////////////////////////////
////////////////내부클래스
////////////////클라이언트로부터 읽어온 메시지를 다른 클라이언트(socket)에 보내는 역할을 하는 메서드
	
	class MultiServerT extends Thread
	{
		Socket socket;
		String title = null;
		PrintWriter out = null;
		BufferedReader in = null;
		String name = "";
		
		//생성자
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
		
		//쓰레드를 사용하기 위해 run()메서드 재정의
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
				
				//현재 객체가 가지고 있는 소켓을 제외하고 다른 소켓(클라이언트)들에게
				//접속을 알림.
				//sendAllMsg("!!!!" + name + "님이 입장하셨습니다. !!!!", "");
				
				sendMsg("------" + name + "님이 입장하셨습니다------", "", title);
				
				out.println("\'" + title + "\' 방에 입장하셨습니다.");
				out.println();
				out.println("\'" + title + "\'방의 정원은 " + capa.get(title) + "명 입니다.");
				out.println("\'" + title + "\'방의 현재 접속자 수는 " +roomN.get(title).size() + "명 입니다.");
				out.println("퇴장을 원하시면 /나가기,  /퇴장을 입력해주세요");
				out.println("명령어 목록을 원하시면 /*를 입력해 주세요");
				
				//입력 스트림이 null이 아니면 반복
				while(in != null) {
					
					s = in.readLine();
					//방빠져나가야함
					//방폭파
					if(!roomN.keySet().contains(title)) {
						waitUser.put(name, out);
						out.println("퇴장.");
						th = 1;
						break;
					}
					//강퇴
					if(!roomN.get(title).keySet().contains(name)) {
						waitUser.put(name, out);
						out.println("퇴장.");
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
								
								//귓속말 명령어
								if(s2.equals("/to")) {
									n = s1[1];
									out.println(n + "에게 귓속말>>");
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
								//강퇴기능
								else if(s2.equals("/강퇴")) {
									for(int j = 1; j < s1.length; j++) {
										n = s1[j];
										roomN.get(title).remove(n);
									}
									continue;
								}
								//초대
								else if(s2.equals("/초대")) {
									n = s1[1];
									invite(name, n, title);
								}
							}
							
							
							//명령어 리스트
							else if(s.equals("/*")) {
								Iterator<String> iter = listw.keySet().iterator();
								out.println("<<명령어>>");
								while(iter.hasNext()) {
									String key = iter.next();
									out.println(">> " + key + " :		" + listw.get(key));
								}
								out.println(">> /userlist :		방 참여자 리스트");
								out.println(">> /to 사용자이름:	귓속말");
								out.println(">> /초대 사용자이름:사용자 초대하기");
								out.println(">> /강퇴 사용자아이디:		강퇴기능");
								
							}
							
							else if(s.equals("/list")) {
								list(out);
								
							}
							
							//방리스트
							else if(s.equals("/rlist")){
								roomList(out);
								
							}
							//대기실 사용자 리스트
							else if(s.equals("/waituser")) {
								waitU(out);
							
							}
							
							//금칙어 추가
							else if(s.equals("/adword")) {
								out.println("<<금칙어 추가>>");
								out.println("금칙어 : ");
								ss = in.readLine();
								sql = "insert into $tableName values (?)";
								String query =sql.replace("$tableName",name);
								pstmt1 = con.prepareStatement(query); 
								pstmt1.setString(1, ss);
								rs = pstmt1.executeQuery();
								out.println("\'" + ss + "\'가 금칙어로 추가되었습니다. ");
					
							}
							
							//금칙어 삭제
							else if(s.equals("/delword")) {
								sql = "select * from $tableName";
								String query = sql.replace("$tableName", name);
								PreparedStatement pstmt2 = con.prepareStatement(query);
								rs = pstmt2.executeQuery();
								while(rs.next()) {
									out.println("> " + rs.getString(1));
								}
								
								out.println("삭제할 금칙어 : ");
								ss = in.readLine();
								sql = "delete from $tableName where wordd = ?";
								String query1 =sql.replace("$tableName",name);
								pstmt1 = con.prepareStatement(query1); 
								pstmt1.setString(1, ss);
								rs = pstmt1.executeQuery();
								out.println("금칙어가 삭제되었습니다.");
								
							}
							else if(s.equals("/userlist")) {
								userR(title, out);
								
							}
							
							else if(s.equals("/나가기")|| s.equals("/퇴장")) {
								roomN.get(title).remove(name);
								waitUser.put(name, out);
								if(roomN.get(title).isEmpty()) {
									roomN.remove(title);
								}
								th = 1;
								break;
							}else {
								out.println("올바르지 않은 명령어 입니다.");
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
				//예외가 발생할때 퇴장. 해쉬맵에서 해당 데이터 제거.
				//보통 종료하거나 나가면 java.net.SocketException: 예외발생
				if(th != 1) {
					roomN.get(title).remove(name);
				}
				if(!roomN.get(title).isEmpty())
					sendMsg(name + "님이 퇴장하셨습니다.", "", title);			
				try {
				}catch(Exception e) {
					
				}
			}
				
		}
	
	
	}
}
