static long ReverseString(long number)
{
	char[] str = number.ToString().ToCharArray();
	Array.Reverse(str);
	return long.Parse(new string(str));
} 
