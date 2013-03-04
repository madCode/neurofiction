# Comparing two datasets

setwd("~/Documents/Projects/outsight/src/main/R")
source("emotiv_helper_functions.R")
source("emotiv_analysis_functions.R")

listEmotivSessions()

# Load data into data frames
sam.bored <- loadEmotivSession("Sam - Zelazny 2")
sam.interested <- loadEmotivSession("Sam - Wilde Happy PRince")

# Compare voltage histograms. 
histogramComparisonGrid(sam.bored, sam.interested)

# Compare power spectra (periodograms) 

pgramComparisonGrid(sam.bored,sam.interested)

# Do some filtering - cut-off 30Hz
sam.bored.low <- lowPassEmotivData(sam.bored,Fc=30,order=3)
sam.interested.low <- lowPassEmotivData(sam.bored,Fc=30,order=3)


histogramComparisonGrid(sam.bored.low,sam.interested.low)
pgramComparisonGrid(sam.bored.low,sam.interested.low)


# FIXME this does not show a strong difference -- could have to do with normalisation? 

# TODO: data cleanup ideas
#
# * exclude when gyro movements are high
# * exclude when variance in a 1 second period is very high

# TODO: 
# * look at histograms after bandpass filtering. [Having some issues with R filtering functions... low-pass filtering seems to work, but ] 
# * look at the time-frequency plots. NB: the Time Series Analysis book has nice examples on page 243
# * Make a list of features that we could extract... e.g. entropy measures
# * add density lines to histograms - nicer visualisation with filled density diagrams? In fact, nicer way to do the 
# comparison might be via the sm.density.compare (http://www.statmethods.net/graphs/density.html)
# Might be better to look at the various bands, i.e. delta, theta, alpha, beta, gamma, e.g. plot how much power 
# goes into each band for each sensor. 



