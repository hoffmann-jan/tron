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
#if DEBUG
            IPAddress ipAddress = IPAddress.Parse(Globals.IP);
            int port = Globals.Port;
#else
            try
            {
                string path = Path.Combine(Directory.GetCurrentDirectory(), "AppConfig.json");
                string file = File.ReadAllText(path);
                dynamic config = JsonConvert.DeserializeObject(file);
                Globals.BufferSize = config.BufferSize;
                Globals.EofTag = config.EofTag;
                Globals.IP = config.IP;
                Globals.NumberOfPlayers = config.NumberOfPlayers;
                Globals.Port = config.Port;
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
