using System.Linq;

using Newtonsoft.Json;

namespace Server.Encryption
{
    public class RSAPublicParamters
    {
        public sbyte[] modulus;
        public sbyte[] exponent;

        public byte[] Modulus
        {
            get { return modulus.Select(e => (byte)e).ToArray(); }
            set { modulus = value.Select(e => (sbyte)e).ToArray(); }
        }
        public byte[] Exponent
        {
            get { return exponent.Select(e => (byte)e).ToArray(); }
            set { exponent = value.Select(e => (sbyte)e).ToArray(); }
        }

        public static RSAPublicParamters FromJson(string json)
        {
            return JsonConvert.DeserializeObject<RSAPublicParamters>(json);
        }

        public string ToJson()
        {
            return JsonConvert.SerializeObject(this);
        }
    }
}
