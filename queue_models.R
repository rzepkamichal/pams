sumit <- function(m, ro) {
  sum = 0
  for (i in 1:m-1) {
    sum = sum + ((m * ro)^i)/factorial(i)
  }
  
  sum
}

# Calculates expected response time for an M/M/m queue
mmmQ_ert <- function(m, mu, lambda) {
  ro = lambda / (m * mu)
  
  p0 = 1 + ((m * ro)^m) / (factorial(m) * (1 - ro)) + sumit(m, ro)
  p0 = 1.0 / p0
  
  pq = (((m * ro)^m) / (factorial(m) * (1 - ro))) * p0
  
  er = 1 / mu * (1 + pq / (m * (1 - ro))) * 1000  
}

loadedRecords <- read.csv(file = 'data.csv')

# number of processing units
m = 4

# service rate
mu = 518

results <- lapply(loadedRecords, mmmQ_ert, m = m, mu = mu)
write.csv(results, "output.csv", row.names = FALSE)


