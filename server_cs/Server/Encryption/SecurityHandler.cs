namespace Server.Encryption
{
    public class SecurityHandler
    {
        public RSAEncryption RSA { get; set; } = new RSAEncryption();
        public AESEncryption AES { get; set; } = new AESEncryption();
    }
}
