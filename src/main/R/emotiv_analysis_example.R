# Comparing two datasets

setwd("~/Documents/Projects/outsight/src/main/R")
source("emotiv_helper_functions.R")
source("emotiv_analysis_functions.R")

listEmotivSessions()

# Load data into data frames
sam.bored <- loadEmotivSession("Sam - Zelazny 2")
sam.interested <- loadEmotivSession("Sam - Wilde Happy PRince")

# Compare voltage histograms. 
sensorHistogramComparisonGrid(sam.bored, sam.interested)

# Compare power spectra (periodograms) 

pgramComparisonGrid(sam.bored,sam.interested)

# FIXME this does not show a strong difference -- could have to do with normalisation? 

# TODO: data cleanup ideas
#
# * exclude when gyro movements are high
# * exclude when variance in a 1 second period is very high

# Might be better to look at the various bands, i.e. delta, theta, alpha, beta, gamma, e.g. plot how much power 
# goes into each band for each sensor. 
