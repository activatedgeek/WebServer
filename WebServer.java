import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class WebServer {
	private static final int threads=100;
	private static final Executor executor = Executors.newFixedThreadPool(threads);
	private static final int port = 1893;
	static String urlPrefix =  "/home/activatedgeek/Downloads/";
	
	public static void main(String[] args) throws IOException{
		@SuppressWarnings("resource")
		ServerSocket socket = new ServerSocket(port);
		System.out.println("Server started at port: "+port);
		while(true){
			final Socket connection = socket.accept();
			Runnable task = new Runnable() {
				@Override
				public void run() {
					try {
						HandleRequest(connection);
					} catch (IOException e) {
						System.out.println("Error in handling request: "+e.getMessage());
					}
				}
			};
			executor.execute(task);
		}
	}
	
	private static void HandleRequest(Socket connection) throws IOException{
		try{
			String webServerAddress = connection.getInetAddress().toString();
			webServerAddress = webServerAddress.substring(1);
			int remotePort = connection.getPort();
			Calendar cal = Calendar.getInstance();
			Date currentTime = cal.getTime();
			System.out.println("New connection: "+webServerAddress+":"+remotePort+" @ "+currentTime);
			String filename = receiveFiles(connection);
		}
		catch(Exception e){
			System.out.println("Error: "+e.getMessage());
		}finally{
			if(connection!=null){
				connection.close();
				System.out.println("Connection closed");
			}
		}
	}
	
	private static String receiveFiles(Socket socket){
		BufferedInputStream bis = null;
		DataInputStream dis = null;
		String filename="Error";
		try{
			bis = new BufferedInputStream(socket.getInputStream());
			dis = new DataInputStream(bis);
			
			int filesCount = dis.readInt();
			System.out.println("Receiving "+filesCount+" file(s)..");
			File[] files = new File[filesCount];
			for(int i=0;i<filesCount;++i){
				long fileLength = dis.readLong();
				filename = dis.readUTF(); 
				files[i] = new File(urlPrefix+filename);
				FileOutputStream fos = new FileOutputStream(files[i]);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				
				for(int j=0;j<fileLength;++j){
					int n = bis.read();
					bos.write(n);
				}
				bos.flush();
				bos.close();
				System.out.println("Saved File.."+filename);
			}
		}catch(IOException e){
			System.out.println("Error in receiving files: "+e.getMessage());
			return "Error";
			//e.printStackTrace();
		}
		return filename;
	}
	
	protected static boolean sendImage(String path,Socket socket) throws IOException,UnknownHostException{
		FileInputStream fis = new FileInputStream(path);
		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
		int i;
		while((i=fis.read())>-1)
			dos.write(i);
		fis.close();
		socket.shutdownOutput();
		return true;
	}
}
