using System.Collections.Generic;
using System.Linq;
using System.Net.Sockets;
using System.Numerics;
using System.Security.Cryptography;

namespace Server.Encryption
{
    public class RSAServerClientEncryption : RSAEncryption
    {
        private Dictionary<TcpClient, RSACryptoServiceProvider> csps = new Dictionary<TcpClient, RSACryptoServiceProvider>();

        public void AddClient(TcpClient client, RSAPublicParamters paramters)
        {
            byte[] modulus = paramters.Modulus;
            byte[] exponent = paramters.Exponent;

            // Remove Java sign byte
            if (modulus[0] == 0)
                modulus = modulus.Skip(1).ToArray();
            if (exponent[0] == 0)
                exponent = exponent.Skip(1).ToArray();

            var cspParameters = new RSAParameters()
            {
                Modulus = modulus,
                Exponent = exponent
            };

            csps[client] = new RSACryptoServiceProvider();
            csps[client].ImportParameters(cspParameters);
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
