#!/bin/bash

echo "🚀 Starting Kite Algo Trading Application..."

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 17 or higher."
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven 3.6+."
    exit 1
fi

# Check if PostgreSQL is running
if ! command -v psql &> /dev/null; then
    echo "⚠️  PostgreSQL is not installed or not in PATH."
    echo "   Please install PostgreSQL and create a database named 'kite_trading'"
fi

# Build the project
echo "📦 Building project..."
mvn clean install -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Build failed. Please check the error messages above."
    exit 1
fi

# Run the application
echo "🎯 Starting application..."
mvn spring-boot:run

echo "✅ Application started successfully!"
echo "🌐 Dashboard available at: http://localhost:8080/index.html"
