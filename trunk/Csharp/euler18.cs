using System;

class Program
{
    static long MaxTotalFromTriangle()
    {
        long[][] triangle = new long[15][];
        long max = 0;
        triangle[0] = new long[] { 75 };
        triangle[1] = new long[] { 95, 64 };
        triangle[2] = new long[] { 17, 47, 82 };
        triangle[3] = new long[] { 18, 35, 87, 10 };
        triangle[4] = new long[] { 20, 04, 82, 47, 65 };
        triangle[5] = new long[] { 19, 01, 23, 75, 03, 34 };
        triangle[6] = new long[] { 88, 02, 77, 73, 07, 63, 67 };
        triangle[7] = new long[] { 99, 65, 04, 28, 06, 16, 70, 92 };
        triangle[8] = new long[] { 41, 41, 26, 56, 83, 40, 80, 70, 33 };
        triangle[9] = new long[] { 41, 48, 72, 33, 47, 32, 37, 16, 94, 29 };
        triangle[10] = new long[] { 53, 71, 44, 65, 25, 43, 91, 52, 97, 51, 14 };
        triangle[11] = new long[] { 70, 11, 33, 28, 77, 73, 17, 78, 39, 68, 17, 57 };
        triangle[12] = new long[] { 91, 71, 52, 38, 17, 14, 91, 43, 58, 50, 27, 29, 48 };
        triangle[13] = new long[] { 63, 66, 04, 68, 89, 53, 67, 30, 73, 16, 69, 87, 40, 31 };
        triangle[14] = new long[] { 04, 62, 98, 27, 23, 09, 70, 98, 73, 93, 38, 53, 60, 04, 23 };

        for (int i = 1; i < triangle.Length; i++)
        {
            for (int j = 0; j < triangle[i].Length; j++)
            {
                // Accumulating maximum total
                if (j == 0)
                {
                    triangle[i][j] += triangle[i - 1][j];
                }
                else if (j == triangle[i].Length - 1)
                {
                    triangle[i][j] += triangle[i - 1][triangle[i - 1].Length - 1];
                }
                else
                {
                    triangle[i][j] += Math.Max(triangle[i - 1][j], triangle[i - 1][j - 1]);
                }
                // Finding maximum from last row
                if (i == triangle.Length - 1)
                {
                    if (triangle[i][j] > max) max = triangle[i][j];
                }
            }
        }
        return max;
    }

    static void Main(string[] args)
    {
        System.Diagnostics.Stopwatch sw = new System.Diagnostics.Stopwatch();
        sw.Start();
        long ans = MaxTotalFromTriangle();
        sw.Stop();
        Console.WriteLine("Total sum {0}; cpu time {1} ms", ans, sw.Elapsed.TotalMilliseconds);
        Console.ReadLine();
    } 
}
