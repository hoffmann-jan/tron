using System;
using System.Collections.Generic;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;

namespace Server
{
    /// <summary>
    /// The server socket.
    /// </summary>
    public class AsynchronousSocketListener
    {
        #region Fields
        /// <summary>
        /// Connected Clients.
        /// </summary>
        private static List<Socket> _ActiveClients = new List<Socket>();
        #endregion

        #region Properties
        /// <summary>
        /// Thread signal.
        /// </summary>
        public static ManualResetEvent AllDone = new ManualResetEvent(false);
        #endregion

        /// <summary>
        /// TCP/IP socket listener (server).
        /// </summary>
        /// <param name="ipAddress">IPAdress.</param>
        /// <param name="port">Port.</param>
        public static void StartListening(IPAddress ipAddress, int port)
        {
            // Establish the local endpoint for the socket.
            IPEndPoint localEndPoint = new IPEndPoint(ipAddress, port);

            // Create a TCP/IP socket.
            Socket listener = new Socket(ipAddress.AddressFamily, SocketType.Stream, ProtocolType.Tcp);

            // Bind the socket to the local endpoint and listen for incoming connections.
            try
            {
                listener.Bind(localEndPoint);
                listener.Listen(100);

                Console.WriteLine($"Server listening on {ipAddress}:{port}.");

                while (true)
                {
                    // Set the event to nonsignaled state.
                    AllDone.Reset();

                    // Start an asynchronous socket to listen for connections.
                    Console.WriteLine("Waiting for a connection...");
                    listener.BeginAccept(new AsyncCallback(AcceptCallback), listener);

                    // Wait until a connection is made before continuing.
                    AllDone.WaitOne();
                }

            }
            catch (Exception exception)
            {
                Console.WriteLine(exception.Message);
            }

            Console.WriteLine($"{Environment.NewLine}Press ENTER to continue...");
            Console.Read();
        }

        /// <summary>
        /// Accept the callback.
        /// </summary>
        /// <param name="asyncResult">Async result.</param>
        public static void AcceptCallback(IAsyncResult asyncResult)
        {
            // Signal the main thread to continue.
            AllDone.Set();

            // Get the socket that handles the client request.
            Socket listener = (Socket)asyncResult.AsyncState;
            Socket handler = listener.EndAccept(asyncResult);

            // Print connection information.
            Console.WriteLine($"Client connected. {handler.LocalEndPoint}.");

            // Create the state object.
            StateObject state = new StateObject();
            state.WorkSocket = handler;

            // Add client to list.
            _ActiveClients.Add(handler);

            handler.BeginReceive(state.Buffer, 0, StateObject.BufferSize, 0, new AsyncCallback(ReadCallback), state);
        }

        /// <summary>
        /// Callback reader.
        /// </summary>
        /// <param name="asyncResult">Async result.</param>
        public static void ReadCallback(IAsyncResult asyncResult)
        {
            string content = string.Empty;

            // Retrieve the state object and the handler socket from the asynchronous state object.
            StateObject state = (StateObject)asyncResult.AsyncState;
            Socket handler = state.WorkSocket;

            // Read data from the client socket.
            int bytesRead = handler.EndReceive(asyncResult);

            if (bytesRead > 0)
            {
                // There  might be more data, so store the data received so far.
                state.StringBuilder.Append(Encoding.ASCII.GetString(state.Buffer, 0, bytesRead));

                // Check for end-of-file tag. If it is not there, read more data.
                content = state.StringBuilder.ToString();
                if (content.IndexOf("<EOF>", StringComparison.InvariantCulture) > -1)
                {
                    // All the data has been read from the client. Display it on the console.
                    Console.WriteLine($"Read {content.Length} bytes from socket.{Environment.NewLine}Data : {content}");
                    // Echo the data back to the client.
                    Send(handler, content);
                }
                else
                {
                    // Not all data received. Get more.
                    handler.BeginReceive(state.Buffer, 0, StateObject.BufferSize, 0, new AsyncCallback(ReadCallback), state);
                }
            }
        }

        private static void Send(Socket handler, string data)
        {
            // Convert the string data to byte data using ASCII encoding.
            byte[] byteData = Encoding.ASCII.GetBytes(data);

            // Begin sending the data to the remote device.
            handler.BeginSend(byteData, 0, byteData.Length, 0, new AsyncCallback(SendCallback), handler);
        }

        private static void SendCallback(IAsyncResult asyncResult)
        {
            try
            {
                // Retrieve the socket from the state object.
                Socket handler = (Socket)asyncResult.AsyncState;

                // Complete sending the data to the remote device.
                int bytesSent = handler.EndSend(asyncResult);
                Console.WriteLine($"Sent {bytesSent} bytes to client.");

                // Remove client from active client list.
                _ActiveClients.Remove(handler);

                handler.Shutdown(SocketShutdown.Both);
                handler.Close();
            }
            catch (Exception exception)
            {
                Console.WriteLine(exception.Message);
            }
        }

    }
}
