# HTTP Load Balancer

**J-Balancer** is a custom multi-threaded Layer 7 Load Balancer built from scratch in Java. It is designed to distribute HTTP traffic across a cluster of backend servers, ensuring high availability and fault tolerance. 

This project manually implements core distributed systems concepts, including Round Robin scheduling, passive health checks, and connection pooling—using raw TCP Sockets.

## Architecture

**The Flow:**
1.  **Client** sends an HTTP request to the Load Balancer (Port 8080).
2.  **J-Balancer** accepts the connection using a fixed `ExecutorService` (Thread Pool) to handle high concurrency without resource exhaustion.
3.  **Router** selects a backend server using a **Round-Robin** algorithm.
4.  **Health Check:** If the selected backend is unresponsive, the system detects the failure (TCP Connection Refused), automatically retries, and reroutes traffic to a healthy node.
5.  **Bridge:** Data is streamed between the Client and the Backend using byte-level manipulation.

## Key Features

* **Round Robin Scheduling:** Distributes traffic evenly across `N` backend nodes to prevent overloading a single server.
* **Fault Tolerance:** Implements a retry loop mechanism. If a backend node crashes, the load balancer catches the `IOException`, marks the attempt as failed, and transparently reroutes the user to the next available node.
* **Concurrency Management:** Java `ExecutorService` (Fixed Thread Pool) to decouple request submission from execution, preventing `OutOfMemoryErrors` under load.
* **Containerized Infrastructure:** Fully Dockerized application for consistent deployment across environments.

## Tech Stack

* **Core Logic:** Java (JDK 17) - `java.net.ServerSocket`, `java.util.concurrent`.
* **Backend Simulation:** Python (`http.server`) - Simulating a distributed cluster.
* **Infrastructure:** Docker.
* **Tools:** Git, cURL (for testing).

## How to Run

### Option 1: Manual (Local Development)

**1. Start the Backend Cluster (Python)**
Open 3 separate terminal windows to simulate a distributed environment:
```bash
python backend_server.py 8081
python backend_server.py 8082
python backend_server.py 8083
```

**2. Start the Load Balancer (Java)**
In a new terminal:

```bash
javac LoadBalancer.java
java LoadBalancer
```

**3. Test the System**
Visit `http://localhost:8080` or use cURL:

```bash
curl http://localhost:8080
```

*Result:* You should see the response rotate between ports 8081, 8082, and 8083.

### Option 2: Docker (Production-Ready)
Build the image and run it attached to the host network (to allow access to local Python servers):

```bash
# Build the image
docker build -t j-balancer:v1 .

# Run container
docker run -p 8080:8080 --network="host" j-balancer:v1
```


## Testing "Self-Healing" Capabilities
To verify fault tolerance:

1. Start all 3 backend servers.
2. **Kill one server**.
3. Run `curl http://localhost:8080` multiple times.
4. **Observation:** You will never see a connection error. The logs will show:
> *Backend port 8082 is dead. Retrying...*
> *Connected to backend port 8083*

## Future Improvements

* **Active Health Checks:** Implement a background thread that pings servers every 5 seconds to proactively remove dead nodes from the rotation.
* **Configuration File:** Move ports and thread pool size settings to a `config.properties` file.
* **Least Connections Algorithm:** Upgrade scheduling to route traffic to the server with the fewest active connections.