echo "Starting Spring Boot server..."
cd API
./mvnw.cmd spring-boot:run & 
# Start the Angular project
echo "Starting Angular project..."
cd ../GUI/inz-gui/
npm install
npm start & 