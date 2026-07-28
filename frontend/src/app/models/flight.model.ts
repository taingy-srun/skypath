export interface FlightSegment {
  flightNumber: string;
  airline: string;
  origin: string;
  destination: string;
  departureTime: string;
  arrivalTime: string;
  price: number;
  aircraft: string;
  durationMinutes: number;
}

export interface Itinerary {
  segments: FlightSegment[];
  layoverDurationsMinutes: number[];
  totalDurationMinutes: number;
  totalPrice: number;
}

export interface FlightSearchRequest {
  origin: string;
  destination: string;
  date: string;
}

export interface FlightSearchResponse {
  origin: string;
  destination: string;
  date: string;
  totalResults: number;
  itineraries: Itinerary[];
}
