mvn -f books/pom.xml clean
mvn -f books/pom.xml package

mvn -f authors/pom.xml clean
mvn -f authors/pom.xml package

mvn -f web-sockets/pom.xml clean
mvn -f web-sockets/pom.xml package

mvn -f frontend/pom.xml clean
mvn -f frontend/pom.xml package

docker build -t oharkusha/bff-books-service:1.0 -f books/Dockerfile books
docker build -t oharkusha/bff-authors-service:1.0 -f authors/Dockerfile authors
docker build -t oharkusha/bff-web-sockets-service:1.0 -f web-sockets/Dockerfile web-sockets
docker build -t oharkusha/bff-frontend:1.0 -f frontend/Dockerfile frontend
