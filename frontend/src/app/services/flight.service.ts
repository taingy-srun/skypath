import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { FlightSearchRequest, FlightSearchResponse } from '../models/flight.model';

@Injectable({
  providedIn: 'root'
})
export class FlightService {
  private apiUrl = '/api/flights';

  constructor(private http: HttpClient) {}

  searchFlights(request: FlightSearchRequest): Observable<FlightSearchResponse> {
    return this.http.post<FlightSearchResponse>(`${this.apiUrl}/search`, request);
  }

}
