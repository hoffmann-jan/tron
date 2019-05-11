using System;
using System.Net.NetworkInformation;
using System.Collections.Generic;
using System.Security.Cryptography;
using System.Net.Sockets;
using Newtonsoft.Json;
using Server.Logic.Event;
using System.Text;
using System.Numerics;

namespace Server.Tcp
{
    public class ConnectionThread
    {
        public static Dictionary<TcpClient, RSACryptoServiceProvider> clientKeys 
            = new Dictionary<TcpClient, RSACryptoServiceProvider>();

        public TcpListener threadListener;
        private static int connections = 0;

        public void HandleConnection()
        {
            byte[] data = new byte[1024];

            TcpClient client = threadListener.AcceptTcpClient();
            client.NoDelay = true;
            NetworkStream ns = client.GetStream();
            connections++;
            Console.WriteLine($"New client accepted: {connections} active connections");

            {
                string json = JsonConvert.SerializeObject(
                    TronServer.rsaParameters 
                );
                json = string.Concat(json, "\n");
                byte[] jsonBytes = Encoding.UTF8.GetBytes(json);

                var stream = client.GetStream();
                stream.Write(jsonBytes, 0, jsonBytes.Length);
                stream.Flush();
            }

            int bytesToRead = 0, nextReadCount = 0, rc = 0;
            byte[] byteCount = BitConverter.GetBytes(1024);
            byte[] receiveBuffer = new byte[4096];

            try
            {
                while (true)
                {
                    bytesToRead = BitConverter.ToInt32(byteCount, 0);

                    // Receive the data
                    //Console.WriteLine("TCP Listener: Receiving, reading & displaying the data...");
                    while (bytesToRead > 0)
                    {
                        if (!ns.CanRead)
                            break;

                        // Make sure we don't read beyond what the first message indicates
                        //    This is important if the client is sending multiple "messages" --
                        //    but in this sample it sends only one
                        if (bytesToRead < receiveBuffer.Length)
                            nextReadCount = bytesToRead;
                        else
                            nextReadCount = receiveBuffer.Length;

                        // Read some data
                        rc = ns.Read(receiveBuffer, 0, nextReadCount);

                        // Detect if client disconnected
                        if (client.Client.Poll(0, SelectMode.SelectRead))
                        {
                            byte[] buff = new byte[1];
                            if (client.Client.Receive(buff, SocketFlags.Peek) == 0)
                            {
                                // Client disconnected
                                Console.Error.WriteLineAsync($"Host {client.Client.RemoteEndPoint.ToString()} disconected!");
                                ConnectionLost?.Invoke(new ConnectionLostArguments(client.Client.RemoteEndPoint.ToString(), this));
                                break;
                            }
                        }

                        // Display what we read
                        string readText = Encoding.UTF8.GetString(receiveBuffer, 0, rc);                        

                        if (clientKeys.ContainsKey(client))
                        {
                            readText = readText.Replace(Environment.NewLine, "");
                            byte[] readTextBytes = Convert.FromBase64String(readText);
                            byte[] plainTextBytes = TronServer.serverCsp.Decrypt(readTextBytes, false);

                            readText = Encoding.UTF8.GetString(plainTextBytes);
#if DEBUG
                            Console.WriteLine($"Received from {client.Client.RemoteEndPoint}: {readText}");
#endif
                            Protocol.Protocol protocol = null;
                            try
                            {
                                protocol = JsonConvert.DeserializeObject<Protocol.Protocol>(readText);
                            }
                            catch (Exception ex)
                            {
                                protocol = null;
                                Console.Error.WriteLineAsync($"JSON deserialize ERROR: '{ex.Message}'.");
                            }

                            if (protocol != null)
                            {
                                ProtocolRecievedArguments protocolRecievedArguments = new ProtocolRecievedArguments(protocol, client);
                                ProtocolRecieved?.Invoke(protocolRecievedArguments);
                            }
                        }
                        else  // First message always contains the key
                        {
                            var parameters = JsonConvert.DeserializeObject<RsaParameters>(
                                readText
                            );

                            RSAParameters p = new RSAParameters();

                            p.Modulus = BigInteger.Parse(parameters.modulus).ToByteArray();
                            p.Exponent = parameters.exponent.ToByteArray();

                            var csp = new RSACryptoServiceProvider();
                            csp.ImportParameters(p);

                            clientKeys[client] = csp;
                        }
                        bytesToRead -= rc;
                    }

                    if (rc == 0)
                    {
                        break;
                    }
                }
            }
            catch (Exception ex)
            {
                Console.Error.WriteLineAsync(ex.ToString());
            }
            finally
            {
                ns.Close();
                client.Close();
                connections--;
                Console.WriteLine($"Client disconnected: {connections} active connections");
            }
        }

        public bool PingHost(string nameOrAddress)
        {
            bool pingable = false;
            Ping pinger = null;

            try
            {
                pinger = new Ping();
                PingReply reply = pinger.Send(nameOrAddress);
                pingable = reply.Status == IPStatus.Success;
            }
            catch (PingException)
            {
                return false;
            }
            finally
            {
                if (pinger != null)
                {
                    pinger.Dispose();
                }
            }

            return pingable;
        }


        public delegate void ProtocolRecievedHandler(ProtocolRecievedArguments protocolRecievedArguments);
        public event ProtocolRecievedHandler ProtocolRecieved;

        public delegate void ConnectionLostHandler(ConnectionLostArguments connectionLostArguments);
        public event ConnectionLostHandler ConnectionLost;

    }
}
