# Transaction Management System

## Overview

A high-performance, RESTful transaction management microservice built with Spring Boot 17. This application provides comprehensive CRUD operations for financial transactions with built-in caching, validation, and monitoring capabilities.

## Features

- **RESTful API**: Clean and well-structured API design with context path `/transaction-management`
- **Caching Mechanism**: Built-in caching with Spring Cache for improved performance
- **Data Validation**: Robust input validation and exception handling
- **Pagination Support**: Efficient data queries with pagination
- **Monitoring**: Spring Boot Actuator integration for health checks and metrics
- **Containerization**: Docker support with optimized container configuration
- **Comprehensive Testing**: Unit tests, integration tests, and stress testing

## Technology Stack

- **Java 17**: Latest LTS version for optimal performance
- **Spring Boot 3**: Modern framework for microservices
- **Spring Data JPA**: Database abstraction layer
- **H2 Database**: In-memory database for development and testing
- **Spring Cache**: Caching abstraction with Caffeine implementation
- **Spring Boot Actuator**: Application monitoring and management
- **Docker**: Containerization support
- **Maven**: Build automation and dependency management

## API Endpoints

### Base URL
All endpoints are prefixed with: `/transaction-management`

### Transaction Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/transactions` | Create a new transaction |
| `GET` | `/api/v1/transactions/{id}` | Get transaction by ID |
| `GET` | `/api/v1/transactions/reference/{reference}` | Get transaction by reference |
| `GET` | `/api/v1/transactions` | Get all transactions (with pagination and filtering) |
| `PUT` | `/api/v1/transactions/{id}` | Update existing transaction |
| `DELETE` | `/api/v1/transactions/{id}` | Delete transaction by ID |
| `DELETE` | `/api/v1/transactions/reference/{reference}` | Delete transaction by reference |

### Monitoring Endpoints

| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Application health status |
| `/actuator/info` | Application information |
| `/actuator/metrics` | Performance metrics |
| `/actuator/beans` | Spring beans information |
| `/actuator/env` | Environment variables |

## API Documentation

This application includes Swagger for API documentation. Swagger provides an interactive UI to explore and test the API endpoints.

### Accessing Swagger UI

Once the application is running, you can access the Swagger UI at:

```
http://localhost:8080/transaction-management/swagger-ui/index.html
```

### Swagger JSON

The raw Swagger JSON can be accessed at:

```
http://localhost:8080/transaction-management/api-docs
```

Use this for integration with tools like Postman or other API clients.

## Quick Start

### Prerequisites

- Java 17 or later
- Maven 3.6 or later
- Docker (optional, for containerization)

### Local Development

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd transaction-management
    ```
2. **Build the project**
   ```bash
   mvn clean package
   ```
3. **Run the application**
   ```bash
   java -jar target/transaction-management-1.0.0.jar
    ```
   
4. **Access the API**
   - The API will be available at `http://localhost:8080/transaction-management/api/v1/transactions`
   - Use tools like [Postman](https://www.postman.com/) or `curl` to interact with the endpoints.

## Example API Usage

### Create a Transaction

```bash
curl -X POST http://localhost:8080/transaction-management/api/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{
        "transactionReference": "TXN123456",
        "description": "Payment for invoice #123",
        "amount": 150.75,
        "type": "DEBIT",
        "category": "INVOICE"
      }'
```

### Get Transaction by ID

```bash
curl http://localhost:8080/transaction-management/api/v1/transactions/1
```

### Get All Transactions (Paginated)

```bash
curl "http://localhost:8080/transaction-management/api/v1/transactions?page=0&size=10"
```

## Running Tests

To run unit and integration tests:

```bash
mvn test
```

## Docker Usage

Build the Docker image (replace `1.0.0` with your actual version if needed):

```bash
docker build --build-arg APP_VERSION=1.0.0 -t transaction-management:1.0.0 -f docker/Dockerfile .
```

Run the container:

```bash
docker run -p 8080:8080 transaction-management:1.0.0
```

## Kubernetes Deployment

Kubernetes manifests are located in the `kubernetes` directory:

- `transaction-management-deployment.yaml`: Defines the deployment for the application.
- `transaction-management-service.yaml`: Exposes the application as a Kubernetes Service.

**Deployment Steps:**

1. Push your Docker image to a registry accessible by your cluster.
2. Update the image reference in `transaction-management-deployment.yaml` (set the tag to match your `APP_VERSION`).
3. Apply the deployment and service manifests:

```bash
kubectl apply -f kubernetes/transaction-management-deployment.yaml
kubectl apply -f kubernetes/transaction-management-service.yaml
```

4. Check pod and service status:

```bash
kubectl get pods
kubectl get services
```

Access the service according to your Kubernetes service configuration.

**Note:**  
The `APP_VERSION` variable is used to tag your Docker image and should match the version referenced in your Kubernetes deployment YAML file under the container image field.

## Configuration

- Application properties can be configured in `src/main/resources/application.yml`.
- Logging configuration is in `src/main/resources/logback-spring.xml`.

## Health Check

The application exposes a health endpoint at:

```
http://<host>:8080/transaction-management/actuator/health
```

## Environment Variables

- `JAVA_OPTS`: JVM options for container optimization.
- `SPRING_APPLICATION_JSON`: Used to set the context path.

## License

This project is licensed under the MIT License.

## Contact

For questions or support, contact the maintainer at [80996060@qq.com].
