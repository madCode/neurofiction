# Comparing two datasets

source("/Users/hannu/Documents/Projects/outsight/src/main/R/emotiv_helper_functions.R")
source("/Users/hannu/Documents/Projects/outsight/src/main/R/emotiv_analysis_functions.R")

listEmotivSessions()

# Load data into data frames
hannu.daydreaming <- loadEmotivSession("Daydreaming")
hannu.island <- loadEmotivSession("Hannu: Island of Doctor Death")

# Compare 
sensorHistogramComparisonGrid(hannu.daydreaming,hannu.island)
