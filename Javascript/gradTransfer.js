function showValue(newValue,el)
{
  document.getElementById(el).innerHTML=parseFloat(newValue).toFixed(2);
  GeneratePlot();
}

function Clamp(x,a,b) {
  return Math.min(Math.max(x, a), b);
};

function NonLinearTransfer(x,a,b) {
  return (1-a)*x + a*Math.pow(1+Math.exp(b-2*b*x),-1);
};

function GeneratePlot() {
    var data = [];
    var a =  document.getElementById("rngNonlinearity").value;
    var b =  document.getElementById("rngAbruptness").value;

    for (var i = 0; i <= 1; i += 0.01)
        data.push([i, Clamp(NonLinearTransfer(i,a,b),0,1)]);
    
    $.plot($("#placeholder"),
          [{ data: data, label: "Transfer function"}],
          { 
               	xaxes: [ { min: 0, max: 1 }],
               	yaxes: [ { min: 0, max: 1 }],
		legend: { position: 'nw' }
           }
          );
    GenerateGrad();
};

function Blend(k,x,y) {
  return (1-k)*x + k*y;
}

function setPixel(imageData, x, y, r, g, b, a) {
    index = (x + y * imageData.width) * 4;
    imageData.data[index+0] = r;
    imageData.data[index+1] = g;
    imageData.data[index+2] = b;
    imageData.data[index+3] = a;
}

function GenerateGrad() {

element = document.getElementById("canvasGrad");
c = element.getContext("2d");

width = parseInt(element.getAttribute("width"));
height = parseInt(element.getAttribute("height"));

imageData = c.createImageData(width, height);

scolor = [0,255,0];
tcolor = [0,0,255];
c1 =  document.getElementById("rngNonlinearity").value;
c2 =  document.getElementById("rngAbruptness").value;

// draw gradient
for (x = 0; x < width; x++) {
  k = x/width;
  for (y = 0; y < height; y++) {
    r = Blend(NonLinearTransfer(k,c1,c2),scolor[0],tcolor[0]);
    g = Blend(NonLinearTransfer(k,c1,c2),scolor[1],tcolor[1]);
    b = Blend(NonLinearTransfer(k,c1,c2),scolor[2],tcolor[2]);
    setPixel(imageData, x, y, r, g, b, 0xff);
  }
}

c.putImageData(imageData, 0, 0);
}

