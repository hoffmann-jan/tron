using System;
using System.Net;
using System.Net.Sockets;
using System.Text;
using Newtonsoft.Json;

namespace Server.Udp
{
    public class UpdListener
    {
        UdpClient _Listener;

        public UpdListener(UdpClient server)
        {
            _Listener = server;
        }

        public void RecieveProtocol()
        {
            IPEndPoint remoteIpEndPoint = new IPEndPoint(IPAddress.Any, 0);

            try
            {
                while (true)
                {
#if DEBUG
                    Console.WriteLine("Waiting for protocol");
#endif
                    byte[] bytes = _Listener.Receive(ref remoteIpEndPoint);
                    string data = Encoding.ASCII.GetString(bytes, 0, bytes.Length);
#if DEBUG
                    Console.WriteLine($"Data received: {data}");
#endif

                    Protocol.Protocol protocol = JsonConvert.DeserializeObject<Protocol.Protocol>(data);

                    ProtocolReceived?.Invoke(new ProtocolReceivedArguments(protocol, remoteIpEndPoint.Address.ToString(), remoteIpEndPoint.Port));
                }
            }
            catch (SocketException e)
            {
                Console.WriteLine(e);
            }
        }


        public delegate void ProtocolReceivedHandler(ProtocolReceivedArguments protocolReceivedArguments);
        public event ProtocolReceivedHandler ProtocolReceived;
    }
}
