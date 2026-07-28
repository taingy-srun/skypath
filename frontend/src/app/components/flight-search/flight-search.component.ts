import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FlightService } from '../../services/flight.service';
import { FlightSearchRequest, FlightSearchResponse, Itinerary } from '../../models/flight.model';

@Component({
  selector: 'app-flight-search',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './flight-search.component.html',
  styleUrl: './flight-search.component.css'
})
export class FlightSearchComponent implements OnInit {
  // Form fields
  origin: string = '';
  destination: string = '';
  date: string = '';

  // Results
  searchResponse: FlightSearchResponse | null = null;
  isLoading: boolean = false;
  errorMessage: string = '';
  expandedItineraries: Set<number> = new Set();

  constructor(private flightService: FlightService) {}

  ngOnInit(): void {
  }

  toggleItinerary(index: number): void {
    if (this.expandedItineraries.has(index)) {
      this.expandedItineraries.delete(index);
    } else {
      this.expandedItineraries.add(index);
    }
  }

  isExpanded(index: number): boolean {
    return this.expandedItineraries.has(index);
  }

  onSearch(): void {
    // Validate inputs
    if (!this.origin || !this.destination || !this.date) {
      this.errorMessage = 'Please fill in all fields';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.searchResponse = null;

    const request: FlightSearchRequest = {
      origin: this.origin.trim().toUpperCase(),
      destination: this.destination.trim().toUpperCase(),
      date: this.date
    };

    this.flightService.searchFlights(request).subscribe({
      next: (response) => {
        this.searchResponse = response;
        this.isLoading = false;
        this.expandedItineraries.clear(); // Reset expanded state on new search
        console.log('Search results:', response);
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = error.error?.message || 'An error occurred while searching for flights';
        console.error('Search error:', error);
      }
    });
  }

  formatDuration(minutes: number): string {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return `${hours}h ${mins}m`;
  }

  formatDateTime(dateTime: string): string {
    return new Date(dateTime).toLocaleString('en-US', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getStopsLabel(itinerary: Itinerary): string {
    const stops = itinerary.segments.length - 1;
    if (stops === 0) return 'Nonstop';
    if (stops === 1) return '1 stop';
    return `${stops} stops`;
  }
}
