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
m = 2

# service rate
mu = 1260

results <- lapply(loadedRecords, mmmQ_ert, m = m, mu = mu)
write.csv(results, "output.csv", row.names = FALSE)


mva <- function() {
  
  # number of clients
  N = 5
  servers <- c("fixed_capacity", "load_dependent")
  
  M = length(servers)
  Q <- c(0)
  P <- list(c(1), c(1))
  
  # fixed capacity: mean service time
  S <- c(0.26, 0)
  
  # service rate: load dependent server
  u <- list(c(0), c(0.32, 0.39, 0.42))
  
  # num of visits to i-th device
  Vi <- c(6, 1)
  
  # thinking time
  Z = 0
  
  # system response time
  R = 0
  
  # response time of i-th device
  Ri <- c(0)
  
  # system throughput
  X = 0
  
  # throughput of i-th device
  Xi <- c(0)
  
  # utilization of i-th device
  Ui <- c(0)
  
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
        print("")
        for (j in n:1) {
          
          uij = 1
          
          if (j <= length(u[[i]])) {
            uij = u[[i]][[j]]  
          } else {
            uij = u[[i]][[length(u[[i]])]]
          }
          
          P[[i]][[j + 1]] = (X / uij) * P[[i]][[j]]
          print(uij)
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
    
    #print(Ri)
    #print(R)
    #print(X)
    #print(Ui)
  
    
  }
  

  
}

print(mva())