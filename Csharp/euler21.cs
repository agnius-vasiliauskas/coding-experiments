using System;

class Program
{
    static long DivSum(long n)
    {
        long un = 1 + (long)Math.Sqrt(n);
        long sum = 1;

        for (int i = 2; i <= un; i++)
        {
            if (n % i == 0)
                sum += i + (n / i);
        } return sum;
    }

    static long AmicablePair(long n)
    {
        long ds = DivSum(n);
        long os = DivSum(ds);
        long ret;
        ret = (os == n && n != ds) ? ds : 0;
        return ret;
    }

    static long AmicableNumbersSum(long until)
    {
        bool[] amic = new bool[until];
        long nextAmic;
        long sum = 0;
        for (int i = 1; i < until; i++)
        {
            if (!amic[i])
            {
                nextAmic = AmicablePair(i);
                if (nextAmic > 0)
                {
                    if (nextAmic < until) amic[nextAmic] = true;
                    sum += i + nextAmic;
                }
            }
        }
        return sum;
    }

    static void Main(string[] args)
    {
        System.Diagnostics.Stopwatch sw = new System.Diagnostics.Stopwatch();
        sw.Start();
        Console.WriteLine("Amicable numbers sum {0}", AmicableNumbersSum(10000));
        sw.Stop();
        Console.WriteLine("Calculation time {0} ms", sw.ElapsedMilliseconds);
        Console.ReadLine();
    } 
}
