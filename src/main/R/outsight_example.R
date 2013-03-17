
setwd("~/Documents/Projects/outsight/src/main/R")
source("emotiv_helper_functions.R")
source("emotiv_analysis_functions.R")
source("emotiv_classification.R")

story <- data.frame(start="Dreaming Princess",training=c("Life","Death"),scenes=c("Kiss","Dwarfs","Hunter","Queen"),visited=c(FALSE, FALSE, FALSE, FALSE),fin=c("Ending: Life","Ending: Death"),stringsAsFactors=FALSE)

# The column ordering is K, D, H, Q. The row gives the transition probabilities to a new scene. 
transition.death <- matrix(c(0.0,0.2,0.6,0.2,0.2,0.0,0.2,0.2,0.2,0.6,0.0,0.6,0.6,0.2,0.2,0.0),nrow=4,byrow=TRUE)
transition.life <- matrix(c(0.0,0.6,0.2,0.6,0.6,0.0,0.6,0.2,0.2,0.2,0.0,0.2,0.2,0.2,0.2,0.0),nrow=4, byrow=TRUE)

# Load some examples - nothing to do with Snow White, but just to show it works... 
sam.frown <- loadEmotivSession("Sam Frowning")
sam.zelazny <- loadEmotivSession("Sam - Zelazny")
sam.interested <- loadEmotivSession("Sam - Wilde Happy PRince")
sam.sw <- loadEmotivSession("Sam Snow White")
sam.smile <- loadEmotivSession("Sam Smiling")
sam.shorts <- loadEmotivSession("Sam - hitrecord shorts")

eeg.test <- list(sam.smile,sam.frown,sam.zelazny,sam.shorts,sam.interested,sam.sw)


test.sequence <- generateStorySequence(story,eeg.test,t.death=transition.death,t.life=transition.life)


generateStorySequence <- function(story,eeg.sessions,t.death,t.life){
  # Generates a story sequence based on emotiv data sessions. 
  # Args: 
  #   story: a container for the story information. Here just a data frame. 
  #   eeg.sessions: A list of EEG session data frames. The first two are "life" and "death". 
  #   The rest should be in same order as the scene list. (Kiss, Dwarfs, Hunter, Queen)
  
  # FIXME Lists can have names, so should use names for the scenes...! 
  scene.sequence <- list()
  eeg.life <- eeg.sessions[[1]]
  eeg.death <- eeg.sessions[[2]]
  eeg.scenes <- eeg.sessions[3:length(eeg.sessions)]
  
  
  # Start by sampling randomly. 
  current  <- sample(story$scenes,size=1,prob=c(0.25,0.25,0.25,0.25))
  story$visited[match(current,story$scenes)] <- TRUE
  scene.sequence[[1]] <- current
  
  show(current)
  # Keep track of total life/death score. 
  total.life <- emotivSessionDistance(eeg.life,eeg.scenes[[match(current,story$scenes)]])
  total.death <- emotivSessionDistance(eeg.life,eeg.scenes[[match(current,story$scenes)]])
  show(total.life)
  show(total.death)
  
  # After the initial scene, generate 
  for (i in 1:3){
    scene.new <- nextScene(current,
                          scenes=story$scenes,
                         visited=story$visited,
                         eeg.current=eeg.scenes[[match(current,story$scenes)]],
                         eeg.life=eeg.life,eeg.death=eeg.death,t.death=t.death,t.life=t.life)  
    current <- scene.new$nextscene
    show(current)
    # Update visited list. 
    story$visited[match(current,story$scenes)] <- TRUE
    show(story$visited)
    # Update life / death score. 
    total.life <- total.life + scene.new$life
    total.death <- total.death <- scene.new$death
  
    scene.sequence[[i+1]] <- current
  }
  # Last remaining scene is... 
  # scene.sequence[[4]] <- story$scenes[match(FALSE,story$visited)]
  
  # decide end scene
  if (total.life < total.death){
    scene.sequence[[5]] <- story$fin[1]
  } else {
    scene.sequence[[5]] <- story$fin[2]
    
  }
  
  return(scene.sequence)
  # TODO possibly return other objects, too... 
}

nextScene <- function(current,scenes,visited,eeg.current,eeg.life,eeg.death,t.death,t.life){
  # Outputs the next scene in the sequence. 
  # Args: 
  #   current: a string that gives the current scene. 
  #   scenes: a character vector that gives the list of scenes. 
  #   eeg.life, death, current: EEG session data. 
  #   t.death, t.life: transition matrices.   
  # Returns a data frame with fields nextscene, d.life, d.death.
  
  # Update transition matrices. 
  next.scene <- data.frame(nextscene="FOO",life=0.0,death=0.0,stringsAsFactors=FALSE)
  
  t.death.up <- updateTransitionMatrix(visited,t.death)
  t.life.up <- updateTransitionMatrix(visited,t.life)
  
  next.scene$life <- emotivSessionDistance(eeg.life,eeg.current)
  next.scene$death <- emotivSessionDistance(eeg.death,eeg.current)
  
  if (next.scene$life < next.scene$death){
    next.scene$nextscene <- sample(scenes,1,prob=t.life.up[,match(current,scenes)])
  } else {
    next.scene$nextscene <- sample(scenes,1,prob=t.death.up[,match(current,scenes)])
    
  }
  
  return(next.scene)
}

updateTransitionMatrix <- function(visited,transition){
  # Update scene transition matrix to reflect visited scenes. 
  # Args: 
  #   visited: a logical vector that lists visited scenes. 
  #   transitions: a matrix of transition probabilities
  
  i<-1 
  for (s in visited){
    if (s == TRUE){
      # Make transition probability zero. 
      transition[i,] <- 0.0
      # Normalise columns. 
      for (j in 1:4)
      transition[,j] <- transition[,j]/sum(transition[,j])
      
    }
    i<- i+1
  }
  # FIXME remove this
  show(transition)
  return(transition)
  
}

