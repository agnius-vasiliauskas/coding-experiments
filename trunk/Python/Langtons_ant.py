from Tkinter import *

rs = raw_input("Enter rule set (e.g.: L/R) => ")
str0 = rs.split('/')[0]
str1 = rs.split('/')[1]   
i0, i1 = -1, -1
w,h = 500, 500
lx,ly = w/2, h/2
dirx,diry = 0,-1

# defining absolute directions
vec = {
    (0,1,'L'):(1,0),
    (1,0,'L'):(0,-1),
    (0,-1,'L'):(-1,0),
    (-1,0,'L'):(0,1),
    (0,1,'R'):(-1,0),
    (1,0,'R'):(0,1),
    (0,-1,'R'):(1,0),
    (-1,0,'R'):(0,-1)
    }
mod = 2
grid = dict([((x,y),0) for y in range(0,h,mod) for x in range(0,w,mod)])

# Initialize Tk window
root = Tk()
ant = Canvas(root,width=w, height=h, bg='white')
ant.pack(fill=BOTH)

while 1:
    if lx < w and ly < h and lx > 0 and ly > 0:
      if grid[(lx,ly)] == 0:
        i0 = (i0+1)%len(str0)
        rdir = str0[i0]
      elif grid[(lx,ly)] == 1:
        i1 = (i1+1)%len(str1)
        rdir = str1[i1]
      dirx, diry = vec[(dirx,diry,rdir)]
      grid[(lx,ly)] = grid[(lx,ly)]^1
      col = "white" if grid[(lx,ly)] == 0 else "darkorange"
    ant.delete("current")
    ant.create_rectangle(lx, ly, lx+mod-1, ly+mod-1, fill="black",outline="black",tags="current")
    ant.update()
    ant.delete((lx,ly))
    ant.create_rectangle(lx, ly, lx+mod-1, ly+mod-1, fill=col,outline=col,tags=(lx,ly))
    lx,ly = lx+dirx*mod, ly+diry*mod
