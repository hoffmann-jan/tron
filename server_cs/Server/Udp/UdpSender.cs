using System;
using System.Net.Sockets;

namespace Server.Udp
{
    public class UdpSender
    {
        UdpClient _Server;

        public UdpSender(UdpClient server)
        {
            _Server = server;
        }

        public void Send()
        {
            /*
            IPEndPoint groupEndPoint = new IPEndPoint(IPAddress.Any, 0);

            try
            {
                while (true)
                {
                    Console.WriteLine("Waiting for broadcast");
                    byte[] bytes = listener.Receive(ref groupEndPoint);

                    Console.WriteLine($"Received broadcast from {groupEndPoint} :");
                    Console.WriteLine($" {Encoding.ASCII.GetString(bytes, 0, bytes.Length)}");
                }
            }
            catch (SocketException e)
            {
                Console.WriteLine(e);
            }
            finally
            {
                listener.Close();
            }
            */
        }
    }
}
