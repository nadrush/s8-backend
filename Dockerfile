FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the maven wrapper and pom.xml
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy the source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Create a non-root user
RUN addgroup --system spring && adduser --system spring --ingroup spring

# Copy the jar file
COPY target/*.jar app.jar

# Change ownership of the app directory
RUN chown -R spring:spring /app

# Switch to the non-root user
USER spring:spring

# Expose the port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
