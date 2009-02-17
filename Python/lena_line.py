from sga import sgaGenotype
from PIL import Image, ImageDraw

def ConvertToImage(gen,n):
	im = Image.new("L",(200,200),255)
	draw = ImageDraw.Draw(im)
	for gr in gen.Lgroups:
		draw.line([gr[0].Genes[n], \
		           gr[1].Genes[n], \
		           gr[0].Genes[n] + gr[2].Genes[n], \
		           gr[1].Genes[n] + gr[3].Genes[n]], fill = gr[4].Genes[n], width = gr[5].Genes[n] )
	return im

def MeasureColorDifference(gen,n):
		global data
		dif = 0
		for co,cg in zip(data,list(ConvertToImage(gen,n).getdata())):
			dif+=abs(co-cg)
		return dif

def BetterFoundNotify(gen, it):
	global cycles
	print "Iteration",it,"of",cycles,';','Fitness',gen.Fitness[2]
	ConvertToImage(gen,2).save('/tmp/0000'+str(it)+'.png')

data = list(Image.open("LenaG.png").getdata()) # 200x200 pixels Lena image
cycles = 100000  # How much iterations do we try
dxy = 20

gen = sgaGenotype([range(dxy,200-dxy),range(dxy,200-dxy) # line start x,y \
		,range(-dxy,dxy+1),range(-dxy,dxy+1) # line dx,dy \
		, [x for x in range(220) if x%5 == 0] # line grayscale color\
		, range(1,8) ] # line width in pixels\
		, 200 # Number of lines\
		, MeasureColorDifference # Fitness function, for determining picture quality\
		, BetterFoundNotify # Notify function, informs when better solution is found\
		, Minimize = True) # Do we need to minimize or maximize fitness function
gen.Evolve(cycles)
print "Done. Now you can go to drink coffee."
