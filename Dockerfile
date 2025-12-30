FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY LoadBalancer.java /app/LoadBalancer.java
RUN javac LoadBalancer.java
CMD ["java", "LoadBalancer"]