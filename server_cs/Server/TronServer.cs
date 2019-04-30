using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;

using Newtonsoft.Json;
using Server.Logic;
using Server.Logic.Event;
using Server.Protocol;

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
            Tron = new Tron(Convert.ToInt32(Properties.Resources.FieldSize));
            Tron.SnapshotCreated += Tron_SnapshotCreated;
            Tron.PlayerDied += Tron_PlayerDied;
            Tron.GameEnded += Tron_GameEnded;
            StartListening(ipAddress, port);
        }

        private static void Tron_GameEnded(GameEndedArguments g)
        {
            Protocol.Protocol protocol = new Protocol.Protocol();
            protocol.Type = Protocol.Type.TYPE_RESULT;
            _Clients.TryGetValue(g.Id, out var winner);
            protocol.Players.Add(winner.Player);

            Broadcast(protocol);
        }

        /// <summary>
        /// Broadcast game.
        /// </summary>
        /// <param name="s">SnapshotArguments</param>
        private static void Tron_SnapshotCreated(SnapshotArguments s)
        {
            // Broadcast game information.
            Broadcast(s.Protocol);
        }

        private static void Tron_PlayerDied(DeathArguments d)
        {
            Protocol.Protocol protocol = new Protocol.Protocol();
            protocol.Type = Protocol.Type.TYPE_DEAD;
            _Clients.TryGetValue(d.Id, out var loser);
            protocol.Players.Add(loser.Player);

            Broadcast(protocol);
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

            try
            {
                handler.BeginReceive(state.Buffer, 0, StateObject.BufferSize, 0, new AsyncCallback(ReadCallback), state);
            }
            catch
            {
                Console.Error.WriteLine("Lost connection to client.");
            }
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


            NetworkStream stream = new NetworkStream(handler);
            if (!stream.CanRead)
                return;

            using (StreamReader reader = new StreamReader(stream, Encoding.ASCII, false, Globals.BufferSize, true))
            {
                string line;

                while ((line = await reader.ReadLineAsync()) != null)
                {

                }
            }


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
                if (content.IndexOf(Globals.EofTag, StringComparison.InvariantCulture) > -1)
                {
                    content = content.Replace(Globals.EofTag, string.Empty);
                    Console.WriteLine(content);

                    // Get message from data.
                    Protocol.Protocol protocol = JsonConvert.DeserializeObject<Protocol.Protocol>(content.ToString());


                    if (protocol == null)
                    {
                        Console.Error.WriteLine("Can not read the protocol!");
                        return;
                    }

                    switch (protocol.Type)
                    {
                        case Protocol.Type.TYPE_CONNECT:
                            // Add client to list.
                            ClientInfo clientInfo = new ClientInfo(state, GetNextPlayerId(), protocol.Players.First(), protocol.LobbyId);
                            if (!_Clients.TryAdd(clientInfo.Player.Id, clientInfo))
                                return;

                            // Init player
                            Console.WriteLine($"Player: {protocol.Players.First().Name} joined.");
                            Tron.RegisterPlayer(clientInfo.Player);
                            protocol.Players = new List<Player>();
                            protocol.Players.Add(clientInfo.Player);
                            protocol.Type = Protocol.Type.TYPE_CONNECT;

                            // Send inital game information to client.
                            Send(handler, protocol);

                            // Lobby
                            SendLobbyMessage(handler, clientInfo);


                            break;

                        case Protocol.Type.TYPE_ACTION:
                            // Process action
                            Tron.ProcessInput(protocol);
                            break;

                        case Protocol.Type.TYPE_READY:
                            // When all rdy, then start the game
                            _Clients.First(p => p.Value.Player.Id == protocol.Players.First().Id).Value.Ready = true;

                            if (_Clients.Any(c => c.Value.Ready != true))
                                break;

                            Tron.StartGameLoop();
                            break;

                        case Protocol.Type.TYPE_DISCONNECT:
                            // Remove client from active client list.
                            KeyValuePair<int, ClientInfo> kvc = _Clients.FirstOrDefault(kv => kv.Value.StateObject.WorkSocket.Equals(handler));
                            if (kvc.Value != null)
                            {
                                _Clients.TryRemove(kvc.Key, out var info);
                            }

                            handler.Shutdown(SocketShutdown.Both);
                            handler.Close();
                            break;

                        case Protocol.Type.TYPE_ADD:
                        case Protocol.Type.TYPE_DEAD:
                        case Protocol.Type.TYPE_LOBBY:
                        case Protocol.Type.TYPE_RESULT:
                        case Protocol.Type.TYPE_START:
                        case Protocol.Type.TYPE_UPDATE:
                        default:
                            Console.Error.WriteLine("Invaild protocol message recieved!");
                            break;
                    }
                }
                else
                {
                    // Not all data received. Get more.
                    handler.BeginReceive(state.Buffer, 0, StateObject.BufferSize, 0, new AsyncCallback(ReadCallback), state);
                }

            }
        }

        private static void SendLobbyMessage(Socket handler, ClientInfo clientInfo)
        {
            var others = _Clients.Where(o => o.Key != clientInfo.Player.Id);

            // inform client
            Protocol.Protocol lobbyProtocol = new Protocol.Protocol();
            lobbyProtocol.Type = Protocol.Type.TYPE_LOBBY;
            lobbyProtocol.LobbyId = clientInfo.LobbyId;
            foreach(var other in others)
            {
                lobbyProtocol.Players.Add(other.Value.Player);
            }
            Send(handler, lobbyProtocol);

            // inform others
            Protocol.Protocol addProtocol = new Protocol.Protocol();
            addProtocol.Type = Protocol.Type.TYPE_ADD;
            addProtocol.Players.Add(clientInfo.Player);

            foreach(var other in others)
            {
                Send(other.Value.StateObject.WorkSocket, addProtocol);
            }
        }

        /// <summary>
        /// Broadcast protocol.
        /// </summary>
        /// <param name="protocol">protocol</param>
        private static void Broadcast(Protocol.Protocol protocol)
        {
            foreach (var client in _Clients)
            {
                Send(client.Value.StateObject.WorkSocket, protocol);
            }
        }

        private static void Send(Socket handler, Protocol.Protocol protocol)
        {
            SendDone.Reset();

            string data = JsonConvert.SerializeObject(protocol, typeof(Protocol.Protocol), Formatting.None, new JsonSerializerSettings());

            data = string.Concat(data, Properties.Resources.EndOfFileTag, Environment.NewLine);
            Console.WriteLine(data);

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
#if DEBUG
                Console.WriteLine($"Sent {bytesSent} bytes to client.");
#endif
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
