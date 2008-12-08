static int PrimesBelow(long until)
{
	int sum = 0;
	bool[] composite = new bool[until + 1];
	for (long i = 2; i < until; i++)
	{
		if (!composite[i])
		{
			for (long j = i + i; j <= until; j += i)
			{
				composite[j] = true;
			}
			sum++;
		}
	}
	return sum;
}
