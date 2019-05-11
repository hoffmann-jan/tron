using System.Numerics;
using System.Security.Cryptography;

namespace Server.Encryption
{
    public class RSAEncryption
    {
        private RSACryptoServiceProvider csp = new RSACryptoServiceProvider(2048);

        public byte[] Decrypt(byte[] cipher)
        {
            return csp.Decrypt(cipher, RSAEncryptionPadding.Pkcs1);
        }

        public RSAPublicParamters PublicJavaParamters()
        {
            RSAParameters publicKey = csp.ExportParameters(false);

            return new RSAPublicParamters(
                new BigInteger(publicKey.Modulus).ToJavaString(),
                new BigInteger(publicKey.Exponent).ToString()
            );
        }
    }
}
