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
using Server.Encryption;
using Server.Logic;
using Server.Logic.Event;
using Server.Protocol;
using Server.Tcp;

namespace Server
{

    /// <summary>
    /// Game server for Tron.
    /// </summary>
    public static class TronServer
    {
        #region Fields
        /// <summary>
        /// Use GetNextPlayerId().
        /// </summary>
        private static int _NextPlayerId = 0;
        private static bool _FirstStart = true;

        private static TcpListener _Server;
        private static Tron _Tron;
        private static List<Tuple<int, CancellationTokenSource>> _TemporaryConnectionInfos = new List<Tuple<int, CancellationTokenSource>>();

        private static Enum.State _State;
        private static Enum.State State
        {
            get => _State;
            set
            {
#if DEBUG
                Console.WriteLine($"Change server state to {value.ToString()}");
#endif
                _State = value;
            } 
        }
        #endregion

        #region Properties
        /// <summary>
        /// Connected Clients.
        /// </summary>
        public static ConcurrentDictionary<int, ClientInfo> Clients { get; set; }
        public static RSAServerClientEncryption Encryption { get; set; } = new RSAServerClientEncryption();
        #endregion

        public static void StartTronServer(IPAddress ipAddress, int port)
        {
            Clients = new ConcurrentDictionary<int, ClientInfo>();
            _Tron = new Tron(Convert.ToInt32(Properties.Resources.FieldSize));
            _Tron.SnapshotCreated += Tron_SnapshotCreated;
            _Tron.PlayerDied += Tron_PlayerDied;
            _Tron.GameEnded += Tron_GameEnded;
            StartListening(ipAddress, port);
        }

        private static void Tron_GameEnded(GameEndedArguments g)
        {
            Protocol.Protocol protocol = new Protocol.Protocol();
            protocol.Type = Protocol.Type.TYPE_RESULT;
            Clients.TryGetValue(g.Id, out var winner);
            protocol.Players.Add(winner.Player);

            Broadcast(protocol);
            _Tron.StopGameLoop();
            State = Enum.State.Winner;

            Thread.Sleep(3000);
            State = Enum.State.Lobby;
            SendLobbyMessage(null, true);
            foreach (var client in Clients)
            {
                client.Value.Ready = false;
            }
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
            Clients.TryGetValue(d.Id, out var loser);
            protocol.Players.Add(loser.Player);

            Broadcast(protocol);
        }

        private async static void ConnectionThread_ConnectionLost(ConnectionLostArguments connectionLostArguments)
        {
            await Task.Delay(500);
            var client = Clients.FirstOrDefault(kv => kv.Value.TcpClient.Client.RemoteEndPoint.ToString().Equals(connectionLostArguments.Address));
            Clients.TryRemove(client.Key, out ClientInfo nfo);
        }

        private static void ConnectionThread_ProtocolRecieved(ProtocolRecievedArguments protocolRecievedArguments)
        {
            Protocol.Protocol protocol = protocolRecievedArguments.Protocol;
            TcpClient client = protocolRecievedArguments.TcpClient;

            if (protocol == null)
            {
                Console.Error.WriteLine("Can not read the protocol!");
                return;
            }

            switch (protocol.Type)
            {
                case Protocol.Type.TYPE_CONNECT:
                    // Add client to list.
                    var connectionInfo = _TemporaryConnectionInfos.First();
                    _TemporaryConnectionInfos.Remove(connectionInfo);
                    ClientInfo clientInfo = new ClientInfo(client, connectionInfo.Item1, protocol.Players.First(), protocol.LobbyId, connectionInfo.Item2);
                    if (!Clients.TryAdd(clientInfo.Player.Id, clientInfo))
                        return;

                    // Init player
                    Console.WriteLine($"Player: {protocol.Players.First().Name} joined.");
                    _Tron.RegisterPlayer(clientInfo.Player);
                    protocol.Players = new List<Player>();
                    protocol.Players.Add(clientInfo.Player);
                    protocol.Type = Protocol.Type.TYPE_CONNECT;

                    // Send inital game information to client.
                    Send(client, protocol);

                    // Lobby
                    SendLobbyMessage(clientInfo);
                    break;

                case Protocol.Type.TYPE_ACTION:
                    // Process action
                    var clients = Clients.ToList();
                    int id = clients.First(kv => kv.Value.TcpClient.Equals(client)).Key;
                    _Tron.ProcessInput(protocol, id);
                    break;

                case Protocol.Type.TYPE_READY:
                    // When all rdy, then start the game
                    Clients.First(p => p.Value.Player.Id == protocol.Players.First().Id).Value.Ready = true;

                    if (Clients.Any(c => c.Value.Ready != true))
                        break;
                    if (_FirstStart)
                    {
                        _Tron.StartGameLoop();
                        _FirstStart = false;
                    }
                    else
                    {
                        _Tron.RestartGameLoop(Clients);
                    }
                    State = Enum.State.InGame;
                    break;

                case Protocol.Type.TYPE_DISCONNECT:

                    KeyValuePair<int, ClientInfo> kvc = Clients.FirstOrDefault(kv => kv.Value.TcpClient.Equals(client));
                    if (kvc.Value != null)
                    {
                        // send disconnect
                        var player = kvc.Value;
                        player.Player.Name = "Ignore";
                        player.LobbyId = protocol.LobbyId;
                        Position pos = new Position
                        {
                            X = 0,
                            Y = 0,
                            Jumping = false
                        };
                        player.Player.Position = pos;
                        protocol.Players.Clear();
                        protocol.Players.Add(player.Player);
                        Broadcast(protocol);

                        try
                        {
                            kvc.Value.CancellationTokenSource.Cancel();
                        }
                        catch (ObjectDisposedException) { }

                        // Remove client from active client list.
                        Clients.TryRemove(kvc.Key, out var info);
                    }

                    // send new lobby message
                    if (State == Enum.State.Lobby)
                    {
                        SendLobbyMessage(null, true);
                    }

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


        /// <summary>
        /// TCP/IP socket listener (server).
        /// </summary>
        /// <param name="ipAddress">IPAdress.</param>
        /// <param name="port">Port.</param>
        public static void StartListening(IPAddress ipAddress, int port)
        {
            try
            {
                // Initialise the server.
                _Server = new TcpListener(ipAddress, port);
                _Server.Server.NoDelay = true;
                // Run the Server.
                _Server.Start();
                Console.Clear();
                Console.WriteLine($"Tron server listening on {ipAddress}:{port}.");
                State = Enum.State.Lobby;

                while (true)
                {
                    // Save resources.
                    while (!_Server.Pending())
                    {
                        Thread.Sleep(500);
                    }

                    ConnectionThread connectionThread = new ConnectionThread();
                    connectionThread.threadListener = _Server;
                    connectionThread.ProtocolRecieved += ConnectionThread_ProtocolRecieved;
                    connectionThread.ConnectionLost += ConnectionThread_ConnectionLost;

                    CancellationTokenSource tokenSource = new CancellationTokenSource();

                    Task.Factory.StartNew(
                        connectionThread.HandleConnection,
                        tokenSource.Token,
                        TaskCreationOptions.LongRunning,
                        TaskScheduler.Default);

                    _TemporaryConnectionInfos.Add(new Tuple<int, CancellationTokenSource>(GetNextPlayerId(), tokenSource));
                }

            }
            catch (Exception exception)
            {
                Console.WriteLine(exception.Message);
            }

            Console.WriteLine($"{Environment.NewLine}Press ENTER to continue...");
            Console.ReadLine();
        }

        private static void SendLobbyMessage(ClientInfo clientInfo, bool all = false)
        {
            IEnumerable<KeyValuePair<int, ClientInfo>> others = null;
            if (!all)
                others = Clients.Where(o => o.Key != clientInfo.Player.Id);

            // inform client
            Protocol.Protocol lobbyProtocol = new Protocol.Protocol();
            lobbyProtocol.Type = Protocol.Type.TYPE_LOBBY;
            if (all)
                lobbyProtocol.LobbyId = 1;
            else
                lobbyProtocol.LobbyId = clientInfo.LobbyId;

            if (all)
            {
                foreach (var client in Clients)
                {
                    lobbyProtocol.Players.Add(client.Value.Player);
                }

                foreach (var client in Clients)
                {
                    Send(client.Value.TcpClient, lobbyProtocol); 
                }
            }
            else
            {
                if (others.Any())
                {
                    foreach (var other in others)
                    {
                        lobbyProtocol.Players.Add(other.Value.Player);
                    }
                }

                Send(clientInfo.TcpClient, lobbyProtocol);

                // inform others
                Protocol.Protocol addProtocol = new Protocol.Protocol();
                addProtocol.Type = Protocol.Type.TYPE_ADD;
                addProtocol.Players.Add(clientInfo.Player);
                if (others.Any())
                {
                    foreach (var other in others)
                    {
                        if (other.Value.TcpClient.Connected)
                            Send(other.Value.TcpClient, addProtocol);
                    }
                }
            }
        }

        /// <summary>
        /// Broadcast protocol.
        /// </summary>
        /// <param name="protocol">protocol</param>
        private static void Broadcast(Protocol.Protocol protocol)
        {
            foreach (var client in Clients)
            {
                Send(client.Value.TcpClient, protocol);
            }
        }

        private static void Send(TcpClient client, Protocol.Protocol protocol)
        {
            string plain = JsonConvert.SerializeObject(protocol);
#if DEBUG
            Console.WriteLine($"{Environment.NewLine}SENDING MESSAGE TYPE:{protocol.Type}.{Environment.NewLine}");
            Console.WriteLine($"Sending: {plain}");
#endif
            byte[] plainBytes = Encoding.UTF8.GetBytes(plain);
            byte[] cipherBytes = Encryption.Encrypt(plainBytes, client);
            string cipherBase = Convert.ToBase64String(cipherBytes);
            cipherBase = string.Concat(cipherBase, Environment.NewLine);
            byte[] cipherBaseBytes = Encoding.UTF8.GetBytes(cipherBase);

            try
            {
                // Begin sending the data to the remote device.
                var stream = client.GetStream();
                stream.Write(cipherBaseBytes, 0, cipherBaseBytes.Length);
                stream.Flush();
            }
            catch (Exception)
            {
                Console.Error.WriteLineAsync($"{Environment.NewLine}Exception: send failed, {client.Client.RemoteEndPoint}.{Environment.NewLine}");
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
