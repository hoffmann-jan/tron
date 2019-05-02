using System;
using System.Net.Sockets;

namespace Server.Logic.Event
{
    public class ProtocolRecievedArguments
    {
        private Protocol.Protocol _Protocol;
        private TcpClient _TcpClient;

        public Protocol.Protocol Protocol { get => _Protocol; }
        public TcpClient TcpClient { get => _TcpClient; }

        public ProtocolRecievedArguments(Protocol.Protocol protocol, TcpClient tcpClient)
        {
            _Protocol = protocol;
            _TcpClient = tcpClient;
        }
    }
}
