# generates visualisations of the Neurowhisky braindump
# @author Sam Halliday

require(DBI)
require(RPostgreSQL)

setPdfOut <- function(filename){
	if (interactive()) {
		quartz()
	} else {
		pdf(filename, width=11, height=8.5)
	}
}

con <- dbConnect(PostgreSQL(), dbname="zoku", user="postgres")  
query <- function(sql) {
	return(dbGetQuery(con, sql))
}

# sensors <- query("select column_name from information_schema.columns where table_name='emotivdatum'")
slice <- c("af3","af4","f3","f4","f7","f8","fc5","fc6","o1","o2","p7","p8","t7","t8")

toTimeseries <- function(eeg.data) {
  data_ts <- list()
  for (sensor in slice) {
    data_ts[[sensor]] <- ts(eeg.data[[sensor]], frequency=127)
  }
  return(data_ts)
}

getSessionData <- function(id) {
  return(query(paste("select * from emotivdatum where session_id = ", "'", id, "'", sep="")))
}

plotEegHists <- function(session, data, xlimits=c(8000,10000)){
	par(mfrow=c(5,3), mai=c(0,0,0.25,0), oma=c(0,0,4,0))	
	for (sensor in slice) {
		hist(data[[sensor]], breaks=1000, main=sensor, xlab=NULL, ylab=NULL, axes=F, xlim=xlimits)
	}
	title(main=paste(session[["name"]], " (", session[["notes"]], ")", sep=""), outer=TRUE)
}


neurowhisky.sessions <- query("select * from emotivsession where name like 'Neurowhisky%';")

for(i in 1:nrow(neurowhisky.sessions)) {
    session <- neurowhisky.sessions[i,]
    data <- getSessionData(session[["id"]])
	setPdfOut(paste(sub(" ", "_", session[["name"]]), ".pdf", sep=""))
	plotEegHists(session, data)
}


dbDisconnect(con)
