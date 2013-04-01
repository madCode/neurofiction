
setwd("~/Documents/Projects/outsight/src/main/R")
source("lesley_example.R")

normalise <- function(x){
  return(x/sum(x))
  
}

# Compute distances from life and death scenes. 

vis.scenes <- list(lesley.kiss,lesley.dwarfs,lesley.hunter,lesley.queen,lesley.ending.death)

d.death <- unlist(lapply(vis.scenes,
                                   emotivSessionDistance,
                                   eeg.data2=lesley.death))

d.life <- unlist(lapply(vis.scenes,
                                  emotivSessionDistance,
                                  eeg.data2=lesley.life))

# Zuzana's idea: visualise the sequence of transitions and then the colours...



# TODO could also do an arcdiagram
# http://www.gastonsanchez.com/arcdiagram

# Visualising this with arcplotlibrary(arcdiagram)
# TODO load this from the actual processed file... 

lesley.path <- rbind(c("Queen","Kiss"),c("Kiss","Dwarfs"),c("Dwarfs","Hunter"),c("Hunter","Ending: Death"))
death <- "#9cf896"
life <- "#f36d73"

# Labels for nodes
node.ld <- c(death,life,life,life,death)

# Weigh node size e.g. with the distance to the archetypal life.. 
node.strength <- pmax(log(1/d.death),log(1/d.life))

# make a graph
lesley.graph <- graph.edgelist(lesley.path,directed=TRUE)

# assign weights based on the transition magnitude... 
lesley.weight <- abs(d.death-d.life)

E(lesley.graph)$weight <- 100*lesley.weight
E(lesley.graph)$weight <- 100*lesley.weight[2:5]
arcplot(lesley.path,las=1,col.nodes=node.ld,show.nodes=TRUE,bg.nodes=node.ld,cex.nodes=5*node.strength,lwd.arcs=5*E(lesley.graph)$weight,col.arcs="#77777765")

emotivLissajous <- function(length=2,step=0.01,r=0.020,apple1,apple2){
  # Makes a pretty plot about how apple colours change, plotted in 3D, as a sequence of coloured spheres. 
  # Args: 
  #   length: How far do we carry the parametric curve. 
  #   step: step size
  #   r: sphere radius. 
  #   apple: a vector that contains the death/life values for each scene (five values in [0,1]), computed using emotivSessionDistance.
  # TODO possibly do these 
  require(rgl)
  
  t <- seq(0,length,by=step)
  # Normalise "apple"
  apple1 <- apple1/max(apple1)
  apple2 <- apple2/max(apple2)
  # Fill out values between scenes. 
  appleCol1 <- makeColorVector(floor(length/step),apple1)
  appleCol2 <- makeColorVector(floor(length/step),apple2)
  # Turn this into an interpolated RGB value vector using colorRamp. 
  ramp1 <- colorRamp(c("white","red"),bias=1,space="rgb")(appleCol1)
  ramp2 <- colorRamp(c("white","#9cf896"),bias=1,space="rgb")(appleCol2)
  
  
  x1 <- sin(11*t + 0.8*cos(2*t) + pi/3)
  y1 <- cos(5*t + 0.8*sin(3*t) + pi/5)
  z1 <- cos(7*t + 0.8 * cos(8*t))
  
  x2 <- sin(11*t + 0.8*cos(2*t) + pi/3) - 0.03
  y2 <- cos(5*t + 0.8*sin(3*t) + pi/5) - 0.03 
  z2 <- cos(7*t + 0.8 * cos(8*t)) - 0.03
  
  
  
  # setup rgl environment:
  zscale <- "1#f9d0cd"
  
  # clear scene:
  clear3d("all")
  
  # setup env:
  bg3d(color="black")
  light3d()
  
  # draw shperes in an rgl window
  spheres3d(x1, y1, z1, radius=r, color=rgb(ramp1,maxColorValue=255))
  spheres3d(x2, y2, z2, radius=r, color=rgb(ramp2,maxColorValue=255))
  
  
}


makeColorVector <- function(n,cols){
  # Make a vector of length n from a vector cols by interpolating between its values. 
  k <- floor(n/(length(cols)-1))
  cv <- list()
  for (i in 1:(length(cols)-1)){
    cv <- c(cv,seq(from=cols[i],to=cols[i+1],length.out=k))
    
  }
  return(unlist(cv))
    
}


