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
  
  er = 1 / mu * (1 + pq / (m * (1 - ro))) * 1000 - 0.5
}

loadedRecords <- read.csv(file = 'data.csv')

# number of processing units
m = 4

# service rate
mu = 595

results <- lapply(loadedRecords, mmmQ_ert, m = m, mu = mu)
write.csv(results, "output.csv", row.names = FALSE)


mva <- function() {
  
  # number of clients
  N = 32
  servers <- c("fixed_capacity", "load_dependent", "load_dependent")
  
  M = length(servers)
  Q <- c(0)
  P <- list(c(1), c(1), c(1))
  
  # fixed capacity: mean service time
  S <- c(0.00016, 0, 0)
  
  mu = 2041
  # service rate: load dependent server
  u <- list(c(0), c(mu, 1.85 * mu), c(mu, 1.85 * mu))
  
  # num of visits to i-th device
  Vi <- c(1, 0.5, 0.5)
  
  # thinking time
  Z = 0.00098
  
  # system response time
  R = 0
  
  # response time of i-th device
  Ri <- c(0)
  
  # system throughput
  X = 0
  
  # stores system throughput for each iteration
  tputs <- c()

  
  # throughput of i-th device
  Xi <- c(0)
  
  # utilization of i-th device
  Ui <- c(0)
  
  utilizations <- data.frame()
  
  
  for (i in 1:M) { 
    Q[i] = 0
    P[[i]][[1]] = 1
  }
  
  for (n in 1:N) {
    
    for (i in 1:M) {
      
      if (servers[i] == "fixed_capacity") {
        
        Ri[i] = S[i] * (1 + Q[i])
        
      
      } else if (servers[i] == "load_dependent") {
        
        sum = 0
      
        for (j in 1 : n) {
          
          # if n exceeds u[], then get last element from u
          uij = 1
          if (j <= length(u[[i]])) {
            uij = u[[i]][[j]]  
          } else {
            uij = u[[i]][[length(u[[i]])]]
          }
          
          sum = sum + P[[i]][[j]] * (j) / uij
        }
        
        Ri[i] = sum
        
      } else {
        print("illegal service center name:" + servers[i])
      }
      
    }
    
    
    R = 0
    for (i in 1:M) {
      R = R + Ri[i] * Vi[i]
    }
    
    X = n / (Z + R)
    
    
    for (i in 1:M) {
      if (servers[i] == "fixed_capacity") {
        
        Q[i] = X * Vi[i] * Ri[i]
        
      } else if (servers[i] == "load_dependent") {
        for (j in n:1) {
          
          
          # if n exceeds u[], then get last element from u
          uij = 1
          if (j <= length(u[[i]])) {
            uij = u[[i]][[j]]  
          } else {
            uij = u[[i]][[length(u[[i]])]]
          }
          
          P[[i]][[j + 1]] = (X / uij) * P[[i]][[j]]
        }
        
        sum = 0
        for (j in 1:n) {
          sum = sum + P[[i]][[j + 1]]
        }
        
        P[[i]][[1]] = 1 - sum
        
      }
    }
    
    
    for (i in 1:M) {
      Xi[i] = X * Vi[i]
    }
    
    for (i in 1:M) {
      if (servers[i] == "fixed_capacity") {
        
        Ui[i] = X * S[i] * Vi[i]
        
      } else if (servers[i] == "load_dependent") {
      
        Ui[i] = 1 - P[[i]][[1]]
      
      }
    }
    
    #print("")
    #print(Ri * 1000)
    #print(R * 1000)
    #print(X)
    #print(Xi)
    #print(Ui)
    
  
    if ((n != 15 && n <= 16) || n == 18 || n == 24 || n == 32 || n == 64) {
      
      tputs = append(tputs, X)
      utilizations <- rbind(utilizations, data.frame(Ui[1], Ui[2], Ui[3]))
    }
    
    
  }
  
  tputs
  #utilizations
  
  
}

#print (mva())

#write.csv(mva(), "output.csv", row.names = FALSE)
