# SkyPath Flight Search Engine

A full-stack flight connection search application that finds direct flights and flights with up to 2 stops.

## Technology Stack

### Backend
- Java 21
- Spring Boot 4.1.0
- Maven

### Frontend
- Angular 18
- TypeScript

## Prerequisites

- Docker and Docker Compose
- (Optional for local development) Java 21, Node.js 24.7.0, Maven

## Running with Docker

### Build and Run

```bash
# Build and start both backend and frontend
docker-compose up --build

# Or run in detached mode
docker-compose up -d --build
```

The application will be available at:
- **Frontend**: http://localhost
- **Backend API**: http://localhost:8080/api

### Stop the Application

```bash
docker-compose down
```

### View Logs

```bash
# View all logs
docker-compose logs -f

# View backend logs only
docker-compose logs -f backend

# View frontend logs only
docker-compose logs -f frontend
```

## Running Locally (Development)

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

The backend API will be available at http://localhost:8080

### Frontend

```bash
cd frontend
npm install
npm start
```

The frontend will be available at http://localhost:4200

## API Endpoints

### Search Flights

**POST** `/api/flights/search`

Request body:
```json
{
  "origin": "JFK",
  "destination": "LAX",
  "date": "2024-03-15"
}
```

Response:
```json
{
  "origin": "JFK",
  "destination": "LAX",
  "date": "2024-03-15",
  "totalResults": 10,
  "itineraries": [...]
}
```

## Features

- Search for flights between any two airports
- Supports direct flights, 1-stop, and 2-stop connections
- Google Flights-inspired UI
- Collapsible flight details with timeline view
- Displays flight segments, layovers, prices, and duration

## Testing

### Backend Tests

```bash
cd backend
./mvnw test
```

## Project Structure

```
skypath/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/skypath/backend/
│   │   │   │   ├── controller/        # REST API endpoints
│   │   │   │   ├── service/           # Business logic
│   │   │   │   ├── model/             # Domain models
│   │   │   │   ├── dto/               # Data transfer objects
│   │   │   │   ├── validator/         # Input validation
│   │   │   │   └── exception/         # Exception handling
│   │   │   └── resources/             # Application properties
│   │   └── test/                      # Unit and integration tests
│   ├── Dockerfile
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── app/
│   │   │   ├── components/            # Angular components
│   │   │   ├── models/                # TypeScript interfaces
│   │   │   └── services/              # HTTP services
│   ├── Dockerfile
│   ├── nginx.conf
│   └── package.json
├── flights.json                       # Flight data
└── docker-compose.yml                 # Container orchestration
```

## Architecture Decisions

### Backend Architecture

**1. Layered Architecture Pattern**
- **Controller Layer**: Handles HTTP requests/responses and input validation
- **Service Layer**: Contains core business logic for flight search algorithms
- **Model Layer**: Domain entities (Flight, Itinerary, FlightSegment)
- **DTO Layer**: Separates API contracts from internal models

**Why this approach?**
- **Separation of Concerns**: Each layer has a single, well-defined responsibility
- **Testability**: Service layer can be unit tested without HTTP concerns
- **Maintainability**: Changes to API structure don't affect business logic
- **Scalability**: Easy to add new features or modify existing ones

**2. In-Memory Data Loading**
- Flight data is loaded from JSON file into memory at application startup
- No database required for this MVP

**Why this approach?**
- **Simplicity**: Faster development for MVP/prototype
- **Performance**: All data in memory = instant access, no I/O overhead
- **Stateless**: Enables easy horizontal scaling
- **Cost-effective**: No database infrastructure needed

**Tradeoff**: Limited to datasets that fit in memory, data changes require restart

**3. Graph Traversal Algorithm for Flight Connections**
- Used breadth-first search (BFS) approach to find connecting flights
- Builds connection chains by exploring direct flights first, then 1-stop, then 2-stop

**Why this approach?**
- **Correctness**: BFS guarantees we find all possible connections
- **Optimal Results**: Returns shortest connection chains first
- **Predictable Performance**: O(n²) for 1-stop, O(n³) for 2-stop where n = number of flights
- **Simple to Understand**: Easier to test and maintain than complex graph libraries

**Tradeoff**: Not optimized for very large datasets, but sufficient for typical airport connection scenarios

**4. Spring Boot Framework**
- **Why?**: Industry standard, extensive ecosystem, built-in features (REST, validation, dependency injection)
- **Tradeoff**: Heavier than lightweight frameworks, but worth it for developer productivity

### Frontend Architecture

**1. Component-Based Architecture (Angular)**
- Single primary component (`flight-search`) handles search form and results display
- Standalone components approach (Angular 18+)

**Why this approach?**
- **Modularity**: Self-contained, reusable components
- **Type Safety**: TypeScript catches errors at compile time
- **Developer Experience**: Angular CLI, robust tooling
- **Enterprise-Ready**: Scalable for larger applications

**2. Google Flights-Inspired UI Design**
- Material Design principles
- Collapsible panels with smooth animations
- Timeline visualization for flight segments

**Why this approach?**
- **Familiar UX**: Users already know how Google Flights works
- **Information Density**: Compact summary view with detailed expansion
- **Professional Appearance**: Modern, clean design
- **Mobile-Responsive**: Adapts to different screen sizes

**3. Service Layer Pattern**
- `FlightService` handles all HTTP communication
- Separates API logic from component logic

**Why this approach?**
- **Reusability**: Multiple components can use the same service
- **Testability**: Easy to mock HTTP calls in tests
- **Maintainability**: API changes only affect the service

### Data Flow

```
User Input → FlightSearchComponent → FlightService → Backend API
                        ↓
                  Update UI with results
```

### DevOps & Deployment

**1. Docker Multi-Stage Builds**
- Backend: Maven build stage + JRE runtime stage
- Frontend: Node build stage + Nginx runtime stage

**Why this approach?**
- **Smaller Images**: Only runtime dependencies in final image
- **Security**: Fewer packages = smaller attack surface
- **Performance**: Faster image pulls and container startup
- **Build Consistency**: Same build process everywhere (dev, CI, prod)

**2. Docker Compose Orchestration**
- Single command to run entire stack
- Shared network for service communication

**Why this approach?**
- **Developer Experience**: Easy local development
- **Environment Parity**: Dev environment matches production
- **Documentation as Code**: docker-compose.yml documents the stack

**3. Nginx as Frontend Server**
- Serves static files
- Proxies API requests to backend

**Why this approach?**
- **Performance**: Nginx is highly optimized for serving static content
- **Production-Ready**: Same server used in development and production
- **CORS Handling**: Proxy eliminates CORS issues
- **Scalability**: Can easily add load balancing, caching, SSL

## What I Would Improve With More Time

### 1. Backend Improvements

**Database Integration**
- Migrate to PostgreSQL with flight schedule tables
- Add indexes on origin, destination, departure date
- Enable real-time data updates without restart
- Support pagination for large result sets
- **Impact**: Production-ready data persistence

**Search Algorithm Optimization**
- Implement A* pathfinding for faster connection discovery
- Add parallel processing for multi-stop searches
- Index flights by airport for O(1) lookup
- Implement early termination when enough results found
- **Impact**: 2-5x faster search for complex queries

**Advanced Features**
- Price filtering (min/max price)
- Time preferences (prefer morning/evening departures)
- Airline preferences
- Connection time limits (minimum/maximum layover)
- Multi-city search
- Return flight search
- **Impact**: Better user experience, more useful results

**Monitoring & Observability**
- Add Spring Boot Actuator for health checks
- Implement structured logging (JSON logs)
- Add metrics with Micrometer/Prometheus
- Distributed tracing with OpenTelemetry
- Performance monitoring with APM tools
- **Impact**: Production readiness, easier debugging

**API Enhancements**
- Add pagination (limit, offset)
- Implement sorting options (price, duration, stops)
- Add API versioning (/api/v1/flights)
- OpenAPI/Swagger documentation
- Rate limiting
- **Impact**: Better API design, prevent abuse

### 2. Frontend Improvements

**User Experience**
- Add autocomplete for airport codes
- Show airport names, not just codes
- Add date picker with calendar UI
- Display airline logos (not just placeholder icons)
- Add loading skeletons instead of "Searching..."
- Show search history (recent searches)
- Add "Book Now" button with external links
- **Impact**: Significantly better UX

**Advanced Filtering**
- Filter by number of stops
- Filter by airline
- Sort by price, duration, departure time
- Filter by time of day
- Price range slider
- **Impact**: Users can find their ideal flight faster

**Performance Optimization**
- Implement virtual scrolling for long result lists
- Lazy load flight details (only when expanded)
- Add service worker for offline support
- Optimize CSS (remove unused styles)
- Code splitting for faster initial load
- **Impact**: Better performance, especially on mobile

**Testing**
- Add unit tests for components
- Add integration tests with TestBed
- Add E2E tests with Playwright
- Visual regression testing
- **Impact**: Confidence in deployments, fewer bugs

### 3. DevOps & Infrastructure

**CI/CD Pipeline**
- GitHub Actions or GitLab CI
- Automated testing on every commit
- Automated Docker builds
- Deploy to staging on merge to main
- Deploy to production on release tag
- **Impact**: Faster, safer deployments

**Security**
- Add HTTPS/TLS certificates
- Implement rate limiting
- Add request validation and sanitization
- Security headers (CSP, HSTS, etc.)
- Dependency vulnerability scanning
- Container image scanning
- **Impact**: Secure production application

**Observability**
- Centralized logging (ELK stack)
- Metrics dashboard (Grafana)
- Alerting (PagerDuty, OpsGenie)
- Error tracking (Sentry)
- **Impact**: Proactive issue detection

### 4. Data Quality

**Flight Data Enhancements**
- Real flight schedule data from APIs (Amadeus, Skyscanner)
- Include airport metadata (city, country, timezone)
- Add airline information (name, logo URL)
- Include aircraft types with seat maps
- Add baggage allowance info
- Include fare classes and restrictions
- **Impact**: Real-world usefulness

**Data Validation**
- Validate flight times are chronological
- Ensure layover times are realistic (>30min, <24hr)
- Check for duplicate flight numbers
- Validate airport codes exist
- **Impact**: Higher data quality

### 5. Additional Features

**User Accounts**
- User registration and authentication (JWT)
- Save favorite routes
- Flight price alerts
- Booking history
- **Impact**: Personalized experience

**Multi-Language Support**
- i18n with Angular i18n
- Support multiple currencies
- Localized date/time formats
- **Impact**: Global accessibility

## Summary

The architecture prioritizes simplicity and developer productivity for an MVP while remaining extensible for future enhancements. The chosen technologies (Spring Boot + Angular) provide a solid foundation that can scale to production workloads with the improvements outlined above.

