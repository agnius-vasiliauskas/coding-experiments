using System;

class Program
{
    static int ProcessVariable(int i)
    {
        int res;

        res =
            //i * i
        (int)Math.Sqrt(i)
            //i >> 1
        ;

        return res;
    }

    static void InitArrayParallel(int[] num, int clusters)
    {
        int clustcount = num.Length / clusters;

        Parallel.For(1, clusters + 1, k =>
        {
            int ixlow = (k - 1) * clustcount;
            int ixhigh = ixlow + clustcount;

            for (int i = ixlow; i < ixhigh; i++)
            { num[i] = ProcessVariable(i); }
        });
    }

    static void InitArraySerial(int[] num)
    {
        for (int i = 1; i < num.Length; i++)
        { num[i] = ProcessVariable(i); }
    }

    static void Main(string[] args)
    {
        int limit = 10000000;
        int[] num = new int[limit];
        double ds = 0.0;
        double dp = 0.0;
        double speed = 0.0;
        int avg_count = 6;
        DateTime t1, t2;

        for (int c = 1; c <= limit; c *= 2)
        {
            ds = 0.0;
            dp = 0.0;
            for (int i = 1; i <= avg_count; i++)
            {
                t1 = DateTime.Now;
                InitArraySerial(num);
                t2 = DateTime.Now;
                ds += ((TimeSpan)(t2 - t1)).TotalMilliseconds;
                t1 = DateTime.Now;
                InitArrayParallel(num, c);
                t2 = DateTime.Now;
                dp += ((TimeSpan)(t2 - t1)).TotalMilliseconds;
            }
            speed = ds / dp;
            Console.WriteLine("Data clusters {1}; Speed-up factor {0:F2}", speed, c);
        }
        Console.ReadLine();
    }
}
