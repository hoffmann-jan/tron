﻿using System;
using System.Net.Sockets;
using Newtonsoft.Json;
using Server.Logic.Event;

namespace Server.Tcp
{
    class ConnectionThread
    {
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


            int bytesToRead = 0, nextReadCount = 0, rc = 0;
            byte[] byteCount = BitConverter.GetBytes(1024);
            byte[] receiveBuffer = new byte[1024];

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

                    // If connection is lost
                    if (rc == 0)
                    {
                        break;
                    }

                    // Display what we read
                    string readText = System.Text.Encoding.UTF8.GetString(receiveBuffer, 0, rc);
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

                    bytesToRead -= rc;

                }

                if (rc == 0)
                {
                    break;
                }
            }

            ns.Close();
            client.Close();
            connections--;
            Console.WriteLine($"Client disconnected: {connections} active connections");
        }

        public delegate void ProtocolRecievedHandler(ProtocolRecievedArguments protocolRecievedArguments);
        public event ProtocolRecievedHandler ProtocolRecieved;
    }
}