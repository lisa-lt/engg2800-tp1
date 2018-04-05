package midiblocks;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.fazecast.jSerialComm.SerialPort;

/**
 * Instances of Serial are a higher-level wrapper of a SerialPort. They abstract
 * connection logic, as well as being able to consume/send messages rather
 * than bytes. 
 * @author Lisa Liu-Thorrold
 */
public class Serial {
	// The character set used to interpret inbound stream data.
	private static final String CHARSET = "US-ASCII";

	// Connection status of the Serial
	private boolean isConnected = false;

	// The underlying SerialPort
	private SerialPort serialPort;

	// The Serial's outbound stream.
	private BufferedWriter out;

	// THe serial's inbound stream
	private BufferedReader in;

	/**
	 * Connect to the the given port using the specified communication speed.
	 * @param portName The name of the port to connect to
	 * @param baudRate Communicate rate in symbols per second (ie. BAUD rate)
	 */
	public void connect(String portName, int baudRate) {
		try {

			SerialPort[] ports = SerialPort.getCommPorts();

			int index = -5;

			for (int i=0; i < ports.length; i++) {
				if (ports[i].getSystemPortName().equals(portName)) {
					index = i;
					break;
				}	
			}

			if (index >= 0) {
				serialPort = SerialPort.getCommPorts()[index];

				serialPort.openPort();

				serialPort.setComPortParameters(baudRate, 8, 
						SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Setup the communication streams
		try {
			out = new BufferedWriter(
					new OutputStreamWriter(serialPort.getOutputStream(), CHARSET));

			in = new BufferedReader(
					new InputStreamReader(serialPort.getInputStream(), CHARSET));

		} catch (IOException e) {
			e.printStackTrace();
		}

		isConnected = true;
	}

	/**
	 * Closes the serial port connection.
	 */
	public void close() {
		serialPort.closePort();
	}


	/**
	 * Sends a message over the serial
	 * @param message The message to send.
	 * @throws IOException 
	 */
	public void sendMessage(int message) throws IOException {
		out.write(message);
		//		if (message == '[' || message == ']' || message == '^') {
		//			out.write(message);
		//		} else {
		//			out.write(Integer.toString(message));
		//		}
		//		//out.write(Integer.toString(message));
		//		out.newLine();
		out.flush();
	}

	/**
	 * Receive a message over the serial. FOR DEBUGGING.
	 * @throws IOException
	 */
	public void getMessage() throws IOException {

		// StringBuilder message = new StringBuilder();

		int i = in.read();

		//        if (message.length() == 0) return null;
		System.out.println("Received by avr: " + Integer.toString(i));

		//System.out.println(line.toString());
	}

	/**
	 * Check if the Serial is connected.
	 * @return True of the Serial is connected, false otherwise.
	 */
	public boolean isConnected() {
		return isConnected;
	}
}
