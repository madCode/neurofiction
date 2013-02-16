
# emotiv_analysis_functions.R 
# Copyright Hannu Rajaniemi 2012

# Functions for analysing emokit-java data. 



# Filtering ---------------------------------------------------------------

lowPassEmotivData <- function(eeg.data,Fny=64,Fc){
  # Carry out low-pass filtering on EEG data using a fifth-order Butterworth filter. 
  # Args: 
  #   eeg.data: A data frame of emokit-java data from the default PostgreSQL database. 
  #   Fny: The Nyquist sampling frequency for the signal. The Emotiv sampling rate is 128Hz, so this defaults to 64. 
  #   Fc: The desired cutoff frequency. 
  # Returns: A data frame where the low-pass filter has been applied to each factor (except timestamp and the gyro data.)
  # FIXME should have more filtering options. 
  
  # Load the signal package. 
  require(signal)
  
  # Fc must be smaller than Fny. 
  if (Fc>Fny){
    stop("The cutoff frequency Fc must be smaller than the Nyquist frequency Fny.")
  }
  
  # Create a Butterworth filter. Note that the 'signal' package wants units in normalised frequencies. 
  bf <- butter(5,Fc/Fny,type="low")
  
  # Get the sensor names.   
  sensors.all <- colnames(eeg.data)
  # Ignore some of the sensors. 
  sensors.ignore <- c("id","gyrox","gyroy","timestamp","session_id","battery")
  
  sensors <- setdiff(sensors.all,sensors.ignore)
  # Create a data frame to hold the results. 
  # message("Filtering sensors ",paste(sensors,sep=" "))
  
  # Initialise an empty data frame of appropriate size. 
  eeg.data.filtered <- eeg.data
  
  # Now apply filters to the sensors. 
  for (sensor in sensors){
    eeg.data.filtered[[sensor]] <- filter(bf,eeg.data[[sensor]])    
  }
  
  return(eeg.data.filtered)
  
}


# Histograms 


compareSensorHistograms <- function(eeg.data1,eeg.data2,sensor,xlimits=c(8000,9000)){
	# Plots alpha-blended histograms of two EEG sensors. Note that this requires that data is loaded into a data frame in workspace. 
	# Args: 
	# 	eeg.data1,eeg.data2: EEG data data frames. 
	#	sensor: sensor name from "id"          "af3"         "af3_quality" "af4"         "af4_quality" "f3"          "f3_quality" 
 # "f4"          "f4_quality"  "f7"          "f7_quality"  "f8"          "f8_quality"  "fc5"        
 # "fc5_quality" "fc6"         "fc6_quality" "o1"          "o1_quality"  "o2"          "o2_quality" 
# "p7"          "p7_quality"  "p8"          "p8_quality"  "t7"          "t7_quality"  "t8"         
# "t8_quality"  "battery"     "gyrox"       "gyroy"       "timestamp"   
	
histo.1 <- hist(eeg.data1[[sensor]],breaks=1000,plot=FALSE)
histo.2 <- hist(eeg.data2[[sensor]],breaks=1000,plot=FALSE)

ymax <- max(max(histo.1$density),max(histo.2$density))


plot(histo.1,col=rgb(0,0,1,1/4),ylim=c(0,ymax),xlab="",border=rgb(0,0,0,0),freq=FALSE,main=sensor)  # first histogram
plot(histo.2,col=rgb(1,0,0,1/4),add=T,border=rgb(0,0,0,0),xlab="",freq=FALSE)
	
		
}

sensorHistogramComparisonGrid <- function(eeg.data1,eeg.data2,xlimits=c(8000,9000)){
  # Plots a grid of alpha-shaded histograms for two different EEG datasets for each sensor. 
  
  sensors <- c("af3","af4","f3","f4","f7","f8","fc5","fc6","o1","o2","p7","p8","t7","t8","gyrox","gyroy")
  par(mfrow=c(4,4))
  
  for (sensor in sensors){
    compareSensorHistograms(eeg.data1,eeg.data2,sensor)

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