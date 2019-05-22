using System;
using System.Linq;
using System.IO;
using System.Net.Sockets;
using System.Text;

using Newtonsoft.Json;
using Server.Logic.Event;
using Server.Encryption;

namespace Server.Tcp
{
    public class ConnectionThread
    {
        public TcpListener threadListener;
        private bool _Terminate = false;
        private static int connections = 0;

        public void HandleConnection()
        {
            if (_Terminate)
                return;

            TcpClient client = threadListener.AcceptTcpClient();
            client.NoDelay = true;
            NetworkStream ns = client.GetStream();
            connections++;
            Console.WriteLine($"New client accepted: {connections} active connections");

            int bytesToRead = 0, nextReadCount = 0, rc = 0;
            byte[] byteCount = BitConverter.GetBytes(1024);
            byte[] receiveBuffer = new byte[4096];

            try
            {
                string data = string.Empty;

                while (true)
                {
                    bytesToRead = BitConverter.ToInt32(byteCount, 0);


                    // Receive the data
                    while (bytesToRead > 0)
                    {
                        if (!ns.CanRead)
                            break;

                        // Make sure we don't read beyond what the first message indicates
                        // This is important if the client is sending multiple "messages" --
                        // but in this sample it sends only one
                        if (bytesToRead < receiveBuffer.Length)
                            nextReadCount = bytesToRead;
                        else
                            nextReadCount = receiveBuffer.Length;

                        // Read some data
                        try
                        {
                            rc = ns.Read(receiveBuffer, 0, nextReadCount);
                        }
                        catch (ObjectDisposedException)
                        {
                            ns.Close();
                            ConnectionLost?.Invoke(new ConnectionLostArguments(client.Client.RemoteEndPoint.ToString(), this));
                            _Terminate = true;
                            return;
                        }
                        catch (IOException)
                        {
                            ns.Close();
                            ConnectionLost?.Invoke(new ConnectionLostArguments(client.Client.RemoteEndPoint.ToString(), this));
                            _Terminate = true;
                            return;
                        }

                        // Detect if client disconnected
                        if (client.Client.Poll(0, SelectMode.SelectRead))
                        {
                            try
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
                            catch (SocketException s)
                            {
                                // Client disconnected
                                Console.Error.WriteLineAsync($"Host {client.Client.RemoteEndPoint.ToString()} disconected!");
#if DEBUG
                                Console.Error.WriteLineAsync($"Message: {s.ToString()}");
#endif
                                ConnectionLost?.Invoke(new ConnectionLostArguments(client.Client.RemoteEndPoint.ToString(), this));
                                _Terminate = true;
                                return;
                            }
                        }

                        data += Encoding.UTF8.GetString(receiveBuffer, 0, rc);                        

                        if (data.Contains(Environment.NewLine))
                        {
                            string[] parts = data.Split(Environment.NewLine);

                            data = parts[0];

                            if (TronServer.Security.AES.HasClient(client))
                            {
                                data = data.Replace(Environment.NewLine, "");
                                byte[] dataCipher = Convert.FromBase64String(data);
                                data = TronServer.Security.AES.Decrypt(dataCipher, client);
#if DEBUG
                                Console.WriteLine($"Received from {client.Client.RemoteEndPoint}: {data}");
#endif
                                Protocol.Protocol protocol = null;
                                try
                                {
                                    protocol = JsonConvert.DeserializeObject<Protocol.Protocol>(data);
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
                            else
                            {
                                HandshakeClient(client, data);
                            }
                            data = parts[1];
                        }

                        bytesToRead -= rc;
                    }

                    if (rc == 0)
                    {
                        break;
                    }
                }
                // Client disconnected
                Console.Error.WriteLineAsync($"Host {client.Client.RemoteEndPoint.ToString()} disconected!");
                ConnectionLost?.Invoke(new ConnectionLostArguments(client.Client.RemoteEndPoint.ToString(), this));
            }
            catch (ObjectDisposedException)
            {
                Console.WriteLine("Connection canceled!");
                ConnectionLost?.Invoke(new ConnectionLostArguments(client.Client.RemoteEndPoint.ToString(), this));
            }
            catch (Exception ex)
            {
                Console.Error.WriteLineAsync(ex.ToString());
                ConnectionLost?.Invoke(new ConnectionLostArguments(client.Client.RemoteEndPoint.ToString(), this));
            }
            finally
            {
                if (ns != null)
                    ns.Close();
                if (client != null)
                    client.Close();

                _Terminate = true;
                connections--;
                Console.WriteLine($"Client disconnected: {connections} active connections");
            }
        }

        private void HandshakeClient(TcpClient client, string rsaData)
        {
            TronServer.Security.RSA.AddClient(client, RSAPublicParamters.FromJson(rsaData));
            TronServer.Security.AES.AddClient(client);

            AESPublicParameters parameters = TronServer.Security.AES.PublicParameters(client);
            string json = parameters.ToJson();

            byte[] cipher = TronServer.Security.RSA.Encrypt(Encoding.UTF8.GetBytes(json), client);
            byte[] newline = Encoding.UTF8.GetBytes(Environment.NewLine);

            NetworkStream stream = client.GetStream();
            stream.Write(cipher, 0, cipher.Length);
            stream.Write(newline, 0, newline.Length);
            stream.Flush();
        }

        public delegate void ProtocolRecievedHandler(ProtocolRecievedArguments protocolRecievedArguments);
        public event ProtocolRecievedHandler ProtocolRecieved;

        public delegate void ConnectionLostHandler(ConnectionLostArguments connectionLostArguments);
        public event ConnectionLostHandler ConnectionLost;

    }
}
