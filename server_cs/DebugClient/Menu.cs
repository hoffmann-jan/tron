using DebugProperties;
using System;
using System.Collections.Generic;
using System.Net;
using System.Net.Sockets;
using System.Text;

namespace DebugClient
{
    public static class Menu
    {
        #region fields
        private static bool _TcpIpConnected = false;
        private static Socket _TcpClient;
        #endregion
        #region public functions
        public static int Enter()
        {
            try
            {
                NextMenu next = ConnectionMainMenu();

                while (next != NextMenu.Quit)
                {
                    switch (next)
                    {
                        case NextMenu.Main:
                            next = ConnectionMainMenu();
                            break;
                        case NextMenu.DisConnectTcp:
                            next = DisConnectTcp();
                            break;
                        case NextMenu.SendMessage:
                            next = SendMessage();
                            break;
                        case NextMenu.ConnectTo:
                            next = ConnectTo();
                            break;
                    }
                }
            }
            catch (Exception exception)
            {
                Console.Error.Write(exception.Message);
                return -1;
            }
            return 0;
        }
        #endregion

        #region private functions
        private static NextMenu ConnectionMainMenu()
        {
            Console.Clear();
            Console.WriteLine("Connection Main Menu");
            if (_TcpIpConnected)
                Console.WriteLine($"Connected to {_TcpClient.RemoteEndPoint}");
            Console.WriteLine("####################");
            Console.WriteLine();
            Console.WriteLine("TCP/IP");
            Console.WriteLine("======");
            if (_TcpIpConnected)
                Console.WriteLine("1 - Disconnect");
            else
                Console.WriteLine("1 - Connect");
            if (_TcpIpConnected)
                Console.WriteLine("2 - Send Message");
            Console.WriteLine();
            Console.WriteLine("q - Quit");

            while (true)
            {
                ConsoleKeyInfo keyInfo = Console.ReadKey();
                if (keyInfo.Key == ConsoleKey.Q)
                    return NextMenu.Quit;
                else if (keyInfo.Key == ConsoleKey.D1)
                    return NextMenu.DisConnectTcp;
                else if (keyInfo.Key == ConsoleKey.D2)
                    return NextMenu.SendMessage;
            }
        }

        private static NextMenu DisConnectTcp()
        {
            if (_TcpIpConnected)
            {
                // Release the socket.  
                _TcpClient.Shutdown(SocketShutdown.Both);
                _TcpClient.Close();
                _TcpIpConnected = false;
                return NextMenu.Main;
            }
            else
            {
                // Connect
                return NextMenu.ConnectTo;
            }
        }

        private static NextMenu SendMessage()
        {
            Console.Clear();
            Console.WriteLine("Enter a message and hit enter to send:");
            string message = Console.ReadLine();
            string.Concat(message, "<EOF>");
            Console.WriteLine("Sending message..");
            AsynchronousClient.Send(_TcpClient, message);
            Console.WriteLine("Sended.");
            //Console.WriteLine("Wait for resopnse.");
            //AsynchronousClient.Receive(_TcpClient);
            Console.WriteLine("Press any key to continue..");
            Console.ReadKey();

            return NextMenu.Main;
        }

        private static NextMenu ConnectTo()
        {
            Console.Clear();
            Console.WriteLine("Connect to");
            Console.WriteLine("##########");
            Console.WriteLine();
            Console.WriteLine("TCP/IP");
            Console.WriteLine("======");
            Console.WriteLine("1 - Connect to default");
            Console.WriteLine($"     {Globals.IP}:{Globals.Port}");
            Console.WriteLine("2 - Enter other IPAddress");
            Console.WriteLine();
            Console.WriteLine("q - Main Menu");

            NextMenu result = NextMenu.None;
            while (result == NextMenu.None)
            {
                ConsoleKeyInfo keyInfo = Console.ReadKey();
                if (keyInfo.Key == ConsoleKey.Q)
                    result = NextMenu.Main;
                else if (keyInfo.Key == ConsoleKey.D1)
                {
                    Connect(IPAddress.Parse(Globals.IP), Globals.Port);
                    result = NextMenu.Main;
                }
                else if (keyInfo.Key == ConsoleKey.D2)
                {
                    Connect();
                    result = NextMenu.Main;
                }
            }

            Console.WriteLine("Wait for resopnse.");
            while (true)
            {
                AsynchronousClient.Receive(_TcpClient);
                AsynchronousClient.ReceiveDone.WaitOne();
            }
            return result;
        }

        private static void Connect()
        {
            string ipString = string.Empty;
            IPAddress ip = null;
            bool fail = false;
            while (true)
            {
                Console.Clear();
                if (fail)
                    Console.WriteLine($"{ipString} is not a valid ip! Please try again.");
                Console.WriteLine("Enter IP and hit enter. 'Example 123.123.123.123'");
                ipString = Console.ReadLine();
                if (IPAddress.TryParse(ipString, out ip))
                    break;
                fail = true;
            }

            string portString = string.Empty;
            int port = -1;
            fail = false;
            while (true)
            {
                Console.Clear();
                if (fail) Console.WriteLine("Please try again.");
                Console.WriteLine($"Enter Port for host '{ipString}'and hit enter. '11000'");
                portString = Console.ReadLine();
                if (int.TryParse(portString, out port))
                    break;
                fail = true;
            }
            Connect(ip, port);
        }

        private static void Connect(IPAddress ip, int port)
        {
            _TcpClient = AsynchronousClient.Connect(ip, port);
            _TcpIpConnected = true;
        }
        #endregion


    }
    #region enum
    enum NextMenu
    {
        Quit,
        Main,
        DisConnectTcp,
        ConnectTo,
        SendMessage,
        None
    }
    #endregion
}
