

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.StringTokenizer;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


public class ChatClient extends JFrame implements ActionListener, Runnable{
	//프레임의 Center 
	JPanel centerPane = new JPanel(new BorderLayout());
		JPanel northPane = new JPanel(new BorderLayout());
			JTextField connTf = new JTextField("192.168.0.214");
			JButton connBtn = new JButton("Connection");
		JTextArea msgView = new JTextArea();
		JScrollPane sp = new JScrollPane(msgView);
		JPanel southPane = new JPanel(new BorderLayout());
			JTextField msgTf = new JTextField();
			JButton sendBtn = new JButton("Send");
	//프레임의 EAST
	JPanel eastPane = new JPanel(new BorderLayout());
		JLabel connListLbl = new JLabel("      접속자 리스트      ");
		DefaultListModel<String> model = new DefaultListModel<String>(); //JList에 넣을 모델
		JList<String> connList = new JList<String>(model);
		JScrollPane sp2 = new JScrollPane(connList);
		JLabel connCount = new JLabel("현재접속자 : 0명");
		
	//////////////	
	Socket socket;	
	//메세지 보내는 객체.. 메세지 보내기
	PrintWriter pw;
	
	
	//메세지 받는 객체.. 메세지 받기
	BufferedReader br;
	
	
	
	public ChatClient() {
		super("채팅프로그램");
		connTf.setBackground(new Color(200,200,230));
		msgTf.setBackground(new Color(200,200,230));
		msgView.setBackground(Color.BLUE);
		msgView.setForeground(Color.WHITE);
		
		//Frame의 center에 넣기
		northPane.add(connTf);
		northPane.add(connBtn, BorderLayout.EAST); //이부분 다시 확인하기. 북쪽 패널에 다시 보더레이아웃으로 오른쪽에 배치
		
		southPane.add(msgTf);
		southPane.add(sendBtn, BorderLayout.EAST);
		
		centerPane.add(northPane,BorderLayout.NORTH);
		centerPane.add(sp);
		centerPane.add(southPane, BorderLayout.SOUTH);
		
		add(centerPane);
		
		//Frame의 east에 넣기
		eastPane.add(connListLbl, BorderLayout.NORTH);
		eastPane.add(sp2);
		eastPane.add(connCount,BorderLayout.SOUTH);
		
		model.addElement("         "); //모델에 데이터를 넣어둬서 프레임에 기본적인 사이즈를 대충 지정해줌
		add(eastPane, BorderLayout.EAST);

		
		setSize(600,400);
		setVisible(true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		connTf.addActionListener(this);
		connBtn.addActionListener(this);
		msgTf.addActionListener(this);
		sendBtn.addActionListener(this);
		
	}
	public void actionPerformed(ActionEvent ae) {
		Object event = ae.getSource();
		if(event == connTf || event == connBtn) {//서버와 연결
			setServerConnection();
		}else if(event == msgTf || event == sendBtn){//서버로 문자 보내기
			msgSend();
			msgTf.setText("");
		}
	}
	//서버로 문자보내기
	public void msgSend() {
		pw.println(msgTf.getText());
		pw.flush();
	}
	
	public void setServerConnection() {
		try {
			if(!connTf.getText().equals("")) {//conntf를 읽었더니 ""이랑 같지 않으면.. 앞에 부정
		//서버에 접속
				InetAddress ia = InetAddress.getByName(connTf.getText());// 이 아이피를 가지고 서버 접속할것이다.
				socket = new Socket(ia, 9999); //chatserver클래스에서 열어놓은 포트번호
		
		//inputStream
				br = new BufferedReader(new InputStreamReader(socket.getInputStream()));// 변수에 따로따로 안넣고 한줄로 끝내는 방법
				
				
		//outputStream
				pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
				
		//연결 버튼 비활성화
				connTf.setEditable(false);
				connBtn.setEnabled(false);
				
		//받는 스레드 구현 //이걸 안해주면 아래 run이 안돌아가서 기다리고 있지 않는다.
		Thread t = new Thread(this);
		t.start();
				
;			}
		}catch(Exception e) {
			
		}
	}
	
	//서버에서 보낸 문자 받을 스레드
	public synchronized void run() { //같은 데이터가 반복되지 않도록 동기화.. 원인모를 충돌로 접속자 목록이 사라졌다 보이는 현상때문에 싱크로나이즈드 설정
		while(true) {
			try {
				String receiveMsg = br.readLine();
				if(receiveMsg != null) {
					if(receiveMsg.substring(0, 6).equals("CONGU*")) { //받은 문자의 문자열중 0에서 6까지가 CONGU*과 같으냐
						msgView.append(receiveMsg.substring(6)+"\n"); //6번 이후로 출력 다음줄로
					}else if(receiveMsg.substring(0,6).equals("[CC$@]")) { //현재 접속자수
						connCount.setText("현재접속자 : "+ receiveMsg.substring(6)+"명"); //6번글자이후
					}else if(receiveMsg.substring(0,6).equals("[User]")) {
						//문자열에서 특정 문자나 기호를 기준으로 잘라서 구분하는것.
						//split을(java2 프로젝트 StringTest클래스에 정리되어있음) 이용하거나.
						//StringTokenizer로(java2 StringTokenizer에 정리) 하거나
						
						String username = receiveMsg.substring(6); //6번째부터 끝까지
						StringTokenizer token = new StringTokenizer(username, "/"); //token에는 잘려서 들어가있다. next하면 다음거가 나오는식으로 이용된다.구분자chatserver에서 쓴/
						model.removeAllElements(); //이전리스트 제거
						while(token.hasMoreTokens()) {//토근이 남아있으면.
							model.addElement(token.nextToken()); //순서대로 하나씩 끄집어내서 모델에 넣는다.
						}
					}else if(receiveMsg.substring(0,6).equals("[CMsg]")) {
						msgView.append(receiveMsg.substring(6)+"\n"); 
					}
				}
			}catch(Exception e) {
				
			}
		}
	}
	public static void main(String[] args) {
		new ChatClient();
	}

}
