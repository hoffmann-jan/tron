using System.Collections.Generic;
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
            var cspParameters = new RSAParameters()
            {
                Modulus = BigIntegerConverter.ParseJavaString(paramters.modulus).ToByteArray(),
                Exponent = BigInteger.Parse(paramters.exponent).ToByteArray()
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
