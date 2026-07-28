import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { FlightSearchComponent } from './components/flight-search/flight-search.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, FlightSearchComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'SkyPath Flight Search';
}
