import java.sql.*;
/*
 * JDBC 프로그래밍 절차
 * 1. 오라클 용 JDBC드라이버 를 메모리에 로드.
 * forName으로 호출
 * 
 * 2. 오라클연결을 위해 계정준지. 
 * url jdbc:oracle:thin:@localhost:1521:xe
 * 3. 드라이버 매니저클래스의 정적메소드인 getconnection으로 연결
 * 
 */
public class Main
{
	static {
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			}catch(ClassNotFoundException cnfe) {
				cnfe.printStackTrace();
			}
	}
	
	public static void main(String[] args)
	{
		try
		{
			
			Connection con = DriverManager.getConnection(
					"jdbc:oracle:thin:@localhost:1521:xe",
					"scott",	//아이디	
					"TIGER");	//패스워드
			/*
			 * 쿼리문 전송을 위해 스테이트먼트 인터페이스로 객체생성
			 * */
			Statement stmt = con.createStatement();
			StringBuffer sb = new StringBuffer();
			/*
			 * 쿼리문 작성.
			 * : 작성시 줄바꿈하면서 앞뒤로 스페이스 삽입하면 개꿀띠
			 */
			  
			sb.append("select * from emp");
			/*
			 * 쿼리문을 오라클 데이터베이스로 전송. 실행한 결과는
			 * ResultSet객체를 통해 반환받는다.
			 */
			ResultSet rs = stmt.executeQuery(sb.toString());
			/*
			 * 반환된 결과를 갯수만큼 반복해서 출력.
			 * next()로 남은게 있나확인
			 */
			while(rs.next()) {
				System.out.print("eno: "+rs.getInt(1)+", ");
				System.out.println("ename : "+rs.getString("ename"));
			}
			/*
			 * 자원반납(해제) : 메모리절약을 위해 모두 반납한다.
			 */
			rs.close();
			stmt.close();
			con.close();
		} catch (SQLException sqle)
		{
			System.out.println("Connection Error");
			sqle.printStackTrace();
		}
	}

}
