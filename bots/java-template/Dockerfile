FROM mcr.microsoft.com/openjdk/jdk:21-ubuntu
COPY . /app
WORKDIR /app
RUN find . -name "*.java" > sources.txt && javac @sources.txt
CMD ["java", "-cp", "src/main/java:src:.", "Main"]