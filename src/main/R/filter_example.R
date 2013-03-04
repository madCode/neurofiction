


bf <- butter(3,30/128)
bf2 <- butter(3,20/128)
plot(time[1:128],p7[1:128],type="l")
p7.low <- filter(bf,p7)
p7.lower <- filter(bf2,p7)
lines(time[1:128],p7.low[1:128],col="red")
lines(time[1:128],p7.lower[1:128],col="blue")

# So, low-pass filtering works. 

bf.high <- butter(3,W=2/128,type="high")
plot(time,p7,type="l")
p7.high <- filter(bf.high,p7)
lines(time,p7.high,col="red")

plot(time,p7.high,type="l")

# ... but band-pass filtering doesn't, and neither does high-pass filtering?? 


hannu.island <- loadEmotivSession("Hannu: Island of Doctor Death")
hannu.gaiman <- loadEmotivSession("Hannu: Gaiman: Fairy Reel")
hannu.gaiman <- hannu.gaiman[[2]]


histogramComparisonGrid(hannu.island,hannu.gaiman)
hannu.island.low <- lowPassEmotivData(hannu.island,Fc=30,order=3)
hannu.gaiman.low <- lowPassEmotivData(hannu.gaiman,Fc=30,order=3)
histogramComparisonGrid(hannu.island.low,hannu.gaiman.low)