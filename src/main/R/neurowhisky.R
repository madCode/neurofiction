# generates visualisations of the Neurowhisky braindump (tasting 8 different whiskies)
# @author Sam Halliday

# Notes on the data:
#
# 1. the "sitting" field is not correctly saved, ignore it
# 2. the headset was removed and replaced after the second session
# 3. the headset was bumped in the 8th session
#
# a supplementary session was recorded showing the difference to eating.

require(DBI)
require(RPostgreSQL)

setPdfOut <- function(filename){
	if (interactive()) {
		quartz()
	} else {
		pdf(filename, width=11, height=8.5)
	}
}

setPngOut <- function(filename){
	if (interactive()) {
		quartz()
	} else {
		png(filename, width=1920, height=1080)
	}
}


con <- dbConnect(PostgreSQL(), dbname="zoku", user="postgres")  
query <- function(sql) {
	return(dbGetQuery(con, sql))
}

# sensors <- query("select column_name from information_schema.columns where table_name='emotivdatum'")
slice <- c("af3","af4","f3","f4","f7","f8","fc5","fc6","o1","o2","p7","p8","t7","t8")
sensor_limits <- c(7000, 11000)

toTimeseries <- function(eeg.data) {
  data_ts <- list()
  for (sensor in slice) {
    data_ts[[sensor]] <- ts(eeg.data[[sensor]], frequency=127)
  }
  return(data_ts)
}

getSessionData <- function(id) {
  return(query(paste("select * from emotivdatum where session_id = ", "'", id, "' order by timestamp", sep="")))
}

plotEegHists <- function(session, data){
	par(mfrow=c(5,3), mai=c(0,0,0.25,0), oma=c(0,0,4,0))
	for (sensor in slice) {
		hist(data[[sensor]], breaks=1000, main=sensor, xlab=NULL, ylab=NULL, axes=F, xlim=sensor_limits)
	}
	title(main=paste(session[["name"]], " (", session[["notes"]], ")", sep=""), outer=TRUE)
}


plotEeg <- function(session, data) {
	par(mfrow=c(length(slice), 1), mai=c(0,1,0.25,0), oma=c(0,0,4,0))
	for (sensor in slice) {
		plot(data[["timestamp"]], data[[sensor]], axes=F, type="l", ylab=sensor, ylim=sensor_limits)
	}
	title(main=paste(session[["name"]], " (", session[["notes"]], ")", sep=""), outer=TRUE)
}

neurowhisky.sessions <- query("select * from emotivsession where name like 'Neurowhisky%' or name like 'eating%'")

for(i in 1:nrow(neurowhisky.sessions)) {
    session <- neurowhisky.sessions[i,]
    data <- getSessionData(session[["id"]])
	setPngOut(paste(sub(" ", "_", session[["name"]]), "-timeseries.png", sep=""))
	plotEeg(session, data)
	setPdfOut(paste(sub(" ", "_", session[["name"]]), "-histograms.pdf", sep=""))
	plotEegHists(session, data)
}

dbDisconnect(con)
