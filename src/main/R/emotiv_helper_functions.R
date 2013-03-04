# Copyright Hannu Rajaniemi 2012


# Intro -------------------------------------------------------------------

# Helper functions to assist in the analysis of Emotiv EPOC data collected with emokit-java. 



# Data loading functions --------------------------------------------------

listEmotivSessions <- function(database.name="zoku"){
  # Lists the Emotiv EPOC sessions recorded with emokit-java stored in the PostgreSQL database database.name.
  # Args: 
  # database.name: The PostgreSQL database name where the data is stored. 
  # Returns: a vector of session names.
  
  
  # Load database libraries. 
  require(DBI) # R relational database interfaces. 
  require(RPostgreSQL) # emokit-java uses PostgreSQL.
  # Load database driver and open a connection. 
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv,dbname=database.name,user="postgres") 
  
  sessions <- dbGetQuery(con,"select * from emotivsession") 
  dbDisconnect(con)
  
  return(sessions$name)
  
  
}


loadEmotivSession <- function(session.name,database.name = "zoku"){
  # Loads an Emotiv EPOC session recorded with emokit-java with the name session.name from the default PostgreSQL database. 
  #
  # Args: 
  #   session.name: The name assigned to the Emotiv session in emokit-java. 
  #   database.name: The PostgreSQL database name. Defaults to zoku but included as parameter for future use. 
  # Returns: 
  # A list of data frames containing the data with session IDs; just a data frame if the session ID is unique. (The session ID should be unique, but there appears to be some confusion with )

  # Load libraries. 
  require(DBI) # R relational database interfaces. 
  require(RPostgreSQL) # emokit-java uses PostgreSQL. 
  
  # Load database driver and open a connection. 
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv,dbname=database.name,user="postgres")  
  
  # Query the "emotivsession" table for the session IDs. 
  # Note that dbGetQuery returns a data frame with all the records. 
  # Need to use paste command to construct the search query. 
  session.query.result <- dbGetQuery(con,paste("select * from emotivsession where name =","'",session.name,"'",sep=""))
  # session.id is a character vector with the ids. 
  session.ids <- session.query.result$id
  
  # Create an empty list to hold the query results. 
  session.results <- list()
  
  # Clumsy index variable to iterate through the session ids and add them to the list. 
  i = 1
  for (id in session.ids){
    # FIXME correct use of quotes below? 
    session.data <- dbGetQuery(con, paste("select * from emotivdatum where session_id = ","'",id,"'",sep=""))
	
	# For convenience, let's also make a new time vector 
	
	
    session.results[[i]] <- session.data
    i <- i+1 
  }
  
  if (length(session.results) > 1){
   message("The session ID was not unique.")
  }
  else {
    # If the session ID is unique, just return the data frame. 
    session.results <- session.results[[1]]
  }
  # TODO add some error handling
  # TODO add a time vector generator so plotting becomes easier... 
 dbDisconnect(con)
  
  return(session.results)
  
}

loadEmotivTimeInterval <- function(timestamp1,timestamp2,database.name = "zoku"){
  # Loads data recorded with an Emotiv EPOC and emokit-java from the default PostgreSQL database, between the given timestamps.
  #
  # Args: 
  #   timestamp1,timestamp2: POSIX timestamps defining the desired time interval. 
  #   database.name: The PostgreSQL database name. Defaults to zoku but included as parameter for future use. 
  # Returns: 
  # A list of data frames containing the data with session IDs. (The session ID should be unique, but there appears to be some confusion with this... )
  
  # Load libraries. 
  require(DBI) # R relational database interfaces. 
  require(RPostgreSQL) # emokit-java uses PostgreSQL. 
  
  # Load database driver and open a connection. 
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv,dbname=database.name,user="postgres")

  # Query the database. 
  interval.data <- dbGetQuery(con,paste("select * from emotivdatum where timestamp between '",timestamp1,"'::timestamp"," and '",timestamp2,"'::timestamp",sep=""))

  dbDisconnect(con)
  
  return(interval.data)
  
  
}


createEmotivTimeVector <- function(eeg.data){
	# Creates a time vector used in plotting and visualisation, in convenient units. 
	# Args: 
	#	eeg.data: a data frame extracted from the emokit-java PostgreSQL database.
	# Returns: a time vector of appropriate length. 
	time.elapsed <- difftime(eeg.data$timestamp[length(eeg.data$timestamp)], eeg.data$timestamp[1], unit = "secs")
	time <- seq(0, as.numeric(time.elapsed) - (as.numeric(time.elapsed)/length(eeg.data$timestamp)), 
		as.numeric(time.elapsed)/length(eeg.data$timestamp))
}

createGgobiObject <- function(eeg.data){
	# Creates a ggobi (http://www.ggobi.org) object out of a session data frame recorded with emokit-java. (Augments it with a time vector for easier visualisation in ggobi.)
	# Args: 
	#	eeg.data: a data frame extracted from the emokit-java PostgreSQL database.
	# Returns: a ggobi object. 
	
	# Load the ggobi library. 
	require(rggobi)
	
	# Create a time vector. 
	time.vector <- createEmotivTimeVector(eeg.data)
	# Add it to the data frame. 
	eeg.data$time <- time.vector
	
	data.ggobi <- ggobi(eeg.data)
	return(data.ggobi)
	
}




# Plotting functions ------------------------------------------------------

plotAllSensors <- function(eeg.data){
  # Creates two plots showing all the electrodes from the eeg.data object for visual insepction. 
  # Args: 
  #   eeg.data: A data frame extractede from the emotiv-java PostgreSQL database using either loadEmotivInterval or loadEmotivSession functions.
  
  # Create a time vector in suitable units: 
  time <- createEmotivTimeVector(eeg.data)
  
  # Set up plot and margins
  # So we can have multiple plots side by side... 
  
  plot.new()
  par(mfrow=c(7,1))
  par(mar=c(2,2,1,0.5)) # These were arrived at by black magic... 
  
  # Create plots. 
  # FIXME note that this does not at the moment show gyrox and gyroy, which should also be plotted for reference! 
  
  plot(time,eeg.data$af3,type="l", axes=F, xlab="", ylab="AF3",ylim=c(8000,9000))
  mtext("AF3",2)
  plot(time,eeg.data$af4,type="l", axes=F, xlab="", ylab="AF4")
  mtext("AF4",2)
  plot(time,eeg.data$f3,type="l", axes=F, xlab="", ylab="F3")
  mtext("F3",2)
  plot(time,eeg.data$f4,type="l", axes=F, xlab="", ylab="F4")
  mtext("F4",2)
  plot(time,eeg.data$f7,type="l", axes=F, xlab="", ylab="F7")
  mtext("F7",2)
  plot(time,eeg.data$f8,type="l", axes=F, xlab="", ylab="F8")
  mtext("F8",2)
  plot(time,eeg.data$fc5,type="l", axes=F, xlab="", ylab="FC5")
  mtext("FC5",2)
  # Add the time axis 
  axis(1,pretty(range(time),20))
  
  # Second set of electrodes ------------------------------------------------------
  
  # Plot some more data... 
  
  par(mfrow=c(7,1))
  par(mar=c(2,2,1,0.5)) # this probably needs changing... 
  plot(time,eeg.data$fc6,type="l", axes=F, xlab="", ylab="FC6")
  mtext("FC6",2)
  plot(time,eeg.data$t7,type="l", axes=F, xlab="", ylab="T7")
  mtext("T7",2)
  plot(time,eeg.data$t8,type="l", axes=F, xlab="", ylab="T8")
  mtext("T8",2)
  plot(time,eeg.data$p7,type="l", axes=F, xlab="", ylab="P7")
  mtext("P7",2)
  plot(time,eeg.data$p8,type="l", axes=F, xlab="", ylab="P8")
  mtext("P8",2)
  plot(time,eeg.data$o1,type="l", axes=F, xlab="", ylab="O1")
  mtext("O1",2)
  plot(time,eeg.data$o2,type="l", axes=F, xlab="", ylab="O2")
  mtext("O2",2)
  
  axis(1,pretty(range(time),20))
  
  
  par(mfrow=c(2,1))
  
  # Plot the gyroscopes
  plot(time,eeg.data$gyrox,type="l", axes=F, xlab="", ylab="gyrox")
  mtext("GX",2)
  
  plot(time,eeg.data$gyroy,type="l",axes=F,xlab="",ylab="gyroy")
  mtext("GY",2)
  
  # Add the time axis 
  axis(1,pretty(range(time),20))
  
}

plotSingleSensor <- function(eeg.data,sensor){
  # Create a plot of a single electrode, chosen from a list of all electrodes, with an adjustable slider for the time axis. Intended for visual exploration of one sensor. 
  # Args: 
  #   eeg.data: A data frame extractede from the emotiv-java PostgreSQL database using either loadEmotivInterval or loadEmotivSession functions.
  # Returns: A plot with a menu that allows a choice of electrode and a slider to adjust the time range. 
  
  # FIXME: Note that this does not work outside Rstudio! 
  
  # This uses the Rstudio manipulate library. 
  require(manipulate)
  
  # get a list of column names
  sensors <- colnames(eeg.data)
  # Create a time vector. 
  time <- createEmotivTimeVector(eeg.data)
  
  # Note that this seems to be the most elegant way to access factor names via picker. 
  
  plot.new()
  manipulate(
    plot(time,eeg.data[[sensor]],
                  type="l", axes=F, xlab="", ylab="",xlim=c(x.min,x.max)),
             sensor = picker(as.list(sensors)),
             x.max=slider(0,150),
             x.min=slider(0,150)
  )

  
}

compareTwoSensors <- function(eeg.data){
  
  # TODO: should be possible to create a plot of *multiple* sensors from a list...
  
  
  
}