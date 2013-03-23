setwd("~/Documents/Projects/outsight/src/main/R")
source("emotiv_helper_functions.R")
source("emotiv_analysis_functions.R")
source("emotiv_classification.R")
source("outsight_scene_transitions.R")

# Load Lesley's sessions

sessions <- listSittingSessions("\\x27440030c74247529a4e5a7d6fcdb70a")

# Extract the ones that correspond to scenes (and not spacebar strokes)

L <- grep("md",sessions$name)
lesley.sessions <- sessions[L,]

lesley.data <- loadEmotivSessions(sessions=lesley.sessions$name)

# This reslts in a lot of identity duplication, so let's do some cleanup... 

lesley.princess <- lesley.data[[1]] # gives a list of 3 elements
lesley.princess <- lesley.princess[[3]]
lesley.life <- lesley.data[[2]]
lesley.life <- lesley.life[[3]]
lesley.death <- lesley.data[[3]]
lesley.death <- lesley.death[[3]]
lesley.hunter <- lesley.data[[4]]
lesley.queen <- lesley.data[[5]]
lesley.kiss <- lesley.data[[6]]
lesley.dwarfs <- lesley.data[[7]]
lesley.ending.death <- lesley.data[[8]]


# Now let's test the scene transitions... 
story <- data.frame(start="Dreaming Princess",
                    training=c("Life","Death"),
                    scenes=c("Kiss","Dwarfs","Hunter","Queen"),
                    visited=c(FALSE, FALSE, FALSE, FALSE),
                    fin=c("Ending: Life","Ending: Death"),
                    stringsAsFactors=FALSE)

# The canonical column ordering is K, D, H, Q. The row gives the transition probabilities to a new scene. 
transition.death <- matrix(c(0.0,0.2,0.6,0.2,0.2,0.0,0.2,0.2,0.2,0.6,0.0,0.6,0.6,0.2,0.2,0.0),nrow=4,byrow=TRUE)
transition.life <- matrix(c(0.0,0.6,0.2,0.6,0.6,0.0,0.6,0.2,0.2,0.2,0.0,0.2,0.2,0.2,0.2,0.0),nrow=4, byrow=TRUE)

# List in the order life, death, kiss, dwarfs, hunter, queen
eeg.test <- list(lesley.life,lesley.death,lesley.kiss,lesley.dwarfs,lesley.hunter,lesley.queen)

test.sequence <- generateStorySequence(story,eeg.test,t.death=transition.death,t.life=transition.life)
show(test.sequence)




