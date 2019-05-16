using System;
using System.IO;
using System.Net;
using Newtonsoft.Json;

namespace Server
{ 
    class Program
    {
        /// <summary>
        /// The config. Contains AppConfig.json properties.
        /// </summary>
        public static dynamic Config;

        // Main function
        // *PRODUCTION* Use config file.
        // *DEBUG* Use Globals for configuration.
        public static int Main(string[] args)
        {

            try
            {
                string path = Path.Combine(Directory.GetCurrentDirectory(), "AppConfig.json");
                string file = File.ReadAllText(path);
                Config = JsonConvert.DeserializeObject(file);
            }
            catch (Exception exception)
            {
                Console.Error.WriteLine(exception.Message);
                Console.WriteLine($"{Environment.NewLine}Press ENTER to continue...");
                Console.Read();

                return -1;
            }

            // Start the Server
            string ipString = Config.IP;
            TronServer.StartTronServer(IPAddress.Parse(ipString), Convert.ToInt32(Config.Port));
            return 0;
        }
    }
}
