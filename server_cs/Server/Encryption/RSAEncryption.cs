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

            byte[] modulus = publicKey.Modulus;
            byte[] exponent = publicKey.Exponent;

            // Add Java sign byte
            if ((modulus[0] & (1 << 7)) != 0)
                modulus = new byte[] { 0 }.Concat(modulus).ToArray();
            if ((exponent[0] & (1 << 7)) != 0)
                modulus = new byte[] { 0 }.Concat(exponent).ToArray();

            return new RSAPublicParamters
            {
                Modulus = modulus,
                Exponent = exponent
            };
        }
    }
}
