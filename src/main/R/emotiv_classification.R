
# simple histogram intersection [done]

# Try also: 
# Kullback-Leibler  -> Jensen-Shannon

histIntersection <- function(hist1,hist2){
  # Computes the histogram intersection between two histograms a_i, b_i with N bins.  
  # http://jccaicedo.blogspot.co.uk/2012/01/histogram-intersection.html
  # Args: 
  #   hist1, hist2: R histogram objects. 
  # Returns: \sum _i ^N min (a_i,b_i)
  
  a <- hist1$density
  b <- hist2$density
  
  if (length(a) != length(b)){
    stop("The histograms must be of equal length.")
  }

  hist.int <- sum(pmin(a,b))
  
}

emotivSensorDistance <- function(eeg.data1, eeg.data2, sensor, method= "histint"){
  # Compute a probabilistic distance measure between two Emotiv sessions for a single sensor. 
  if (method == "histint"){
    s1 <- eeg.data1[[sensor]]
    s2 <- eeg.data2[[sensor]]
    
    hist.s1 <- hist(s1,breaks=seq(min(s1),max(s1),length=1000),plot=FALSE)
    hist.s2 <- hist(s2,breaks=seq(min(s2),max(s2),length=1000),plot=FALSE)
    return(histIntersection(hist.s1,hist.s2))  
  } else {
    
    stop("Please specify a valid distance measure.")
  }
  
}


emotivSessionDistance <- function(eeg.data1,eeg.data2,method="histint"){
  # Computes the histogram distance between two Emotiv sessions. 
  
  # Exclude gyroscopes. 
  sensors <- c("af3","af4","f3","f4","f7","f8","fc5","fc6","o1","o2","p7","p8","t7","t8")
  d <- vector()
  i <- 1
  
  if (method == "histint"){
    for (s in sensors){
      d[i] <- emotivSensorDistance(eeg.data1,eeg.data2,sensor=s)
      i<- i+1 
    }
  # To make similar images have smaller distance, return inverse. 
    return(1/norm_e(d))
  } else {
    stop("Please specify a valid distance measure.")
  }
  
  
  
}

norm_e <- function(x) sqrt(sum(x^2))

euclid <- function(x1,x2){
  return(sqrt((x1-x2)^2))  
}