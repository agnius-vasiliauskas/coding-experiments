import sys
from PIL import Image, ImageFilter
import operator as op
import aggdraw as d

def Dist(p1,p2):
    x1, y1 = p1
    x2, y2 = p2
    return (((x1-x2)*(x1-x2)) + ((y1-y2)*(y1-y2)))**0.5

def intesectarea(p1,p2,size):
	x1, y1 = p1
	x2, y2 = p2
	ix1, iy1 = max(x1,x2), max(y1,y2)
	ix2, iy2 = min(x1+size,x2+size), min(y1+size,y2+size)
	iarea = abs(ix2-ix1)*abs(iy2-iy1)
	if iy2 < iy1 or ix2 < ix1: iarea = 0
	return iarea

def hassimilararea(ind, clusters):
	item = op.itemgetter
	simth = 1.5
	found = False
	tx1 = min(clusters[ind],key=item(0))[0]
	ty1 = min(clusters[ind],key=item(1))[1]
	tx2 = max(clusters[ind],key=item(0))[0] + block_len
	ty2 = max(clusters[ind],key=item(1))[1] + block_len
	tarea = (tx2-tx1)*(ty2-ty1)
	for i, cl in enumerate(clusters):
		ax1 = min(cl,key=item(0))[0]
		ay1 = min(cl,key=item(1))[1]
		ax2 = max(cl,key=item(0))[0] + block_len
		ay2 = max(cl,key=item(1))[1] + block_len
		if i != ind:
			aarea = (ax2-ax1)*(ay2-ay1)
			if float(max(aarea,tarea))/min(aarea,tarea) <= simth:
				found = True
				break
	return found

def blockpoints(pix, coords, size):
    xs, ys = coords
    for x in range(xs,xs+size):
        for y in range(ys,ys+size):
            yield pix[x,y]

def colortopalette(color, palette):
	for a,b in palette:
		if color >= a and color < b:
			return b

def imagetopalette(image, palcolors):
	assert image.mode == 'L', "Only grayscale images supported !"
	pal = [(palcolors[i],palcolors[i+1]) for i in range(len(palcolors)-1)]
	image.putdata([colortopalette(c,pal) for c in list(image.getdata())])

def getparts(image, block_len):
 img = image.convert('L') if image.mode != 'L' else image
 w, h = img.size 
 parts = []
 # Bluring image for abandoning image details and noise.
 for n in range(7):
  img = img.filter(ImageFilter.SMOOTH_MORE)
 # Converting image to custom palette
 imagetopalette(img, [x for x in range(256) if x%15 == 0])
 pix = img.load()
 
 for x in range(w-block_len):
  for y in range(h-block_len):
   data = list(blockpoints(pix, (x,y), block_len)) + [(x,y)]
   parts.append(data)
   print len(parts), w*h
 parts = sorted(parts)
 return parts

def similarparts(imagparts):
 dupl = []
 th = 200
 cth = 0.05
 l = len(imagparts[0])-1
 
 for i in range(len(imagparts)-1):  
  difs=0
  for x,y in zip(imagparts[i][:l],imagparts[i+1][:l]):
   difs += abs(x-y)
  
  mean = float(sum(imagparts[i][:l])) / l
  dev = float(sum(abs(mean-val) for val in imagparts[i][:l])) / l
  if dev/mean >= cth:
    if difs <= th:
      if imagparts[i] not in dupl:
       dupl.append(imagparts[i])
      if imagparts[i+1] not in dupl:
       dupl.append(imagparts[i+1])

 return dupl

def clusterparts(parts, block_len):
 """
 Grouping nearest images into groups.
 This is done, because we need to
 filter out very small groups. We
 want to know only big differences.
 """
 parts = sorted(parts, key=op.itemgetter(-1))
 ath = 0.03
 sth = 1.5
 clusters = [[parts[0][-1]]]
 
 # assign all parts to clusters
 for i in range(1,len(parts)):
  x, y = parts[i][-1]
  
  # detect box already in cluster
  fc = []
  for k,cl in enumerate(clusters):
   for xc,yc in cl:
    ar = intesectarea((xc,yc),(x,y),block_len)
    intrat = float(ar)/(block_len*block_len)
    if intrat > ath:
   	 if not fc: clusters[k].append((x,y))
   	 fc.append(k)
   	 break
  
  # if this is new cluster
  if not fc:
   clusters.append([(x,y)])
  else:
   # re-clustering boxes if in several clusters at once
   while len(fc) > 1:
   	clusters[fc[0]] += clusters[fc[-1]]
   	del clusters[fc[-1]]
   	del fc[-1]
 
 item = op.itemgetter
 # filter out small clusters
 clusters = [clust for clust in clusters if Dist((min(clust,key=item(0))[0],min(clust,key=item(1))[1]), (max(clust,key=item(0))[0],max(clust,key=item(1))[1]))/(block_len*1.4) >= sth]
 
 # filter out clusters, which doesn`t have similar area clusters
 clusters = [clust for x,clust in enumerate(clusters) if hassimilararea(x,clusters)]
 
 print 'clusters',len(clusters)
 return clusters

def marksimilar(image, clust, size):
 """
 Mark found similar image blocks on
 original image, by applying red layer
 on similar parts of image.
 """
 if clust:
  draw = d.Draw(image)
  br = d.Brush((255,0,0),255)
  for cl in clust:
   for x,y in cl:
    draw.rectangle((x,y,x+size,y+size),None,br)
  for cl in clust:
   cx1 = min([cx for cx,cy in cl])
   cy1 = min([cy for cx,cy in cl])
   cx2 = max([cx for cx,cy in cl]) + block_len
   cy2 = max([cy for cx,cy in cl]) + block_len
   draw.rectangle([cx1,cy1,cx2,cy2],d.Pen("blue",2),None)
  draw.flush()
 return image

if __name__ == '__main__':
 if not sys.argv[1:]:
  print 'Usage: python image_file options'
  sys.exit()
 block_len = 15
 im = Image.open(sys.argv[1])
 lparts = getparts(im, block_len)
 dparts = similarparts(lparts)
 cparts = clusterparts(dparts, block_len)
 im = marksimilar(im, cparts, block_len)
 im.save('analyzed.jpg')
 im.show()
