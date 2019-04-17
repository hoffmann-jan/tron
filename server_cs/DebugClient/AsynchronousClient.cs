using System;
using System.Net;
using System.Net.Sockets;
using System.Threading;
using System.Text;

using DebugProperties;

// State object for receiving data from remote device.  
public class StateObject
{
    // Client socket.  
    public Socket WorkSocket = null;
    // Size of receive buffer.  
    public const int BufferSize = Globals.BufferSize;
    // Receive buffer.  
    public byte[] Buffer = new byte[BufferSize];
    // Received data string.  
    public StringBuilder StringBuilder = new StringBuilder();
}

public class AsynchronousClient
{
    // ManualResetEvent instances signal completion.  
    private static ManualResetEvent _ConnectDone = new ManualResetEvent(false);
    private static ManualResetEvent _SendDone = new ManualResetEvent(false);
    private static ManualResetEvent _ReceiveDone = new ManualResetEvent(false);

    // The response from the remote device.  
    private static string response = string.Empty;

    public static Socket Connect(IPAddress iPAddress, int port)
    {
        // Establish the remote endpoint for the socket.
        IPEndPoint remoteEndPoint = new IPEndPoint(iPAddress, port);

        // Create a TCP/IP socket.  
        Socket client = new Socket(iPAddress.AddressFamily, SocketType.Stream, ProtocolType.Tcp);

        // Connect to the remote endpoint.  
        client.BeginConnect(remoteEndPoint, new AsyncCallback(ConnectCallback), client);

        return client;
    }

    public static void StartClient()
    {
        // Connect to a remote device.  
        try
        {
            // Establish the remote endpoint for the socket.
            IPAddress ipAddress = IPAddress.Loopback;
            IPEndPoint remoteEP = new IPEndPoint(ipAddress, Globals.Port);

            // Create a TCP/IP socket.  
            Socket client = new Socket(ipAddress.AddressFamily,
                SocketType.Stream, ProtocolType.Tcp);

            // Connect to the remote endpoint.  
            client.BeginConnect(remoteEP,
                new AsyncCallback(ConnectCallback), client);
            _ConnectDone.WaitOne();

            // Send test data to the remote device.  
            Send(client, "This is a test<EOF>");
            _SendDone.WaitOne();

            // Receive the response from the remote device.  
            Receive(client);
            _ReceiveDone.WaitOne();

            // Write the response to the console.  
            Console.WriteLine($"Response received : {response}");

            // Release the socket.  
            client.Shutdown(SocketShutdown.Both);
            client.Close();

        }
        catch (Exception e)
        {
            Console.Error.WriteLine(e);
        }
    }

    private static void ConnectCallback(IAsyncResult ar)
    {
        try
        {
            // Retrieve the socket from the state object.  
            Socket client = (Socket)ar.AsyncState;

            // Complete the connection.  
            client.EndConnect(ar);

            Console.WriteLine($"Socket connected to {client.RemoteEndPoint}");

            // Signal that the connection has been made.  
            _ConnectDone.Set();
        }
        catch (Exception e)
        {
            Console.Error.WriteLine(e);
        }
    }

    public static void Receive(Socket client)
    {
        try
        {
            // Create the state object.  
            StateObject state = new StateObject();
            state.WorkSocket = client;

            // Begin receiving the data from the remote device.  
            client.BeginReceive(state.Buffer, 0, StateObject.BufferSize, 0,
                new AsyncCallback(ReceiveCallback), state);
        }
        catch (Exception e)
        {
            Console.Error.WriteLine(e);
        }
    }

    private static void ReceiveCallback(IAsyncResult ar)
    {
        try
        {
            // Retrieve the state object and the client socket   
            // from the asynchronous state object.  
            StateObject state = (StateObject)ar.AsyncState;
            Socket client = state.WorkSocket;

            // Read data from the remote device.  
            int bytesRead = client.EndReceive(ar);

            if (bytesRead > 0)
            {
                // There might be more data, so store the data received so far.  
                state.StringBuilder.Append(Encoding.ASCII.GetString(state.Buffer, 0, bytesRead));

                // Get the rest of the data.  
                client.BeginReceive(state.Buffer, 0, StateObject.BufferSize, 0,
                    new AsyncCallback(ReceiveCallback), state);
            }
            else
            {
                // All the data has arrived; put it in response.  
                if (state.StringBuilder.Length > 1)
                {
                    response = state.StringBuilder.ToString();
                }
                // Signal that all bytes have been received.  
                _ReceiveDone.Set();
            }
        }
        catch (Exception e)
        {
            Console.WriteLine(e.ToString());
        }
    }

    public static void Send(Socket client, String data)
    {
        // Convert the string data to byte data using ASCII encoding.  
        byte[] byteData = Encoding.ASCII.GetBytes(data);

        // Begin sending the data to the remote device.  
        client.BeginSend(byteData, 0, byteData.Length, 0, new AsyncCallback(SendCallback), client);
    }

    private static void SendCallback(IAsyncResult ar)
    {
        try
        {
            // Retrieve the socket from the state object.  
            Socket client = (Socket)ar.AsyncState;

            // Complete sending the data to the remote device.  
            int bytesSent = client.EndSend(ar);
            Console.WriteLine($"Sent {bytesSent} bytes to server.");

            // Signal that all bytes have been sent.  
            _SendDone.Set();
        }
        catch (Exception e)
        {
            Console.Error.WriteLine(e);
        }
    }

}