using System.Linq;
using System.Numerics;

using System.Security.Cryptography;

namespace Server.Encryption
{
    public class RSAEncryption
    {
        private RSACryptoServiceProvider csp = new RSACryptoServiceProvider(4096);

        public byte[] Decrypt(byte[] cipher)
        {
            return csp.Decrypt(cipher, RSAEncryptionPadding.Pkcs1);
        }

        public RSAPublicParamters PublicJavaParamters()
        {
            RSAParameters publicKey = csp.ExportParameters(false);

            return new RSAPublicParamters
            {
                // Add Java sign bytes
                Modulus = new byte[] { 0 }.Concat(publicKey.Modulus).ToArray(),
                Exponent = new byte[] { 0 }.Concat(publicKey.Exponent).ToArray()
            };
        }
    }
}
