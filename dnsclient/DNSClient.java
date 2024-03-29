import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * The Worlds Worst DNS Client 
 *
 * Compile: javac DNSClient.java
 * Run: java DNSClient 
 */
public class DNSClient {
	private static final String DNS_SERVER_ADDRESS = "8.8.8.8";
	private static final int DNS_SERVER_PORT = 53;
	
	
	
	public static void main(String[] args) throws IOException {
		String domain = "google.com";
		InetAddress ipAddress = InetAddress.getByName(DNS_SERVER_ADDRESS);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		
		 // *** Build a DNS Request Frame ****

        // Identifier: A 16-bit identification field generated by the device that creates the DNS query. 
        // It is copied by the server into the response, so it can be used by that device to match that 
        // query to the corresponding reply received from a DNS server. This is used in a manner similar 
        // to how the Identifier field is used in many of the ICMP message types.
        dos.writeShort(0x1234);
        
        
        // Write Query Flags
        dos.writeShort(0x0100);

		
        // Question Count: Specifies the number of questions in the Question section of the message.
        dos.writeShort(0x0001);
        
        // Answer Record Count: Specifies the number of resource records in the Answer section of the message.
        dos.writeShort(0x0000);
		
        // Authority Record Count: Specifies the number of resource records in the Authority section of 
        // the message. (“NS” stands for “name server”)
        dos.writeShort(0x0000);

        
        // Additional Record Count: Specifies the number of resource records in the Additional section of the message.
        dos.writeShort(0x0000);
        
        
        String[] domainParts = domain.split("\\.");
        System.out.println(domain + " has " + domainParts.length + " parts");
        
        for (int i = 0; i<domainParts.length; i++) {
            System.out.println("Writing: " + domainParts[i]);
            byte[] domainBytes = domainParts[i].getBytes("UTF-8");
            dos.writeByte(domainBytes.length);
            dos.write(domainBytes);
        }
        
        // No more parts
        dos.writeByte(0x00);
        
        // Type 0x01 = A (Host Request)
        dos.writeShort(0x0001);
        
        // Class 0x01 = IN
        dos.writeShort(0x0001);

        
        byte[] dnsFrame = baos.toByteArray(); 
        
        System.out.println("Sending: " + dnsFrame.length + " bytes");
        for (int i =0; i< dnsFrame.length; i++) {
            System.out.print("0x" + String.format("%x", dnsFrame[i]) + " " );
        }
        
        try (DatagramSocket socket = new DatagramSocket()) {
        	DatagramPacket dnsReqPacket = new DatagramPacket(dnsFrame, dnsFrame.length, ipAddress, DNS_SERVER_PORT);
			
        	// *** Send DNS Request Frame ***
        	socket.send(dnsReqPacket);
        	
        	
        	// Await response from DNS server
			byte[] buf = new byte[1024];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			socket.receive(packet);
			
			
			System.out.println("\n\nReceived: " + packet.getLength() + " bytes");

			for (int i = 0; i < packet.getLength(); i++) {
			    System.out.print(" 0x" + String.format("%x", buf[i]) + " " );
			}
			System.out.println("\n");

			DataInputStream din = new DataInputStream(new ByteArrayInputStream(buf));
			System.out.println("Transaction ID: 0x" + String.format("%x", din.readShort()));
			System.out.println("Flags: 0x" + String.format("%x", din.readShort()));
			System.out.println("Questions: 0x" + String.format("%x", din.readShort()));
			System.out.println("Answers RRs: 0x" + String.format("%x", din.readShort()));
			System.out.println("Authority RRs: 0x" + String.format("%x", din.readShort()));
			System.out.println("Additional RRs: 0x" + String.format("%x", din.readShort()));
			
			int recLen = 0;
			while ((recLen = din.readByte()) > 0) {
			    byte[] record = new byte[recLen];

			    for (int i = 0; i < recLen; i++) {
			        record[i] = din.readByte();
			    }

			    System.out.println("Record: " + new String(record, "UTF-8"));
			}
			
			System.out.println("Record Type: 0x" + String.format("%x", din.readShort()));
			System.out.println("Class: 0x" + String.format("%x", din.readShort()));
			System.out.println("Field: 0x" + String.format("%x", din.readShort()));
			System.out.println("Type: 0x" + String.format("%x", din.readShort()));
			System.out.println("Class: 0x" + String.format("%x", din.readShort()));
			System.out.println("TTL: 0x" + String.format("%x", din.readInt()));
			
			short addrLen = din.readShort();
			System.out.println("Len: 0x" + String.format("%x", addrLen));
			
			System.out.print("Address: ");
			for (int i = 0; i < addrLen; i++ ) {
			    System.out.print("" + String.format("%d", (din.readByte() & 0xFF)) + ".");
			}
			
        }
	}
}
