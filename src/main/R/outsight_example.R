
setwd("~/Documents/Projects/outsight/src/main/R")
source("emotiv_helper_functions.R")
source("emotiv_analysis_functions.R")
source("emotiv_classification.R")
source("outsight_scene_transitions.R")

story <- data.frame(start="Dreaming Princess",training=c("Life","Death"),scenes=c("Kiss","Dwarfs","Hunter","Queen"),visited=c(FALSE, FALSE, FALSE, FALSE),fin=c("Ending: Life","Ending: Death"),stringsAsFactors=FALSE)

# The canonical column ordering is K, D, H, Q. The row gives the transition probabilities to a new scene. 
transition.death <- matrix(c(0.0,0.2,0.6,0.2,0.2,0.0,0.2,0.2,0.2,0.6,0.0,0.6,0.6,0.2,0.2,0.0),nrow=4,byrow=TRUE)
transition.life <- matrix(c(0.0,0.6,0.2,0.6,0.6,0.0,0.6,0.2,0.2,0.2,0.0,0.2,0.2,0.2,0.2,0.0),nrow=4, byrow=TRUE)

# Load some example sessions - nothing to do with Snow White, but just to show it works... 
sam.frown <- loadEmotivSession("Sam Frowning")
sam.zelazny <- loadEmotivSession("Sam - Zelazny")
sam.interested <- loadEmotivSession("Sam - Wilde Happy PRince")
sam.sw <- loadEmotivSession("Sam Snow White")
sam.smile <- loadEmotivSession("Sam Smiling")
sam.shorts <- loadEmotivSession("Sam - hitrecord shorts")

# List in the order life, death, kiss, dwarfs, hunter, queenf
eeg.test <- list(sam.smile,sam.frown,sam.zelazny,sam.shorts,sam.interested,sam.sw)

test.sequence <- generateStorySequence(story,eeg.test,t.death=transition.death,t.life=transition.life)
show(test.sequence)

