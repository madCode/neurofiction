#This file contains R code to read the ecg.csv
#file from my website, perform some digital
#signal processing, and produce a nice plot
#of the raw and filtered signals. This script
#may be run from R with the follwing command
#source("http://biostatmatt.com/R/ecg.R")

#Transform the real and imaginary portions of the 
#FFT into magnitude and phase. The argument
#ff should be the output of the fft function
amplitude <- function( x ) { sqrt(Re(x)^2+Im(x)^2) }
phase     <- function( x ) { atan(Im(x)/Re(x)) }

#sinc function of frequency f
sinc      <- function( x, f ) { ifelse(x==0, 2*pi*f, sin(2*pi*f*x)/x) }

#Blackman window from 0..m
Blackman  <- function( m ) { 0.42-0.5*cos(2*pi*(0:m)/m)+0.08*cos(4*pi*(0:m)/m) }

#Hamming window from 0..m
Hamming   <- function( m ) { 0.54-0.46*cos(2*pi*(0:m)/m) }

#simple low pass filter
#y - vector to filter
#t - time interval between measurements (s)
#f - low pass frequency (Hz)
lpf <- function( y, t, f ) {
  rc <- 1 / ( 2 * pi * f )
  a  <- t / ( t + rc )
  n  <- length( y )
  yf <- y
  for( i in 2:length(y) ) {
    yf[i] <- a * y[i] + (1-a) * yf[i-1]
  }
  return( yf )
}  

#windowed sinc low pass filter
#y - vector to filter
#t - time interval between measurements (s)
#f - low pass frequency (Hz)
wlpf <- function( y, t, f ) {
  m  <- min(floor(length(y)/2), 500)
  #generate the sinc kernel
  rk <- sinc(-m:m, f*t)  
  #apply the Blackman window
  bk <- Blackman(2*m) * rk
  #pad the filter with zeros
  k  <- c(bk, rep(0,length(y)-length(bk)))
  #convolve y with the filter kernel
  fy  <- fft(fft(k)*fft(y), inverse=TRUE)
  return(Re(fy))
}

dat  <- scan("http://biostatmatt.com/csv/ecg.csv")
dat  <- ( dat - mean(dat) ) / sd(dat)

#filter high frequency noise
fdat <- wlpf(dat, 1/1000, 30)
fdat <- (fdat-mean(fdat))/sd(fdat)

#isolate respiration bias
rdat <- wlpf(dat, 1/1000, 1)
rdat <- (rdat-mean(rdat))/sd(rdat)

#subtract respiration bias from ecg signal
edat <- fdat - rdat

require(lattice)
xplot <- rep((0:(length(dat)-1))/1000,4)
yplot <- c(dat, fdat, rdat, edat)
gplot <- c(rep("Raw",length(dat)),
           rep("High Frequency Filter",length(dat)),
           rep("Low Frequency Filter",length(dat)),
           rep("ECG",length(dat)))
tp <- xyplot(yplot~xplot|gplot,type="l",layout=c(1,4), xlab="Time", ylab="V")

#uncomment the following to save an image
#trellis.device(png, file="ecgfilter.png", height=750, width=750)
print(tp)
#dev.off()

