static long CountTerms(long x, ref long[] terms)
{
	long tc = 1;
	long k = x;

	while (k!=1)
	{
		tc++;
		k = (k % 2 == 0) ? k / 2 : 3 * k + 1;
		if (k <= x) 
		{
			if (terms[k] > 0)
			{
				terms[x] = terms[k] + tc;
				return terms[x];
			}
		}
	}

	terms[x] = tc;

	return terms[x] ;
}

static long LongestSeq()
{
	long max = 1000000;
	long[] termcount = new long[max+1];
	long tcmax = 0;
	long c = 0;
	long ix = 0;

	for (int i = 1; i <= max; i++) 
	{
		c = CountTerms(i, ref termcount);
		if ( c > tcmax)
		{
			tcmax = c;
			ix = i;
		}
	}

	return ix;
}

static void Main(string[] args)
{
	Console.WriteLine(LongestSeq());
	Console.ReadLine();
}
