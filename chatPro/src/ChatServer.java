import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


//////
public class ChatServer extends Thread{
	//멤버에 접속자 목록을 보관할 컬렉션 생성
	List<ChatService> connList = new ArrayList<ChatService>();
	ServerSocket ss;
	public ChatServer() {
		this.start(); //this는 현재클래스인데 현재클래스에 start()가 없으면 상속받은 Thread의 start()를 실행함
	}
	
	//접속대기 스레드
	public void run() {
		try {
			ss = new ServerSocket(9999);
			
			System.out.println("Server Start");
			
		
			
			while(true) {
				System.out.println("접속대기중...");
				Socket s = ss.accept(); //대기..인데 여러번해야한다. 컴퓨터끌때까지 대기해야함 무한루프
				//누군가 접속 안하면 계속 이자리에있음
			
				//접속자를 관리할 수 있는 컬렉션을 만들어 거기에 담아야함.(현재접속한애들)
				ChatService cs = new ChatService(s);
				
				
				//중복아이피 검사하기
				setConnectionCheck(cs);
				
				connList.add(cs); //아래 클래스에 접속한 소캣을 담아서 정보를 저장하고. 그 정보를 객체로 만들어서 list에 넣는다.
				
				//현재 접속자들에게 접속 알림
				cs.setAllMessage("CONGU*"+cs.username+"님이 접속하였습니다."); //접속자알림. CONGU* -->접속자 알림 메세지
				
				//인원수.. 
				cs.setAllMessage("[CC$@]"+connList.size()); //size가 0이면 접속한사람이 하나도 없는것 .. 문장은 그냥 아무거나 5개?
				///////////////이 기호가 다른사람들과도 통일되어있지 않으면 제대로 표시되지 않는다.
				////////////// 내꺼 서버와 클라이언트의 구분 문자는 같지만 다른사람의 구분문자가 다르면 읽어올수가없는것
				//접속자 목록(10명이든 1명이든 접속자정보를 보내줘야한다.)
				cs.setAllMessage(getAllUsername());
				
				
				//스레드구현
				cs.start();//개인접속자.. 접속할때마다 스레드 하나씩 만들어짐
			}
		}catch(Exception e) {
			
		}
	}
	//모든 접속자 목록 얻어오기
	public String getAllUsername() { //이걸 호출하면 앞에 [User]를 붙여서 접속자 아이피문자열을 만든다음 / 를 붙여서 계속 해줌
		String usernameList = "[User]";
		for(int i=0; i<connList.size(); i++) {
			ChatService cs = connList.get(i); 
			usernameList += cs.username + "/";
		}
		return usernameList;
	}
	
	
	//같은 접속자 제거
	public void setConnectionCheck(ChatService cs) { //중복아이피.. 중복아이디 제거
		for(int i=0; i<connList.size(); i++) {
			ChatService cs2 = connList.get(i);
			if(cs.username.equals(cs2.username)) { //아이디를 비교해서 같으면 
				connList.remove(i); //i번째를 지운다.
				break;
			}
		}
			
	}
	//접속자 1명의 정보를 가진 클래스
	class ChatService extends Thread{ //채팅 보내는걸 위해서 이걸 스레드로 변경
		Socket s; //여기에 저장하겠다. 클래스객체만들때 소켓을 매게변수로 .. 위에 무한루프에서 생성되는 소켓을 받아서 저장.
				  //즉 접속자 한명이 생기면 그 정보를 받아서 저장한다는것.
		PrintWriter pw;
		BufferedReader br;
		String username; //접속자이름은 아이피로 대체할거
		InetAddress ia;
		ChatService(){}
		ChatService(Socket s){
			this.s = s;
			try {
			br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			pw = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
			
			ia = s.getInetAddress(); //소켓에서 inetAddress구한다. 
			username = ia.getHostAddress();// 아이피를 사용자명으로 쓸거다.. getGostName()은 컴퓨터이름 구하는것. 
			
			}catch(Exception e) {}
		}
		public void run() { //위에서 스타트 해줘야 실행됨
			try {
				while(true) {
					String clientMsg = br.readLine();
					if(clientMsg != null) { //통신을 통해 넘어오는 데이터는 null도 있기때문에
						setAllMessage("[CMsg]["+username+"님]"+clientMsg);
					}
				}
			}catch(Exception e) {
				
			}
		}
		
		//현재접속한 모든 접속자에게 정보보내기
		public void setAllMessage(String msg) {
			for(int i=0; i<connList.size(); i++) {//지금 현재 접속한 인원만큼 .. 컬렉션이니까 size.. 배열은 length
				ChatService cs = connList.get(i); //지금 ChatService가 컬렉션에 담겨있으니까.. 변수는 ChatService로
				try { //문자 보내려고 했더니 없다.. 나갔다는것. 에러발생. 이걸 예외처리
				cs.pw.println(msg);
				cs.pw.flush();
				}catch(Exception e) {
					connList.remove(i); //i번재에 보내다가 에러났다는건 i번째 사람이 나갔다는것. i번째를 리스트에서 지워준다.
					//문제는 1,2,3,4 사람중 3번이 나가면 4번이 3번이된다. 그래서 3번을 지우면 3번을 다시 보내도록 해줘야한다.
					i--;
				}
			}
		}
	}
	

	
	public static void main(String[] args) {
		new ChatServer();
	}

}
