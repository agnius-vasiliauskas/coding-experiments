/*
High resolution timer usable in Win32.
*/

#include <windows.h>
#include <math.h>

struct Timer {
	// query high resolution timer as StartTime
    void Start(void) {
        QueryPerformanceCounter(&mTimeStart);
    }
	// query high resolution timer as StopTime
    void Stop(void) {
        QueryPerformanceCounter(&mTimeStop);
    }
	// compute time elapsed in sec.
    double GetDurationInSecs(void) {
        QueryPerformanceFrequency(&mfreq);
        double duration = (double)(mTimeStop.QuadPart-mTimeStart.QuadPart)/(double)mfreq.QuadPart;
        return duration;
	}
	// test if timer working properly. 1 -> OK, 0 -> FAILED.
	bool TestTimer(void) {
		Timer tt;
		bool res = true;
		double lasttime = 0.0;
		int lim = 500000;
		for (int i=lim;i<=20*lim;i*=2) {
			tt.Start();
			for (int j=1;j<=i;++j){
				j=j;
			}
			tt.Stop();
			double cdur = tt.GetDurationInSecs();
			if (lasttime > 0.0)
			{
				if (floor(cdur/lasttime + 0.5) != 2) {
					res = false;
					break;
				}
			}
			lasttime = cdur;
		}
		return res;
	}
private:
	LARGE_INTEGER mfreq;
    LARGE_INTEGER mTimeStart;
    LARGE_INTEGER mTimeStop;
};
