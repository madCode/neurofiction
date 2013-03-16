
# emotiv_analysis_functions.R 
# Copyright Hannu Rajaniemi 2012

# Functions for analysing emokit-java data. 



# Filtering ---------------------------------------------------------------

# FIXME it does not feel like these functions work at all like they should... ? 

lowPassEmotivData <- function(data,Fs=128,Fc){
  # Carry out low-pass filtering on EEG data using a fifth-order Butterworth filter. 
  # Args: 
  #   eeg.data: A data frame of emokit-java data from the default PostgreSQL database. 
  #   Fny: The Nyquist sampling frequency for the signal. The Emotiv sampling rate is 128Hz, so this defaults to 64. 
  #   Fc: The desired cutoff frequency. 
  #   order: the order for the default Butterworth filter. 
  # Returns: A data frame where the low-pass filter has been applied to each factor (except timestamp and the gyro data.)
  # FIXME should have more filtering options. 
  # FIXME note that this includes the windowing artefacts! 
  # Load the signal package. 
  require(signal)
  
  # Fc must be smaller than Fny. 
  if (Fc>Fs/2){
    stop("The cutoff frequency Fc must be smaller than the Nyquist frequency Fny.")
  }
  
  sensors <- c("af3","af4","f3","f4","f7","f8","fc5","fc6","o1","o2","p7","p8","t7","t8","gyrox","gyroy")

  # Hacky way to create a data frame with these column names. 
  data.filtered <- data   

  for (s in sensors){
    data.filtered[[s]] <- lowPassFilterSensor(eeg.data=data,sensor=s,cutoff=Fc)
  }
  
  return(data.filtered)
  
}



highPassEmotivData <- function(eeg.data,Fs=64,Fc,order=5){
  # Carry out high-pass filtering on EEG data using a fifth-order Butterworth filter. 
  # Args: 
  #   eeg.data: A data frame of emokit-java data from the default PostgreSQL database. 
  #   Fny: The Nyquist sampling frequency for the signal. The Emotiv sampling rate is 128Hz, so this defaults to 64. 
  #   Fc: The desired cutoff frequency. 
  #   order: the order for the default Butterworth filter. 
  # Returns: A data frame where the low-pass filter has been applied to each factor (except timestamp and the gyro data.)
  # FIXME should have more filtering options. 
  
  # Load the signal package. 
  require(signal)
  
  # Fc must be smaller than Fny. 
  if (Fc>Fny){
    stop("The cutoff frequency Fc must be smaller than the Nyquist frequency Fny.")
  }
  
  # Create a Butterworth filter. Note that the 'signal' package wants units in normalised frequencies. 
  # 
  bf <- butter(order,Fc/Fny,type="high")
  
  # Get the sensor names.   
  sensors.all <- colnames(eeg.data)
  # Ignore some of the sensors. 
  sensors.ignore <- c("id","gyrox","gyroy","timestamp","session_id","battery")
  
  sensors <- setdiff(sensors.all,sensors.ignore)
  
  # Initialise an empty data frame of appropriate size. 
  eeg.data.filtered <- eeg.data
  
  # Now apply filters to the sensors. 
  for (sensor in sensors){
    eeg.data.filtered[[sensor]] <- filter(bf,eeg.data[[sensor]])    
  }
  
  return(eeg.data.filtered)
  
  
  
  
  
  
  
  
  
}


# Histograms ------------

compareSensorHistograms <- function(eeg.data1,eeg.data2,sensor,xlimits=c(8000,9000)){
	# Plots alpha-blended histograms of two EEG sensors. Note that this requires that data is loaded into a data frame in workspace. 
	# Args: 
	# 	eeg.data1,eeg.data2: EEG data data frames. 
	#	sensor: sensor name from "id"          "af3"         "af3_quality" "af4"         "af4_quality" "f3"          "f3_quality" 
 # "f4"          "f4_quality"  "f7"          "f7_quality"  "f8"          "f8_quality"  "fc5"        
 # "fc5_quality" "fc6"         "fc6_quality" "o1"          "o1_quality"  "o2"          "o2_quality" 
# "p7"          "p7_quality"  "p8"          "p8_quality"  "t7"          "t7_quality"  "t8"         
# "t8_quality"  "battery"     "gyrox"       "gyroy"       "timestamp"   
	
  s.1 <- eeg.data1[[sensor]]
  s.2 <- eeg.data2[[sensor]]
  
  # Note that hist has to be explictly told to use a fixed number of breaks or it changes them... and we'll need this later for classification. 
  # See https://stat.ethz.ch/pipermail/r-help/2008-May/162498.html
histo.1 <- hist(s.1,breaks=seq(min(s.1),max(s.1),length=1000),plot=FALSE)
histo.2 <- hist(eeg.data2[[sensor]],breaks=seq(min(s.2),max(s.2),length=1000),plot=FALSE)

ymax <- max(max(histo.1$density),max(histo.2$density))


plot(histo.1,col=rgb(0,0,1,1/4),ylim=c(0,ymax),xlab="",border=rgb(0,0,0,0),freq=FALSE,main=sensor)  # first histogram
plot(histo.2,col=rgb(1,0,0,1/4),add=T,border=rgb(0,0,0,0),xlab="",freq=FALSE)
	
		
}

compareSensorDensities <- function(eeg.data1,eeg.data2,sensor){
  # Same as above but using kernel smoothing. TODO determine a good kernel... 
  d.1 <- density(eeg.data1[[sensor]])
  d.2 <- density(eeg.data2[[sensor]])
  
  plot(d.1,main=sensor,xlab="",col=rgb(0,0,0,0))
  polygon(d.1,col=rgb(0,0,1,1/4),border=rgb(0,0,0,0))
  polygon(d.2,col=rgb(1,0,0,1/4),border=rgb(0,0,0,0))
  
}

compareFilteredSensorDensities <- function(eeg.data1,eeg.data2,sensor,chop=20){
  # Same as above but using kernel smoothing. TODO determine a good kernel... 
  sensor.1 <- eeg.data1[[sensor]]
  sensor.2 <- eeg.data2[[sensor]]
  
  # Here we chop the time series from the beginning and the end to reduce the Butterworth windowing error.  
  sensor.1.chop <- window(sensor.1, start=chop, end=floor(length(sensor.1))-chop)
  sensor.2.chop <- window(sensor.2, start=chop, end=floor(length(sensor.2))-chop)
  
  d.1 <- density(sensor.1.chop)
  d.2 <- density(sensor.2.chop)
  
  plot(d.1,main=sensor,xlab="",col=rgb(0,0,0,0))
  polygon(d.1,col=rgb(0,0,1,1/4),border=rgb(0,0,0,0))
  polygon(d.2,col=rgb(1,0,0,1/4),border=rgb(0,0,0,0))
  
}



histogramComparisonGrid <- function(eeg.data1,eeg.data2,xlimits=c(8000,9000)){
  # Plots a grid of alpha-shaded histograms for two different EEG datasets for each sensor. 
  
  sensors <- c("af3","af4","f3","f4","f7","f8","fc5","fc6","o1","o2","p7","p8","t7","t8","gyrox","gyroy")
  par(mfrow=c(4,4))
  
  for (sensor in sensors){
    compareSensorHistograms(eeg.data1,eeg.data2,sensor)

  }
}
  
densityComparisonGrid <- function(eeg.data1,eeg.data2){
# Plots a grid of alpha-shaded histograms for two different EEG datasets for each sensor. 
    
  sensors <- c("af3","af4","f3","f4","f7","f8","fc5","fc6","o1","o2","p7","p8","t7","t8","gyrox","gyroy")
  par(mfrow=c(4,4))
    
    for (sensor in sensors){
      compareSensorDensities(eeg.data1,eeg.data2,sensor)
      
    }  
  
  
}

filteredDensityComparisonGrid <- function(eeg.data1,eeg.data2,ch=20){
  # Compare densities low-pass filtered data. 
  sensors <- c("af3","af4","f3","f4","f7","f8","fc5","fc6","o1","o2","p7","p8","t7","t8","gyrox","gyroy")
  par(mfrow=c(4,4))
  op <- par(mar = par("mar")/2)
  for (s in sensors){
    compareFilteredSensorDensities(eeg.data1,eeg.data2,sensor=s)
        
  }
  
  
}


plotSensorHistograms <- function(eeg.data,xlimits=c(8500,9000)){
	# Plot histograms for all the Emotiv sensors for the session. 
	# Args: 
	#	eeg.data: A data frame with the Emotiv data. 
	#	xlim=xlimits: Voltage range to be used in the plot.  
	
	attach(eeg.data)
	par(mfrow=c(3,5))
	
	hist(af3,breaks=1000,xlim=xlimits)
	hist(af4,breaks=1000,xlim=xlimits)	
	hist(f3,breaks=1000,xlim=xlimits)
	hist(f4,breaks=1000,xlim=xlimits)
	hist(f7,breaks=1000,xlim=xlimits)
	hist(f8,breaks=1000,xlim=xlimits)
	hist(fc5,breaks=1000,xlim=xlimits)
	hist(fc6,breaks=1000,xlim=xlimits)
	hist(t7,breaks=1000,xlim=xlimits)
	hist(t8,breaks=1000,xlim=xlimits)
	hist(p7,breaks=1000,xlim=xlimits)	
	hist(p8,breaks=1000,xlim=xlimits)
	hist(o1,breaks=1000,xlim=xlimits)
	hist(o2,breaks=1000,xlim=xlimits)		
		
	detach(eeg.data)
}


# Spectral analysis ------------------

plotSensorSpectrum <- function(eeg.data,sensor,Fs=128,smoothw=100){
  # Plots the periodogram of a sensor from session. 
  # Args:
  #   eeg.data: a data frame from an emokit-java session. 
  #   sensor: one of "af3","af4","f3","f4","f7","f8","fc5","fc6","o1","o2","p7","p8","t7","t8","gyrox","gyroy"
  #   Fs: sampling frequency - defaults to 128 Hz. 
  #   smoothw: smoothing window for the kernel method used by spec.prgram
  data <- eeg.data[[sensor]]
  
  sensor.pr <- spec.pgram(data,span=c(smoothw,smoothw),xaxt="n",xlab="frequency (Hz)")
  axis(side=1, at=(0:5)/10, labels = Fs*(0:5)/10)
  
}

compareSensorPgrams <- function(eeg.data1,eeg.data2,sensor,Fs=128,smoothw=100){
  # Contrasts the periodograms for two different sessions for a sensor. 
  # Args: 
  #   eeg.data1,eeg.data2: data frames storing emokit-java sessions. 
  #   sensor: sensor: one of "af3","af4","f3","f4","f7","f8","fc5","fc6","o1","o2","p7","p8","t7","t8","gyrox","gyroy"
  #   Fs: sampling frequency - defaults to 128 Hz. 
  #   smoothw: smoothing window for the kernel method used by spec.prgram
  
  prgram1 <- spec.pgram(eeg.data1[[sensor]],plot=FALSE,span=c(smoothw,smoothw))
  prgram2 <- spec.pgram(eeg.data2[[sensor]],plot=FALSE,span=c(smoothw,smoothw))
  
  plot(prgram1,col=rgb(0,0,1,1/4),xaxt="n",xlab="",main="")
  plot(prgram2,col=rgb(1,0,0,1/4),add=T,xaxt="n",xlab="frequency (Hz)",main="")
  axis(side=1, at=(0:5)/10, labels = Fs*(0:5)/10)
  
}

pgramComparisonGrid <- function(eeg.data1,eeg.data2,sw=100){
  # Plots a grid of periodogram comparisons for every sensor. 
  # FIXME: plot labels should include sensors and overwrite the spec.pgram labels. 
  
  sensors <- c("af3","af4","f3","f4","f7","f8","fc5","fc6","o1","o2","p7","p8","t7","t8","gyrox","gyroy")
  par(mfrow=c(4,4))
  
  for (sensor1 in sensors){
    compareSensorPgrams(eeg.data1,eeg.data2,sensor=sensor1,smoothw=sw)
    
  }
  
}
  
lowPassFilterSensor <- function(eeg.data,sensor,cutoff,chop=20){
  # Butterworth filter (3rd order) a sensor from Emotiv data. 
  # Args: 
  #   sensor: one of  c("af3","af4","f3","f4","f7","f8","fc5","fc6","o1","o2","p7","p8","t7","t8","gyrox","gyroy")
  #   cutoff: the cutoff frequency in Hz. 
  #   chop: the length (in seconds) to cut from beginning and end to eliminate ringing effects. 
  # Returns: 
  #   filtered sensor time series
  s.original <- eeg.data[[sensor]]
  bf <- butter(3,cutoff/128)
  s.filtered <- filter(bf,s.original)
  # A hack to fix windowing effect here - chop a "chop" length from beginning and end
  # s.filtered.chop <- window(s.filtered,start=start(s.filtered)+chop,end=end(s.filtered)-chop)
  return(s.filtered)
}

compareFiltered2Original <- function(eeg.data,s,cut){
  # Shows a plot that compares the filtered signal to the original, with sensibly defined y ranges.
  # Args:
  #   s: sensor
  #   cut: cutoff frequency
  
  s.original <- eeg.data[[s]]
  s.filtered <- lowPassFilterSensor(eeg.data,sensor=s,cutoff=cut)
  max.original <- max(s.original)
  max.filtered <- max(s.filtered)
  min.original <- min(s.original)
  min.filtered <- min(s.filtered)
  # Usually the filtering introduces an artefact in the beginning so ... 
  ymin <- min.original - 50
  ymax <- max.original + 50
  
  plot(s.original,main=s,ylim=c(ymin,ymax),col=rgb(0,0,1,1/4))
  lines(s.filtered,col="red")
  
}  
  
gridFilteredVsOriginal <- function(data,cutoff){
  # Plot a grid of filtered signals compared to the originals. 
  
  sensors <- c("af3","af4","f3","f4","f7","f8","fc5","fc6","o1","o2","p7","p8","t7","t8","gyrox","gyroy")

  par(mfrow=c(4,4))
  op <- par(mar = par("mar")/2)
  
  for (sensor in sensors){
    compareFiltered2Original(eeg.data=data,s=sensor,cut=cutoff)
    
  }
  
  
  
  
  
}  