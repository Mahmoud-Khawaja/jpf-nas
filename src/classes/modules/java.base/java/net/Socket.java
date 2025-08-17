package java.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Model class for java.net.Socket
 *
 * @author Nastaran Shafiei
 */
public class Socket implements java.io.Closeable {

  static {
    System.out.println("DEBUG: JPF Socket MODEL CLASS loaded successfully!");
    try {
      System.out.println("DEBUG: Socket model class location: " +
              Socket.class.getProtectionDomain().getCodeSource().getLocation());
    } catch (UnsupportedOperationException e) {
      System.out.println("DEBUG: Socket model class location: [JPF environment - getProtectionDomain not supported]");
    }
  }

  private SocketInputStream input = null;
  private SocketOutputStream output = null;

  /**
   * This is used to make JPF account for connection objects when state matching.
   * Keep the hash value of this socket connection object, and it is updated upon any
   * internal change to the connection
   */
  private int hash;

  /**
   * The implementation of this Socket.
   */
  SocketImpl impl;

  /**
   * Various states of this socket.
   */
  private boolean bound = false;
  private boolean connected = false;
  private Object closeLock = new Object();
  private boolean closed = false;

  private Object lock = new Object();
  private Thread waitingThread;

  public Socket() {
    System.out.println("DEBUG: Socket MODEL CLASS default constructor called");
    impl = new SocketImpl();
    impl.setSocket(this);
    impl.bind(-1);
    this.setIOStream();
    System.out.println("DEBUG: Socket() constructor completed, hash=" + hash);
  }

  public Socket(String host, int port) throws UnknownHostException, IOException {
    System.out.println("DEBUG: Socket MODEL CLASS constructor called with host=" + host + ", port=" + port);
    SocketImpl.checkPort(port);
    impl = new SocketImpl();
    impl.setSocket(this);
    impl.bind(-1);
    impl.remoteHost = host;
    impl.port = port;
    this.setIOStream();
    connect(host, port);
    System.out.println("DEBUG: Socket(host, port) constructor completed, connected=" + connected);
  }

  private void setIOStream() {
    System.out.println("DEBUG: Socket MODEL CLASS setIOStream() called");
    this.input = new SocketInputStream(this);
    this.output = new SocketOutputStream(this);
  }

  private native void connect(String host, int port) throws IOException;

  public int getHash() {
    System.out.println("DEBUG: Socket MODEL CLASS getHash() called, returning hash=" + hash);
    return this.hash;
  }

  public void connect(SocketAddress endpoint) throws IOException {
    System.out.println("DEBUG: Socket MODEL CLASS connect(SocketAddress) called");
    connect(endpoint, 0);
  }

  public void connect(SocketAddress endpoint, int timeout) throws IOException {
    System.out.println("DEBUG: Socket MODEL CLASS connect(SocketAddress, timeout=" + timeout + ") called");
    String host = ((InetSocketAddress)endpoint).getHostName();
    int port = ((InetSocketAddress)endpoint).getPort();
    connect(host, port);
  }

  void setBound() {
    System.out.println("DEBUG: Socket MODEL CLASS setBound() called");
    bound = true;
  }

  public InputStream getInputStream() throws IOException {
    System.out.println("DEBUG: Socket MODEL CLASS getInputStream() called, closed=" + closed + ", connected=" + connected);
    if (isClosed()) {
      throw new SocketException("Socket is closed");
    } else if (!isConnected()) {
      throw new SocketException("Socket is not connected");
    }

    assert(!isClosed());
    assert(isConnected());

    return input;
  }

  public OutputStream getOutputStream() throws IOException {
    System.out.println("DEBUG: Socket MODEL CLASS getOutputStream() called, closed=" + closed + ", connected=" + connected);
    if (isClosed()) {
      throw new SocketException("Socket is closed");
    } else if (!isConnected()) {
      throw new SocketException("Socket is not connected");
    }

    assert(!isClosed());
    assert(isConnected());

    return output;
  }

  /**
   * Returns the closed state of the socket.
   */
  public boolean isClosed() {
    return this.closed;
  }

  /**
   * Returns the connection state of the socket.
   */
  public native boolean isConnected();

  @Override
  public native void close() throws IOException;

  private int timeout;
  public void setSoTimeout(int timeout) {
    System.out.println("DEBUG: Socket MODEL CLASS setSoTimeout(" + timeout + ") called");
    this.timeout = timeout;
  }

  @Override
  protected void finalize() throws Throwable{
    System.out.println("DEBUG: Socket MODEL CLASS finalize() called");
    close();
  }

  public Socket(InetAddress address, int port) throws UnknownHostException, IOException {
    this(address.getHostName(), port);
  }
}
