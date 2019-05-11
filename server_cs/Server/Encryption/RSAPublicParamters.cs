using Newtonsoft.Json;

namespace Server.Encryption
{
    public class RSAPublicParamters
    {
        public string modulus;
        public string exponent;

        public RSAPublicParamters(string modulus, string exponent)
        {
            this.modulus = modulus;
            this.exponent = exponent;
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
