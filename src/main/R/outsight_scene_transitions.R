
# Functions for a toy version of the Outsight scene transitions. 
# ------------------------------

generateStorySequence <- function(story,eeg.sessions,t.death,t.life){
  # Generates a story sequence based on emotiv data sessions. 
  # Args: 
  #   story: a container for the story information. Here just a data frame of the form: 
  #   story <- data.frame(start="Dreaming Princess",training=c("Life","Death"),scenes=c("Kiss","Dwarfs","Hunter","Queen"),visited=c(FALSE, FALSE, FALSE, FALSE),fin=c("Ending: Life","Ending: Death"),stringsAsFactors=FALSE)
  #   eeg.sessions: A list of EEG session data frames. The first two are "life" and "death", the rest are the scenes in the canonical order. 
  #   The rest should be in same order as the scene list. (Kiss, Dwarfs, Hunter, Queen)
  
  # FIXME Lists can have names, so should use names for the scenes...! 
  scene.sequence <- list()
  eeg.life <- eeg.sessions[[1]]
  eeg.death <- eeg.sessions[[2]]
  eeg.scenes <- eeg.sessions[3:length(eeg.sessions)]
  
  
  # Start by sampling randomly. 
  current  <- sample(story$scenes,size=1,prob=c(0.25,0.25,0.25,0.25))
  # match here gets the index of the current scene from the scene list. 
  story$visited[match(current,story$scenes)] <- TRUE
  scene.sequence[[1]] <- current
  
  #  show(current)
  # Keep track of total life/death score. 
  total.life <- emotivSessionDistance(eeg.life,eeg.scenes[[match(current,story$scenes)]])
  total.death <- emotivSessionDistance(eeg.life,eeg.scenes[[match(current,story$scenes)]])
  # show(total.life)
  # show(total.death)
  
  # After the initial scene, generate the rest. 
  for (i in 1:3){
    scene.new <- nextScene(current,
                           scenes=story$scenes,
                           visited=story$visited,
                           eeg.current=eeg.scenes[[match(current,story$scenes)]],
                           eeg.life=eeg.life,eeg.death=eeg.death,t.death=t.death,t.life=t.life)  
    current <- scene.new$nextscene
    #    show(current)
    # Update visited list. 
    story$visited[match(current,story$scenes)] <- TRUE
    # show(story$visited)
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
  
  # show(transition)
  return(transition)
  
}
