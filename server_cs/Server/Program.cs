using System.Net;

using DebugProperties;

namespace Server
{
    class Program
    {
        // Main function
        // *PRODUCTION* Arguments example '0.0.0.0:2132'.
        // *DEBUG* Use DebugProperties project for configuration.
        public static int Main(string[] args)
        {
#if DEBUG
            IPAddress ipAddress = IPAddress.Parse(Globals.IP);
            int port = Globals.Port;
#else
            try
            {
            string[] ipPort = args[0].Splitt(':');
            IPAddress ipAddress = IPAdress.Parse(ipPort[0]);
            int port = Convert.ToInt32(ipPort[1]);
            }
            catch (Exception exception)
            {
                Console.Error.WriteLine(exception.Message);
                Console.WriteLine($"{Environment.NewLine}Press ENTER to continue...");
                Console.Read();

                return -1;
            }
#endif

            // Start the Server
            TronServer.StartTronServer(ipAddress, port);
            return 0;
        }
    }
}
