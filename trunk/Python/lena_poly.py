from sga import sgaGenotype
from PIL import Image
import aggdraw as ad

def ConvertToImage(gen,n):
	bk = (255,255,255)
	im = Image.new("RGB",(200,200),bk)
	draw = ad.Draw(im)
	for gr in gen.Lgroups:
		br = ad.Brush((gr[10].Genes[n],gr[11].Genes[n],gr[12].Genes[n]),127)
		draw.polygon([(gr[0].Genes[n] if i%2==0 else gr[1].Genes[n]) + \
					 loc.Genes[n] for i,loc in enumerate(gr[2:10])], None, br)
		draw.flush()
	return im

def MeasureColorDifference(gen,n):
		global data
		dif = 0
		for co,cg in zip(data,list(ConvertToImage(gen,n).getdata())):
			for x,y in zip(co,cg):
				dif+=abs(x-y)
		return dif

def BetterFoundNotify(gen, it):
	global cycles
	print "Iteration",it,"of",cycles,';','Fitness',gen.Fitness[2]
	ConvertToImage(gen,2).save('/tmp/0000'+str(it)+'.png')

data = list(Image.open("LenaC.png").getdata()) # 200x200 pixels Lena image
cycles = 100000  # How much iterations do we try
dxy = 50

gen = sgaGenotype([range(dxy,200-dxy),range(dxy,200-dxy) # polygon center x,y coordinates
		,range(-dxy,dxy+1),range(-dxy,dxy+1)   # polygon 1 point dx,dy \
		,range(-dxy,dxy+1),range(-dxy,dxy+1)   # polygon 2 point dx,dy \
		,range(-dxy,dxy+1),range(-dxy,dxy+1)   # polygon 3 point dx,dy \
		,range(-dxy,dxy+1),range(-dxy,dxy+1)   # polygon 4 point dx,dy \
		, [x for x in range(220) if x%51 == 0]   # polygon fill color R component\
		, [x for x in range(220) if x%51 == 0]   # polygon fill color G component\
		, [x for x in range(220) if x%51 == 0] ] # polygon fill color B component\
		, 100 # Number of polygons\
		, MeasureColorDifference # Fitness function, for determining picture quality\
		, BetterFoundNotify # Notify function, informs when better solution is found\
		, Minimize = True) # Do we need to minimize or maximize fitness function
gen.Evolve(cycles)
print "Done. Now you can go to drink coffee."
