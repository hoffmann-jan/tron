using System.Net;

namespace Server
{
    public static class Globals
    {
        public static int Port = 11000;
        public const int BufferSize = 1024;
        //public static string IP = "10.202.129.191";
        public static string IP = "127.0.0.1";
        public static string EofTag = "<EOF>";
        public static int NumberOfPlayers = 2;
    }
}
