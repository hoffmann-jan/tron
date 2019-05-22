using System.Linq;

using Newtonsoft.Json;

namespace Server.Encryption
{
    public class AESPublicParameters
    {
        public sbyte[] key;
        public sbyte[] iv;

        [JsonIgnore]
        public byte[] Key
        {
            set { key = value.Select(e => (sbyte)e).ToArray(); }
        }

        [JsonIgnore]
        public byte[] IV
        {
            set { iv = value.Select(e => (sbyte)e).ToArray(); }
        }

        public static AESPublicParameters FromJson(string json)
        {
            return JsonConvert.DeserializeObject<AESPublicParameters>(json);
        }

        public string ToJson()
        {
            return JsonConvert.SerializeObject(this);
        }
    }
}
