using System.Collections.Generic;
using System.IO;
using System.Net.Sockets;
using System.Security.Cryptography;

namespace Server.Encryption
{
    public class AESEncryption
    {
        private Dictionary<TcpClient, AesCryptoServiceProvider> csps = new Dictionary<TcpClient, AesCryptoServiceProvider>();

        public void AddClient(TcpClient client)
        {
            csps[client] = new AesCryptoServiceProvider();
            csps[client].GenerateIV();
            csps[client].GenerateKey();
        }

        public bool HasClient(TcpClient client)
        {
            return csps.ContainsKey(client);
        }

        public AESPublicParameters PublicParameters(TcpClient client)
        {
            return new AESPublicParameters()
            {
                Key = csps[client].Key,
                IV = csps[client].IV
            };
        }

        public byte[] Encrypt(string plain, TcpClient client)
        {
            ICryptoTransform encrypter = csps[client].CreateEncryptor();

            byte[] cipher;
            using (var memoryStream = new MemoryStream())
            {
                using (var cryptoStream = new CryptoStream(memoryStream, encrypter, CryptoStreamMode.Write))
                {
                    using (var streamWriter = new StreamWriter(cryptoStream))
                    {
                        streamWriter.Write(plain);
                    }
                    cipher = memoryStream.ToArray();
                }
            }
            return cipher;
        }

        public string Decrypt(byte[] cipher, TcpClient client)
        {
            ICryptoTransform decrypter = csps[client].CreateDecryptor();

            string plain = null;
            using (var memoryStream = new MemoryStream(cipher))
            {
                using (var cryptoStream = new CryptoStream(memoryStream, decrypter, CryptoStreamMode.Read))
                {
                    using (var streamReader = new StreamReader(cryptoStream))
                    {
                        plain = streamReader.ReadToEnd();
                    }
                }
            }
            return plain;
        }
    }
}
