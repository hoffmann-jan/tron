using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Newtonsoft.Json;

using Server.Logic;
using Server.Logic.Event;

namespace Server
{
    /// <summary>
    /// Game server for Tron.
    /// </summary>
    public class TronServer
    {
        #region Fields
        /// <summary>
        /// Connected Clients.
        /// </summary>
        private static ConcurrentDictionary<int, ClientInfo> _Clients = new ConcurrentDictionary<int, ClientInfo>();
        /// <summary>
        /// Use GetNextPlayerId().
        /// </summary>
        private static int _NextPlayerId = 0;

        private static Tron Tron;
        #endregion

        #region Properties
        /// <summary>
        /// Thread signal.
        /// </summary>
        public static ManualResetEvent ConnectDone = new ManualResetEvent(false);
        public static ManualResetEvent SendDone = new ManualResetEvent(false);
        #endregion

        public static void StartTronServer(IPAddress ipAddress, int port)
        {
            StartListening(ipAddress, port);
        }

        private static void Game()
        {
            while (_Clients.Count == 0)
            {
                Thread.Sleep(1000);
            }

            Console.WriteLine("Starting new game.");
            Tron = new Tron(Convert.ToInt32(Properties.Resources.FieldSize));
            Tron.SnapshotCreated += Tron_SnapshotCreated;
            Tron.StartGameLoop(_Clients.Keys.ToArray());
        }

        /// <summary>
        /// Broadcast game.
        /// </summary>
        /// <param name="s">SnapshotArguments</param>
        private static void Tron_SnapshotCreated(SnapshotArguments s)
        {
            // Broadcast game information.
            //Parallel.ForEach(_Clients, (keyValuePair) =>
            //{
            //    int playerId = keyValuePair.Key;
            //    ClientInfo client = keyValuePair.Value;

            //    Send(client.StateObject.WorkSocket, data);
            //});
            foreach (KeyValuePair<int, ClientInfo> keyValuePair in _Clients)
            {
                int playerId = keyValuePair.Key;
                ClientInfo client = keyValuePair.Value;

                string data = JsonConvert.SerializeObject(s.Messages.Where(m => m.id == playerId), typeof(Message), Formatting.None, new JsonSerializerSettings());

                Send(client.StateObject.WorkSocket, string.Concat(data, Properties.Resources.EndOfFileTag));
            }
        }

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

                Task.Run(() => Game());

                while (true)
                {
                    // Set the event to nonsignaled state.
                    ConnectDone.Reset();

                    // Start an asynchronous socket to listen for connections.
                    Console.WriteLine("Waiting for a connection...");
                    listener.BeginAccept(new AsyncCallback(AcceptCallback), listener);

                    // Wait until a connection is made before continuing.
                    ConnectDone.WaitOne();
                }

            }
            catch (Exception exception)
            {
                Console.WriteLine(exception.Message);
            }

            Console.WriteLine($"{Environment.NewLine}Press ENTER to continue...");
            Console.ReadLine();
        }

        /// <summary>
        /// Accept the callback.
        /// </summary>
        /// <param name="asyncResult">Async result.</param>
        public static void AcceptCallback(IAsyncResult asyncResult)
        {
            // Signal the main thread to continue.
            ConnectDone.Set();

            // Get the socket that handles the client request.
            Socket listener = (Socket)asyncResult.AsyncState;
            Socket handler = listener.EndAccept(asyncResult);

            // Print connection information.
            Console.WriteLine($"Client connected. {handler.LocalEndPoint}.");

            // Create the state object.
            StateObject state = new StateObject();
            state.WorkSocket = handler;

            // Add client to list.
            ClientInfo clientInfo = new ClientInfo(state, GetNextPlayerId());
            _Clients.TryAdd(clientInfo.PlayerId, clientInfo);

            //handler.BeginReceive(state.Buffer, 0, StateObject.BufferSize, 0, new AsyncCallback(ReadCallback), state);
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

            if (!handler.Connected)
            {
                return;
            }

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
                    // Get message from data.
                    Message message = JsonConvert.DeserializeObject<Message>(content.ToString());

                    // All the data has been read from the client. Display it on the console.
                    Console.WriteLine($"Read {content.Length} bytes from socket.{Environment.NewLine}Data : {message}");

                    // Process message
                    Tron.ProcessInput(message);

                    //// Echo the data back to the client.
                    //Send(handler, content);
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
            SendDone.Reset();
            // Convert the string data to byte data using ASCII encoding.
            byte[] byteData = Encoding.UTF8.GetBytes(data);

            if (!handler.Connected)
                return;

            // Begin sending the data to the remote device.
            handler.BeginSend(byteData, 0, byteData.Length, 0, new AsyncCallback(SendCallback), handler);
            SendDone.WaitOne();
        }

        private static void SendCallback(IAsyncResult asyncResult)
        {
            try
            {
                SendDone.Set();
                // Retrieve the socket from the state object.
                Socket handler = (Socket)asyncResult.AsyncState;

                // Complete sending the data to the remote device.
                int bytesSent = handler.EndSend(asyncResult);
                Console.WriteLine($"Sent {bytesSent} bytes to client.");

                //// Remove client from active client list.
                //KeyValuePair<int, ClientInfo> kvc =_Clients.FirstOrDefault(kv => kv.Value.StateObject.WorkSocket.Equals(handler));
                //if (kvc.Value != null)
                //{
                //    _Clients.TryRemove(kvc.Key, out var info);
                //}

                //handler.Shutdown(SocketShutdown.Both);
                //handler.Close();
            }
            catch (Exception exception)
            {
                Console.WriteLine(exception.Message);
            }
        }

        /// <summary>
        /// Get the next player id.
        /// </summary>
        /// <returns></returns>
        private static int GetNextPlayerId()
        {
            return ++_NextPlayerId;
        }

    }
}
