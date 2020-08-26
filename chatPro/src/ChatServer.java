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
	//����� ������ ����� ������ �÷��� ����
	List<ChatService> connList = new ArrayList<ChatService>();
	ServerSocket ss;
	public ChatServer() {
		this.start(); //this�� ����Ŭ�����ε� ����Ŭ������ start()�� ������ ��ӹ��� Thread�� start()�� ������
	}
	
	//���Ӵ�� ������
	public void run() {
		try {
			ss = new ServerSocket(9999);
			
			System.out.println("Server Start");
			
		
			
			while(true) {
				System.out.println("���Ӵ����...");
				Socket s = ss.accept(); //���..�ε� �������ؾ��Ѵ�. ��ǻ�Ͳ������� ����ؾ��� ���ѷ���
				//������ ���� ���ϸ� ��� ���ڸ�������
			
				//�����ڸ� ������ �� �ִ� �÷����� ����� �ű⿡ ��ƾ���.(���������Ѿֵ�)
				ChatService cs = new ChatService(s);
				
				
				//�ߺ������� �˻��ϱ�
				setConnectionCheck(cs);
				
				connList.add(cs); //�Ʒ� Ŭ������ ������ ��Ĺ�� ��Ƽ� ������ �����ϰ�. �� ������ ��ü�� ���� list�� �ִ´�.
				
				//���� �����ڵ鿡�� ���� �˸�
				cs.setAllMessage("CONGU*"+cs.username+"���� �����Ͽ����ϴ�."); //�����ھ˸�. CONGU* -->������ �˸� �޼���
				
				//�ο���.. 
				cs.setAllMessage("[CC$@]"+connList.size()); //size�� 0�̸� �����ѻ���� �ϳ��� ���°� .. ������ �׳� �ƹ��ų� 5��?
				///////////////�� ��ȣ�� �ٸ��������� ���ϵǾ����� ������ ����� ǥ�õ��� �ʴ´�.
				////////////// ���� ������ Ŭ���̾�Ʈ�� ���� ���ڴ� ������ �ٸ������ ���й��ڰ� �ٸ��� �о�ü������°�
				//������ ���(10���̵� 1���̵� ������������ ��������Ѵ�.)
				cs.setAllMessage(getAllUsername());
				
				
				//�����屸��
				cs.start();//����������.. �����Ҷ����� ������ �ϳ��� �������
			}
		}catch(Exception e) {
			
		}
	}
	//��� ������ ��� ������
	public String getAllUsername() { //�̰� ȣ���ϸ� �տ� [User]�� �ٿ��� ������ �����ǹ��ڿ��� ������� / �� �ٿ��� ��� ����
		String usernameList = "[User]";
		for(int i=0; i<connList.size(); i++) {
			ChatService cs = connList.get(i); 
			usernameList += cs.username + "/";
		}
		return usernameList;
	}
	
	
	//���� ������ ����
	public void setConnectionCheck(ChatService cs) { //�ߺ�������.. �ߺ����̵� ����
		for(int i=0; i<connList.size(); i++) {
			ChatService cs2 = connList.get(i);
			if(cs.username.equals(cs2.username)) { //���̵� ���ؼ� ������ 
				connList.remove(i); //i��°�� �����.
				break;
			}
		}
			
	}
	//������ 1���� ������ ���� Ŭ����
	class ChatService extends Thread{ //ä�� �����°� ���ؼ� �̰� ������� ����
		Socket s; //���⿡ �����ϰڴ�. Ŭ������ü���鶧 ������ �ŰԺ����� .. ���� ���ѷ������� �����Ǵ� ������ �޾Ƽ� ����.
				  //�� ������ �Ѹ��� ����� �� ������ �޾Ƽ� �����Ѵٴ°�.
		PrintWriter pw;
		BufferedReader br;
		String username; //�������̸��� �����Ƿ� ��ü�Ұ�
		InetAddress ia;
		ChatService(){}
		ChatService(Socket s){
			this.s = s;
			try {
			br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			pw = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
			
			ia = s.getInetAddress(); //���Ͽ��� inetAddress���Ѵ�. 
			username = ia.getHostAddress();// �����Ǹ� ����ڸ����� ���Ŵ�.. getGostName()�� ��ǻ���̸� ���ϴ°�. 
			
			}catch(Exception e) {}
		}
		public void run() { //������ ��ŸƮ ����� �����
			try {
				while(true) {
					String clientMsg = br.readLine();
					if(clientMsg != null) { //����� ���� �Ѿ���� �����ʹ� null�� �ֱ⶧����
						setAllMessage("[CMsg]["+username+"��]"+clientMsg);
					}
				}
			}catch(Exception e) {
				
			}
		}
		
		//���������� ��� �����ڿ��� ����������
		public void setAllMessage(String msg) {
			for(int i=0; i<connList.size(); i++) {//���� ���� ������ �ο���ŭ .. �÷����̴ϱ� size.. �迭�� length
				ChatService cs = connList.get(i); //���� ChatService�� �÷��ǿ� ��������ϱ�.. ������ ChatService��
				try { //���� �������� �ߴ��� ����.. �����ٴ°�. �����߻�. �̰� ����ó��
				cs.pw.println(msg);
				cs.pw.flush();
				}catch(Exception e) {
					connList.remove(i); //i���翡 �����ٰ� �������ٴ°� i��° ����� �����ٴ°�. i��°�� ����Ʈ���� �����ش�.
					//������ 1,2,3,4 ����� 3���� ������ 4���� 3���̵ȴ�. �׷��� 3���� ����� 3���� �ٽ� �������� ������Ѵ�.
					i--;
				}
			}
		}
	}
	

	
	public static void main(String[] args) {
		new ChatServer();
	}

}
