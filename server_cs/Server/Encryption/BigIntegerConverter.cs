using System;
using System.Linq;
using System.Numerics;

namespace Server.Encryption
{
    public static class BigIntegerConverter
    {
        public static byte[] ToJavaByteArray(this BigInteger integer)
        {
            return integer.ToByteArray().Reverse().Concat(new byte[] { 0 }).ToArray();
        }

        public static string ToJavaString(this BigInteger integer)
        {
            return new BigInteger(integer.ToJavaByteArray()).ToString();
        }

        public static BigInteger ParseJavaString(string value)
        {
            BigInteger integer = BigInteger.Parse(value);

            byte[] bytes = integer.ToByteArray();

            return new BigInteger(bytes.Take(bytes.Count() - 1).Reverse().ToArray());
        }
    }
}
