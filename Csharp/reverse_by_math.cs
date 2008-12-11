static long ReverseNumber(long number)
{
	long digit = number % 10;
	long rest = (long) number / 10;
	long o = digit;

	while (rest > 0)
	{
		digit = rest % 10;
		rest /= 10;
		o = (o*10)+ digit;
	}
	return o;
} 
