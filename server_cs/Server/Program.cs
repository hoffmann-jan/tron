using System;
using System.IO;
using System.Net;
using Newtonsoft.Json;

namespace Server
{
    class Program
    {
        // Main function
        // *PRODUCTION* Use config file.
        // *DEBUG* Use Globals for configuration.
        public static int Main(string[] args)
        {
            IPAddress ipAddress;
            int port;
#if DEBUG
            ipAddress = IPAddress.Parse(Globals.IP);
            port = Globals.Port;
#else
            try
            {
                string path = Path.Combine(Directory.GetCurrentDirectory(), "AppConfig.json");
                string file = File.ReadAllText(path);
                dynamic config = JsonConvert.DeserializeObject(file);
                Globals.EofTag = config.EofTag;
                Globals.IP = config.IP;
                Globals.NumberOfPlayers = config.NumberOfPlayers;
                Globals.Port = config.Port;

                ipAddress = IPAddress.Parse(Globals.IP);
                port = Globals.Port;
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
