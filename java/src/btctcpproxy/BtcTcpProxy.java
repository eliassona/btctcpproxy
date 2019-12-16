package btctcpproxy;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import clojure.java.api.Clojure;
import clojure.lang.IFn;

public class BtcTcpProxy {
	private static final int PORT = 8579;
	private static final String CLOJURE_CORE = "clojure.core";
	private static final String BTCPROXY_CORE = "btctcpproxy.core";
	private static IFn require;
	private static IFn btcRpcFn;
	private final ServerSocket server;
	public BtcTcpProxy() throws IOException {
		server = new ServerSocket();
	}
	public static void main(final String[] args) throws IOException {
		System.out.println("starting btctcpproxy...");
		require = Clojure.var(CLOJURE_CORE, "require");
		require.invoke(Clojure.read(BTCPROXY_CORE));
		btcRpcFn= Clojure.var(BTCPROXY_CORE, "btc-rpc-fn");
		new BtcTcpProxy().start();
	}
	
	public void start() throws IOException {
		server.bind(new InetSocketAddress("localhost", PORT));
//		server.bind(new InetSocketAddress(InetAddress.getLocalHost(), PORT));
		new Thread(() -> {
			while (true) {
				try {
					System.out.println("waiting for incoming connections");
					startConnection(server.accept());
					System.out.println("accepted connection");
				} catch (final IOException e) {
					e.printStackTrace();
				}

			}
		}).start();
		System.out.println("started btctcpproxy at " + server);
	}

	private void startConnection(final Socket socket) {
		new Thread(() -> {
			System.out.println("connection started");
			try {
				final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				final DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				while (true) {
					try {
						System.out.println("read line...");
						final String line = in.readLine();
						System.out.println(String.format("Recived line: %s", line));
						final Object res = btcRpcFn.invoke(line);
						System.out.println(String.format("res: %s", res));
						if (res != null) {
							out.writeChars(res.toString());
							out.flush();
						}
					} catch (final Exception e) {
						e.printStackTrace();
					}
				}
			} catch (final IOException e1) {
				e1.printStackTrace();
			}
		}).start();
	}
}
