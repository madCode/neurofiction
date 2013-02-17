# Comparing two datasets

source("emotiv_helper_functions.R")
source("emotiv_analysis_functions.R")

listEmotivSessions()

# Load data into data frames
sam.bored <- loadEmotivSession("Sam - Zelazny 2")
sam.interested <- loadEmotivSession("Sam - Wilde Happy PRince")

# Compare 
sensorHistogramComparisonGrid(sam.bored, sam.interested)

# TODO: data cleanup ideas
#
# * exclude when gyro movements are high
# * exclude when variance in a 1 second period is very high
