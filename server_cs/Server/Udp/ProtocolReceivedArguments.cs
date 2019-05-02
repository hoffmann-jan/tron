namespace Server.Udp
{
    public class ProtocolReceivedArguments
    {
        public int Port { get; set; }
        public string Ip { get; set; }
        public Protocol.Protocol Protocol { get; set; }

        public ProtocolReceivedArguments(Protocol.Protocol protocol, string ip, int port)
        {
            Protocol = protocol;
            Ip = ip;
            Port = port;
        }
    }
}
