package btctcpproxy;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import clojure.java.api.Clojure;
import clojure.lang.IFn;

public class BtcTcpProxy {
	private static final String CLOJURE_CORE = "clojure.core";
	private static final String BTCPROXY_CORE = "btctcpproxy.core";
	private static IFn require;
	private static IFn btcRpcFn;
	private final ServerSocket server;
	public BtcTcpProxy() throws IOException {
		server = new ServerSocket(8579);
	}
	public static void main(final String[] args) throws IOException {
		require = Clojure.var(CLOJURE_CORE, "require");
		require.invoke(Clojure.read(BTCPROXY_CORE));
		btcRpcFn= Clojure.var(BTCPROXY_CORE, "btc-rpc-fn-block");
		new BtcTcpProxy().start();
	}
	
	public void start() {
		new Thread(() -> {
			while (true) {
				try {
					startConnection(server.accept());
				} catch (final IOException e) {
					e.printStackTrace();
				}

			}
		}).start();
	}

	private void startConnection(final Socket socket) {
		new Thread(() -> {
			try {
				final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				final DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				while (true) {
					try {
						final String line = in.readLine();
						final Object res = btcRpcFn.invoke(line);
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
