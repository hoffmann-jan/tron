using System.Collections.Generic;
using System.Linq;
using System.Net.Sockets;
using System.Security.Cryptography;

namespace Server.Encryption
{
    public class RSAEncryption
    {
        private Dictionary<TcpClient, RSACryptoServiceProvider> csps = new Dictionary<TcpClient, RSACryptoServiceProvider>();

        public void AddClient(TcpClient client, RSAPublicParamters paramters)
        {
            var cspParameters = new RSAParameters()
            {
                Modulus = RemoveJavaSignByte(paramters.Modulus),
                Exponent = RemoveJavaSignByte(paramters.Exponent)
            };

            csps[client] = new RSACryptoServiceProvider();
            csps[client].ImportParameters(cspParameters);
        }

        private static byte[] RemoveJavaSignByte(byte[] bytes)
        {
            return bytes[0] != 0 ? bytes : bytes.Skip(1).ToArray();
        }

        public bool HasClient(TcpClient client)
        {
            return csps.ContainsKey(client);
        }

        public byte[] Encrypt(byte[] plain, TcpClient client)
        {
            return csps[client].Encrypt(plain, RSAEncryptionPadding.Pkcs1);
        }
    }
}
